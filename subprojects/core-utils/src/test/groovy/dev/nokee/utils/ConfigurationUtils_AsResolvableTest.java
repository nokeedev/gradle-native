package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.utils.Assertions.assertConfigured;
import static dev.nokee.utils.ConfigurationUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ConfigurationUtils_AsResolvableTest {
	@Test
	void canConfigureConfigurationAsResolvableBucket()  {
		val configuration = testConfiguration(asResolvable());
		assertThat("should not be consumable", configuration.isCanBeConsumed(), equalTo(false));
		assertThat("should be resolvable", configuration.isCanBeResolved(), equalTo(true));
	}

	@Test
	void canCheckWhenConfigurationIsNotResolvable() {
		val ex = assertThrows(IllegalStateException.class,
			() -> assertConfigured(testConfiguration(), asResolvable()));
		assertThat(ex.getMessage(), equalTo("Cannot reuse existing configuration named 'test' as a resolvable configuration because it does not match the expected configuration (expecting: [canBeConsumed: false, canBeResolved: true], actual: [canBeConsumed: true, canBeResolved: true])."));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(asResolvable(), asResolvable())
			.addEqualityGroup(asDeclarable())
			.addEqualityGroup(asConsumable())
			.addEqualityGroup((Action<Configuration>) it -> {})
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(asResolvable(), hasToString("ConfigurationUtils.asResolvable()"));
	}
}
