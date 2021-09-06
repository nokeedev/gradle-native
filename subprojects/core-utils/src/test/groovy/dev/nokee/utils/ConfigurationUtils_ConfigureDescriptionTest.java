package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;

import static com.google.common.base.Suppliers.ofInstance;
import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.providerFactory;
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
	void canUseProviderAsFormatArguments() {
		assertThat(testConfiguration(configureDescription("Value '%s'", providerFactory().provider(() -> "foat"))).getDescription(),
			equalTo("Value 'foat'"));
	}

	@Test
	void canUseCallableAsFormatArguments() {
		assertThat(testConfiguration(configureDescription("Value '%s'", (Callable<String>) () -> "quot")).getDescription(),
			equalTo("Value 'quot'"));
	}

	@Test
	void doesNotProvideAdditionalArgumentsWhenUnpacking() {
		assertThat(testConfiguration(configureDescription("Value '%s'", (Callable<List<String>>) () -> Arrays.asList("aaaa", "bbbb"))).getDescription(),
			equalTo("Value '[aaaa, bbbb]'"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void canCompareConfigurationAction() {
		new EqualsTester()
			.addEqualityGroup(configureDescription("Dependencies for '%s'.", "foo"), configureDescription("Dependencies for '%s'.", "foo"))
			.addEqualityGroup(configureDescription(ofInstance("description")), configureDescription(ofInstance("description")))
			.addEqualityGroup(configureDescription(ofInstance("another description")))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(configureDescription(ofInstance("description")),
			hasToString("ConfigurationUtils.configureDescription(description)"));
		assertThat(configureDescription("Dependencies for '%s'.", "foo"),
			hasToString("ConfigurationUtils.configureDescription(Dependencies for 'foo'.)"));
	}
}
