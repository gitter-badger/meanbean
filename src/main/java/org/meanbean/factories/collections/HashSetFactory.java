package org.meanbean.factories.collections;

import java.util.HashSet;
import java.util.Set;

import org.meanbean.factories.Factory;
import org.meanbean.util.RandomNumberGenerator;

/**
 * Factory that creates HashSets of objects of the specified type.
 * 
 * @param <T>
 *            The data type of the object this Factory creates Sets of.
 */
public class HashSetFactory<T> extends SetFactoryBase<T> {

    /** Unique version ID of this Serializable class. */
    private static final long serialVersionUID = 1L;
        
    /**
     * Construct a new HashSet object Factory.
     * 
     * @param randomNumberGenerator
     *            A random number generator used by the Factory to generate random values.
     * @param itemFactory
     *            Factory used to create each Set item.
     */
    public HashSetFactory(RandomNumberGenerator randomNumberGenerator,Factory<T> itemFactory) {
        super(randomNumberGenerator,itemFactory);
    }

	/**
	 * Create a new concrete Set instance that this Factory will return populated.
	 * 
	 * @return A concrete Set of the type created by this Factory.
	 */
    @Override
	protected Set<T> createSet() {
		return new HashSet<T>();
	}
    
}
