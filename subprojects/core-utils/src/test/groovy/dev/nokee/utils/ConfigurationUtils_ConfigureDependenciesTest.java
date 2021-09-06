package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import dev.gradleplugins.grava.testing.util.ProjectTestUtils;
import dev.nokee.internal.testing.ConfigurationMatchers;
import lombok.val;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.ConfigurationMatchers.forCoordinate;
import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.utils.ConfigurationUtils.add;
import static dev.nokee.utils.ConfigurationUtils.configureDependencies;
import static dev.nokee.utils.ConsumerTestUtils.*;
import static dev.nokee.utils.FunctionalInterfaceMatchers.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.hasToString;

class ConfigurationUtils_ConfigureDependenciesTest {
	@Test
	void callsBackWithConfiguration() {
		val subject = testConfiguration();
		val action = mockBiConsumer();
		configureDependencies(action).execute(subject);
		assertThat(action, calledOnceWith(firstArgumentOf(subject)));
	}

	@Test
	void callsBackWithConfigurationDependencySet() {
		val subject = testConfiguration();
		val action = mockBiConsumer();
		configureDependencies(action).execute(subject);
		assertThat(action, calledOnceWith(secondArgumentOf(subject.getDependencies())));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(configureDependencies(aBiConsumer()), configureDependencies(aBiConsumer()))
			.addEqualityGroup(configureDependencies(anotherBiConsumer()))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(configureDependencies(aBiConsumer()),
			hasToString("ConfigurationUtils.configureDependencies(aBiConsumer())"));
		assertThat(configureDependencies(anotherBiConsumer()),
			hasToString("ConfigurationUtils.configureDependencies(anotherBiConsumer())"));
	}

	@Nested
	class AddTest {
		@Test
		void canAddMappedDependency() {
			val subject = testConfiguration(
				configureDependencies(add(ignored -> ProjectTestUtils.createDependency("com.example:foo:4.2"))));
			assertThat(subject, ConfigurationMatchers.dependencies(contains(forCoordinate("com.example", "foo", "4.2"))));
		}
	}
}
