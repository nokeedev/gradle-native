package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static com.google.common.base.Suppliers.ofInstance;
import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.utils.ConfigurationUtils.configureDescription;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;

class ConfigurationUtils_ConfigureDescriptionTest {
	@Test
	void canConfigureConfigurationDescription() {
		assertThat(testConfiguration(configureDescription(() -> "some description")).getDescription(),
			equalTo("some description"));
	}

	@Test
	void canConfigureConfigurationDescriptionUsingFormatMessage() {
		assertThat(testConfiguration(configureDescription("Value '%s'", "test")).getDescription(),
			equalTo("Value 'test'"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void canCompareConfigurationAction() {
		new EqualsTester()
			.addEqualityGroup(configureDescription(ofInstance("description")), configureDescription(ofInstance("description")))
			.addEqualityGroup(configureDescription(ofInstance("another description")))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(configureDescription(ofInstance("description")),
			hasToString("ConfigurationUtils.configureDescription(Suppliers.ofInstance(description))"));
	}
}
