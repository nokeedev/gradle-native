package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.BuildType;
import dev.nokee.runtime.nativebase.TargetBuildType;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.runtime.base.internal.ProvideAttributes.attributesOf;
import static dev.nokee.utils.ConfigurationUtils.asConsumable;
import static dev.nokee.utils.ConfigurationUtils.asResolvable;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class DefaultTargetBuildTypeTest {
	TargetBuildType createSubject(String name) {
		return RuntimeNativePlugin.TARGET_BUILD_TYPE_FACTORY.named(name);
	}

	@Test
	void providesAttributeForConsumableConfiguration() {
		val project = rootProject();
		val test = project.getConfigurations().create("test",
			asConsumable().andThen(attributesOf(createSubject("my-build-type"))));
		assertThat(test, attributes(hasEntry(equalTo(BuildType.BUILD_TYPE_ATTRIBUTE), named("my-build-type"))));
	}

	@Test
	void providesAttributeForResolvableConfiguration() {
		val project = rootProject();
		val test = project.getConfigurations().create("test",
			asResolvable().andThen(attributesOf(createSubject("my-build-type"))));
		assertThat(test, attributes(hasEntry(equalTo(BuildType.BUILD_TYPE_ATTRIBUTE), named("my-build-type"))));
	}

	@Test
	void hasToStringName() {
		assertThat(createSubject("my-build-type"), hasToString("my-build-type"));
	}
}
