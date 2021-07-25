package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.utils.ConfigurationUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

class ConfigurationUtils_ConfigureAsResolvableTest {
	@Test
	void canConfigureConfigurationAsResolvableBucket()  {
		val configuration = testConfiguration(configureAsResolvable());
		assertThat("should not be consumable", configuration.isCanBeConsumed(), equalTo(false));
		assertThat("should be resolvable", configuration.isCanBeResolved(), equalTo(true));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(configureAsResolvable(), configureAsResolvable())
			.addEqualityGroup(configureAsDeclarable())
			.addEqualityGroup(configureAsConsumable())
			.addEqualityGroup((Action<Configuration>) it -> {})
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(configureAsResolvable(), hasToString("ConfigurationUtils.configureAsResolvable()"));
	}
}
