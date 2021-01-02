package dev.nokee.platform.base.internal.dependencies;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationUtils.assertConfigured;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationUtils.description;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

@Subject(ProjectConfigurationUtils.class)
class ProjectConfigurationUtils_DescriptionTest {
	@Test
	void canConfigureDescriptionProvidedByRawString() {
		assertThat(testConfiguration(description("foo")).getDescription(), equalTo("foo"));
	}

	@Test
	void canConfigureDescriptionProvidedBySupplier() {
		assertThat(testConfiguration(description(() -> "bar")).getDescription(), equalTo("bar"));
	}

	@Test
	void ignoresDescriptionMismatch() {
		assertDoesNotThrow(() -> assertConfigured(testConfiguration(description("some description")),
			description("some other description")));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(description("foo"), description("foo"))
			.addEqualityGroup(description(() -> "bar"))
			.addEqualityGroup(description(() -> "far"))
			.addEqualityGroup(description("tar"))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(description("foo"), hasToString("ProjectConfigurationUtils.description(Suppliers.ofInstance(foo))"));
	}
}
