package dev.nokee.platform.base.internal.dependencies;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import java.util.function.Consumer;

import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Subject(ProjectConfigurationActions.class)
class ProjectConfigurationActions_AsConsumableTest {
	@Test
	void canConfigureConfigurationAsConsumableBucket()  {
		val configuration = testConfiguration(asConsumable());
		assertThat("should be consumable", configuration.isCanBeConsumed(), equalTo(true));
		assertThat("should not be resolvable", configuration.isCanBeResolved(), equalTo(false));
	}

	@Test
	void canCheckWhenConfigurationIsNotConsumable() {
		val ex = assertThrows(IllegalStateException.class,
			() -> assertConfigured(testConfiguration(), asConsumable()));
		assertThat(ex.getMessage(), equalTo("Cannot reuse existing configuration named 'test' as a consumable configuration because it does not match the expected configuration (expecting: [canBeConsumed: true, canBeResolved: false], actual: [canBeConsumed: true, canBeResolved: true])."));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(asConsumable(), asConsumable())
			.addEqualityGroup(asResolvable())
			.addEqualityGroup(asDeclarable())
			.addEqualityGroup((Consumer<Configuration>) it -> {})
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(asConsumable(), hasToString("ProjectConfigurationUtils.asConsumable()"));
	}
}
