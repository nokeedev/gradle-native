package dev.nokee.platform.base.internal.dependencies;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.apache.commons.lang3.mutable.MutableObject;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.model.ObjectFactory;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.function.BiConsumer;

import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.internal.testing.utils.TestUtils.objectFactory;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.using;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.withObjectFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

@Subject(ProjectConfigurationActions.class)
class ProjectConfigurationActions_UsingObjectFactoryTest {
	@Test
	void canUseObjectFactoryInDelegateConfigurationAction() {
		val capturedObjectFactory = new MutableObject<ObjectFactory>();
		testConfiguration(using(objectFactory(), withObjectFactory((configuration, objectFactory) -> capturedObjectFactory.setValue(objectFactory))));
		assertThat(capturedObjectFactory.getValue(), equalTo(objectFactory()));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEqualsOfWithObjectFactory() {
		BiConsumer<Configuration, ObjectFactory> doSomething = doSomething();
		BiConsumer<Configuration, ObjectFactory> doSomethingElse = (a, b) -> {};
		new EqualsTester()
			.addEqualityGroup(withObjectFactory(doSomething), withObjectFactory(doSomething))
			.addEqualityGroup(withObjectFactory(doSomethingElse))
			.testEquals();
	}

	@Test
	void checkToStringOfWithObjectFactory() {
		assertThat(withObjectFactory(doSomething()),
			hasToString("ProjectConfigurationUtils.withObjectFactory(doSomething())"));
	}

	private static BiConsumer<Configuration, ObjectFactory> doSomething() {
		return new BiConsumer<Configuration, ObjectFactory>() {
			@Override
			public void accept(Configuration configuration, ObjectFactory objectFactory) {}

			@Override
			public String toString() {
				return "doSomething()";
			}
		};
	}
}
