package org.meanbean.test;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.Assert.assertThat;

import org.junit.Test;
import org.meanbean.factories.FactoryCollection;
import org.meanbean.lang.Factory;
import org.meanbean.test.beans.Bean;
import org.meanbean.test.beans.BeanFactory;
import org.meanbean.test.beans.BeanWithBadGetterMethod;
import org.meanbean.test.beans.BeanWithBadSetterMethod;
import org.meanbean.test.beans.BeanWithNonBeanProperty;
import org.meanbean.test.beans.BrokenEqualsMultiPropertyBean;
import org.meanbean.test.beans.ComplexBeanFactory;
import org.meanbean.test.beans.CounterDrivenEqualsBeanFactory;
import org.meanbean.test.beans.FieldDrivenEqualsBean;
import org.meanbean.test.beans.FieldDrivenEqualsBeanFactory;
import org.meanbean.test.beans.IncrementalStringFactory;
import org.meanbean.test.beans.InvocationCountingFactoryWrapper;
import org.meanbean.test.beans.MultiPropertyBeanFactory;
import org.meanbean.test.beans.NonBean;
import org.meanbean.test.beans.NullFactory;
import org.meanbean.test.beans.SelfReferencingBeanFactory;

public class PropertyBasedEqualsMethodPropertySignificanceVerifierTest {

	private final PropertyBasedEqualsMethodPropertySignificanceVerifier verifier = new PropertyBasedEqualsMethodPropertySignificanceVerifier();

	private final BeanFactory beanFactory = new BeanFactory();

	@Test
	public void shouldGetFactoryRepository() throws Exception {
		FactoryCollection factoryRepository = verifier.getFactoryCollection();
		assertThat("Failed to get FactoryRepository.", factoryRepository, is(not(nullValue())));
		@SuppressWarnings("unchecked")
		Factory<String> stringFactory = (Factory<String>) factoryRepository.getFactory(String.class);
		String randomString = stringFactory.create();
		assertThat("Failed to get random String from FactoryRepository.", randomString, is(not(nullValue())));
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyEqualsMethodShouldPreventNullFactory() throws Exception {
		verifier.verifyEqualsMethod(null);
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyEqualsMethodShouldPreventFactoryThatCreatesNullObjects() throws Exception {
		verifier.verifyEqualsMethod(new NullFactory());
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyEqualsMethodShouldPreventNullInsignificantProperties() throws Exception {
		verifier.verifyEqualsMethod(new BeanFactory(), (String[]) null);
	}

	@Test
	public void verifyEqualsMethodShouldAcceptNoInsignificantProperties() throws Exception {
		verifier.verifyEqualsMethod(new BeanFactory());
	}

	@Test
	public void verifyEqualsMethodShouldAcceptEmptyInsignificantProperties() throws Exception {
		verifier.verifyEqualsMethod(new BeanFactory(), new String[] {});
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyEqualsMethodShouldPreventFactoryThatCreatesNonEqualObjects() throws Exception {
		verifier.verifyEqualsMethod(new Factory<FieldDrivenEqualsBean>() {
			private int counter;

			@Override
			public FieldDrivenEqualsBean create() {
				// 2nd object created by factory always returns false from equals(); others always return true
				return new FieldDrivenEqualsBean(counter++ != 1);
			}
		});
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyEqualsMethodShouldPreventFactoryThatCreatesObjectsWithDifferentPropertyValues() throws Exception {
		verifier.verifyEqualsMethod(new Factory<FieldDrivenEqualsBean>() {
			private int counter;

			@Override
			public FieldDrivenEqualsBean create() {
				FieldDrivenEqualsBean bean = new FieldDrivenEqualsBean(true);// equal to everything
				bean.setName("NAME" + counter++);// property has different value each time
				return bean;
			}
		});
	}

	@Test(expected = BeanTestException.class)
	public void verifyEqualsMethodShouldWrapExceptionsThrownWhenInvokingSetterMethodInBeanTestException()
	        throws Exception {
		verifier.verifyEqualsMethod(new Factory<BeanWithBadSetterMethod>() {
			@Override
			public BeanWithBadSetterMethod create() {
				return new BeanWithBadSetterMethod();
			}
		});
	}

	@Test(expected = BeanTestException.class)
	public void verifyEqualsMethodShouldWrapExceptionsThrownWhenInvokingGetterMethodInBeanTestException()
	        throws Exception {
		verifier.verifyEqualsMethod(new Factory<BeanWithBadGetterMethod>() {
			@Override
			public BeanWithBadGetterMethod create() {
				return new BeanWithBadGetterMethod();
			}
		});
	}

	@Test(expected = IllegalArgumentException.class)
	public void verifyEqualsMethodShouldPreventNullPropertyValues() throws Exception {
		verifier.verifyEqualsMethod(new Factory<Bean>() {
			@Override
			public Bean create() {
				return new Bean(); // null name property
			}
		});
	}

	@Test(expected = BeanTestException.class)
	public void verifyEqualsMethodShouldWrapNoSuchFactoryExceptionInBeanTestException() throws Exception {
		verifier.verifyEqualsMethod(new Factory<Object>() {
			@Override
			public Object create() {
				BeanWithNonBeanProperty bean = new BeanWithNonBeanProperty();
				bean.setName("TEST_VALUE");
				bean.setNonBean(new NonBean("ANOTHER_TEST_VALUE"));
				return bean;
			}
		});
	}

	@Test(expected = AssertionError.class)
	public void verifyEqualsMethodShouldThrowAssertionErrorWhenEqualityShouldNotHaveChangedButDid() throws Exception {
		verifier.verifyEqualsMethod(new MultiPropertyBeanFactory(), "lastName");
	}

	@Test(expected = AssertionError.class)
	public void verifyEqualsMethodShouldThrowAssertionErrorWhenEqualityShouldHaveChangedButDidNot() throws Exception {
		verifier.verifyEqualsMethod(new Factory<BrokenEqualsMultiPropertyBean>() {
			@Override
			public BrokenEqualsMultiPropertyBean create() {
				BrokenEqualsMultiPropertyBean bean = new BrokenEqualsMultiPropertyBean();
				bean.setFirstName("FIRST_NAME");
				bean.setLastName("LAST_NAME");
				return bean;
			}
		});
	}

	@Test
	public void verifyEqualsMethodShouldNotThrowAssertionErrorWhenTestPassesWithMultiPropertyBean() throws Exception {
		verifier.verifyEqualsMethod(new MultiPropertyBeanFactory());
	}

	@Test
	public void verifyEqualsMethodShouldNotThrowAssertionErrorWhenTestPassesWithSelfReferencingBean() throws Exception {
		verifier.verifyEqualsMethod(new SelfReferencingBeanFactory());
	}

	@Test
	public void verifyEqualsMethodShouldNotThrowAssertionErrorWhenTestPassesWithComplexBean() throws Exception {
		// This tests whether the verifier can cope with non-readable/non-writable properties, etc.
		verifier.verifyEqualsMethod(new ComplexBeanFactory());
	}

	@Test
	public void verifyEqualsMethodShouldIgnoreProperties() throws Exception {
		Configuration configuration = new ConfigurationBuilder().ignoreProperty("lastName").build();
		verifier.verifyEqualsMethod(new MultiPropertyBeanFactory(), configuration, "lastName");
	}

	@Test
	public void verifyEqualsMethodShouldUseOverrideFactory() throws Exception {
		@SuppressWarnings("unchecked")
		Factory<String> stringFactory = (Factory<String>) verifier.getFactoryCollection().getFactory(String.class);
		InvocationCountingFactoryWrapper<String> factory = new InvocationCountingFactoryWrapper<String>(stringFactory);
		Configuration configuration = new ConfigurationBuilder().overrideFactory("name", factory).build();
		verifier.verifyEqualsMethod(new BeanFactory(), configuration);
		assertThat("custom factory was not used", factory.getInvocationCount(), is(1));
	}

	// @Test(expected = AssertionError.class)
	// public void verifyEqualsMethodShouldUseOverrideFactory() throws Exception {
	// final String lastName = "MY_SPECIAL_TEST_STRING";
	// Configuration configuration = new ConfigurationBuilder().overrideFactory("lastName", new Factory<String>() {
	// @Override
	// public String create() {
	// return lastName;
	// }
	// }).build();
	// verifier.verifyEqualsMethod(new Factory<MultiPropertyBean>() {
	// @Override
	// public MultiPropertyBean create() {
	// MultiPropertyBean bean = new MultiPropertyBean();
	// bean.setFirstName("FIRST_NAME");
	// bean.setLastName(lastName);
	// return bean;
	// }
	// }, configuration);
	// }

	@Test(expected = AssertionError.class)
	public void verifyEqualsShouldThrowAssertionErrorWhenValuesDifferButObjectsStillEqualForSignificantProperty()
	        throws Exception {
		verifier.verifyEqualsMethod(new FieldDrivenEqualsBeanFactory(true),
		        new ConfigurationBuilder().overrideFactory("name", new IncrementalStringFactory()).build());
	}

	@Test
	public void verifyEqualsShouldNotThrowAssertionErrorWhenValuesDifferAndObjectsNotEqualForSignificantProperty()
	        throws Exception {
		verifier.verifyEqualsMethod(new BeanFactory(),
		        new ConfigurationBuilder().overrideFactory("name", new IncrementalStringFactory()).build());
	}

	@Test(expected = AssertionError.class)
	public void verifyEqualsShouldThrowAssertionErrorWhenValuesSameButObjectsNotEqualForSignificantProperty()
	        throws Exception {
		Configuration configuration = new ConfigurationBuilder().overrideFactory("name", new Factory<String>() {
			@Override
			public String create() {
				return CounterDrivenEqualsBeanFactory.NAME;
			}
		}).build();
		verifier.verifyEqualsMethod(new CounterDrivenEqualsBeanFactory(1), configuration);
	}

	@Test
	public void verifyEqualsShouldNotThrowAssertionErrorWhenValuesSameAndObjectsEqualForSignificantProperty()
	        throws Exception {
		Configuration configuration = new ConfigurationBuilder().overrideFactory("name", new Factory<String>() {
			@Override
			public String create() {
				return BeanFactory.NAME;
			}
		}).build();
		verifier.verifyEqualsMethod(new BeanFactory(), configuration);
	}

	@Test
	public void verifyEqualsShouldNotThrowAssertionErrorWhenValuesDifferAndObjectsEqualForInsignificantProperty()
	        throws Exception {
		verifier.verifyEqualsMethod(new FieldDrivenEqualsBeanFactory(true),
		        new ConfigurationBuilder().overrideFactory("name", new IncrementalStringFactory()).build(), "name");
	}

	@Test(expected = AssertionError.class)
	public void verifyEqualsShouldThrowAssertionErrorWhenValuesDifferAndObjectsNotEqualForInsignificantProperty()
	        throws Exception {
		verifier.verifyEqualsMethod(new BeanFactory(),
		        new ConfigurationBuilder().overrideFactory("name", new IncrementalStringFactory()).build(), "name");
	}

	@Test(expected = AssertionError.class)
	public void verifyEqualsShouldThrowAssertionErrorWhenValuesSameButObjectsNotEqualForInsignificantProperty()
	        throws Exception {
		Configuration configuration = new ConfigurationBuilder().overrideFactory("name", new Factory<String>() {
			@Override
			public String create() {
				return CounterDrivenEqualsBeanFactory.NAME;
			}
		}).build();
		verifier.verifyEqualsMethod(new CounterDrivenEqualsBeanFactory(1), configuration, "name");
	}

	@Test
	public void verifyEqualsShouldNotThrowAssertionErrorWhenValuesSameAndObjectsEqualForInsignificantProperty()
	        throws Exception {
		Configuration configuration = new ConfigurationBuilder().overrideFactory("name", new Factory<String>() {
			@Override
			public String create() {
				return BeanFactory.NAME;
			}
		}).build();
		verifier.verifyEqualsMethod(new BeanFactory(), configuration);
	}
}