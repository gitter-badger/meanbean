package org.meanbean.factories;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.sameInstance;

import org.junit.Test;
import org.meanbean.test.beans.Bean;
import org.meanbean.test.beans.NonBean;
import org.meanbean.test.beans.PrivateConstructorObject;

public class BasicNewObjectInstanceFactoryTest {

	@Test(expected = IllegalArgumentException.class)
	public void constructorShouldPreventNullRandomNumberGenerator() throws Exception {
		new BasicNewObjectInstanceFactory(null);
	}

	@Test
	public void createShouldReturnNewObjectEachInvocation() throws Exception {
		BasicNewObjectInstanceFactory factory = new BasicNewObjectInstanceFactory(Bean.class);
		Object createdObject1 = factory.create();
		Object createdObject2 = factory.create();
		assertThat("Factory does not create new objects.", createdObject1, is(not(sameInstance(createdObject2))));
	}

	@Test(expected = ObjectCreationException.class)
	public void createWillThrowObjectCreationExceptionWhenClassDoesNotHaveNoArgConstructor() throws Exception {
		new BasicNewObjectInstanceFactory(NonBean.class).create();
	}

	@Test(expected = ObjectCreationException.class)
	public void createWillThrowObjectCreationExceptionWhenClassHasPrivateConstructor() throws Exception {
		new BasicNewObjectInstanceFactory(PrivateConstructorObject.class).create();
	}

}