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

class ConfigurationUtils_AsConsumableTest {
	@Test
	void canConfigureConfigurationAsConsumableBucket()  {
		val configuration = testConfiguration(asConsumable());
		assertThat("should be consumable", configuration.isCanBeConsumed(), equalTo(true));
		assertThat("should not be resolvable", configuration.isCanBeResolved(), equalTo(false));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(asConsumable(), asConsumable())
			.addEqualityGroup(asResolvable())
			.addEqualityGroup(asDeclarable())
			.addEqualityGroup((Action<Configuration>) it -> {})
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(asConsumable(), hasToString("ConfigurationUtils.asConsumable()"));
	}
}
