package org.meanbean.factories;

import org.kohsuke.MetaInfServices;
import org.meanbean.lang.Factory;
import org.meanbean.util.Order;
import org.meanbean.util.RandomValueGenerator;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.meanbean.util.Types.getRawType;

/**
 * FactoryCollection for java.util.Collection types
 */
@Order(4000)
@MetaInfServices
public class CollectionFactoryLookup implements FactoryLookup {

	private final RandomValueGenerator randomValueGenerator = RandomValueGenerator.getInstance();

	private Map<Class<?>, Factory<?>> collectionFactories = buildDefaultCollectionFactories();
	private int maxSize = 8;

	public int getMaxSize() {
		return maxSize;
	}

	public void setMaxSize(int maxArrayLength) {
		this.maxSize = maxArrayLength;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> Factory<T> getFactory(Type typeToken) throws IllegalArgumentException, NoSuchFactoryException {
		return (Factory<T>) createCollectionPopulatingFactory(typeToken);
	}

	private Factory<?> findItemFactory(Type itemType) {
		FactoryCollection factoryCollection = FactoryCollection.getInstance();
		try {
			return factoryCollection.getFactory(itemType);
		} catch (NoSuchFactoryException e) {
			return factoryCollection.getFactory(void.class);
		}
	}

	@Override
	public boolean hasFactory(Type type) {
		Class<?> clazz = org.meanbean.util.Types.getRawType(type);
		return !clazz.equals(void.class) && (Collection.class.isAssignableFrom(clazz) || Map.class.isAssignableFrom(clazz));
	}

	private Type findElementType(Type type, int index) {
		if (type instanceof ParameterizedType) {
			return ((ParameterizedType) type).getActualTypeArguments()[index];
		}
		return String.class;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Factory<?> createCollectionPopulatingFactory(Type typeToken) {
		Class<?> rawType = getRawType(typeToken);
		Factory<Object> instanceFactory = findCollectionInstanceFactory(typeToken, rawType);

		Type itemType = findElementType(typeToken, 0);
		Factory<?> itemFactory = findItemFactory(itemType);

		if (Map.class.isAssignableFrom(rawType)) {
			return createMapPopulatingFactory(typeToken, instanceFactory, itemFactory);

		} else {
			Factory<Object> populatingFactory = () -> {
				Collection collection = (Collection) instanceFactory.create();

				int size = randomValueGenerator.nextInt(maxSize);
				for (int idx = 0; idx < size; idx++) {
					collection.add(itemFactory.create());
				}
				return collection;
			};

			return populatingFactory;
		}
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Factory<?> createMapPopulatingFactory(Type typeToken, Factory<Object> instanceFactory, Factory<?> itemFactory) {
		Type valueType = findElementType(typeToken, 1);
		Factory<?> valueFactory = findItemFactory(valueType);

		Factory<Object> populatingFactory = () -> {
			Map map = (Map) instanceFactory.create();

			int size = randomValueGenerator.nextInt(maxSize);
			for (int idx = 0; idx < size; idx++) {
				map.put(itemFactory.create(), valueFactory.create());
			}
			return map;
		};

		return populatingFactory;
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private <T> Factory<T> findCollectionInstanceFactory(Type type, Class<?> rawType) {
		if (isEnumMap(type, rawType)) {
			Type keyType = findElementType(type, 0);
			return () -> (T) new EnumMap((Class) keyType);
		}

		if (isEnumSet(type, rawType)) {
			Type keyType = findElementType(type, 0);
			return () -> (T) EnumSet.noneOf((Class) keyType);
		}

		Factory<?> factory = collectionFactories.get(rawType);
		if (factory == null) {
			factory = () -> {
				try {
					return rawType.getConstructor().newInstance();
				} catch (Exception e) {
					throw new IllegalStateException("cannot create instance for " + rawType, e);
				}
			};
			collectionFactories.put(rawType, factory);
		}
		return (Factory<T>) factory;
	}

	@SuppressWarnings("rawtypes")
	private boolean isEnumSet(Type type, Class<?> rawType) {
		if (rawType.equals(EnumSet.class)) {
			return true;
		}
		Type keyType = findElementType(type, 0);
		if (rawType.equals(Set.class) && keyType instanceof Class) {
			return ((Class) keyType).isEnum();
		}
		return false;
	}

	@SuppressWarnings("rawtypes")
	private boolean isEnumMap(Type type, Class<?> rawType) {
		if (rawType.equals(EnumMap.class)) {
			return true;
		}
		Type keyType = findElementType(type, 0);
		if (rawType.equals(Map.class) && keyType instanceof Class) {
			return ((Class) keyType).isEnum();
		}
		return false;
	}

	private static Map<Class<?>, Factory<?>> buildDefaultCollectionFactories() {
		Map<Class<?>, Factory<?>> collectionFactories = new ConcurrentHashMap<>();

		// Lists
		collectionFactories.put(List.class, ArrayList::new);

		// Maps
		collectionFactories.put(Map.class, HashMap::new);

		// Sets
		collectionFactories.put(Set.class, HashSet::new);

		// Other
		collectionFactories.put(Collection.class, ArrayList::new);
		collectionFactories.put(Queue.class, LinkedList::new);
		collectionFactories.put(Deque.class, LinkedList::new);
		return collectionFactories;
	}
}