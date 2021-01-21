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
class ProjectConfigurationActions_AsDeclarableTest {
	@Test
	void canConfigureConfigurationAsDeclarableBucket()  {
		val configuration = testConfiguration(asDeclarable());
		assertThat("should not be consumable", configuration.isCanBeConsumed(), equalTo(false));
		assertThat("should not be resolvable", configuration.isCanBeResolved(), equalTo(false));
	}

	@Test
	void canCheckWhenConfigurationIsNotDeclarable() {
		val ex = assertThrows(IllegalStateException.class,
			() -> assertConfigured(testConfiguration(), asDeclarable()));
		assertThat(ex.getMessage(), equalTo("Cannot reuse existing configuration named 'test' as a declarable configuration because it does not match the expected configuration (expecting: [canBeConsumed: false, canBeResolved: false], actual: [canBeConsumed: true, canBeResolved: true])."));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(asDeclarable(), asDeclarable())
			.addEqualityGroup(asResolvable())
			.addEqualityGroup(asConsumable())
			.addEqualityGroup((Consumer<Configuration>) it -> {})
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(asDeclarable(), hasToString("ProjectConfigurationUtils.asDeclarable()"));
	}
}
