package org.meanbean.factories.collections;

import java.util.IdentityHashMap;
import java.util.Map;

import org.meanbean.factories.Factory;
import org.meanbean.util.RandomNumberGenerator;

public class IdentityHashMapFactoryTest extends MapFactoryTestBase {

	@Override
	protected MapFactoryBase<String, Long> getMapFactory(RandomNumberGenerator randomNumberGenerator, Factory<String> keyFactory, Factory<Long> valueFactory) {
		return new IdentityHashMapFactory<String, Long>(randomNumberGenerator, keyFactory, valueFactory);
	}

	@Override
    protected Map<String, Long> getMapOfExpectedType() {
	    return new IdentityHashMap<String, Long>();
    }
}