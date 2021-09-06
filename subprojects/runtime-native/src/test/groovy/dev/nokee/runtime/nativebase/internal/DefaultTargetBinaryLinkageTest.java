package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.TargetLinkage;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static dev.nokee.internal.testing.ConfigurationMatchers.attributes;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.utils.ConfigurationUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class DefaultTargetBinaryLinkageTest {
	TargetLinkage createSubject(String name) {
		return new DefaultTargetLinkage(BinaryLinkage.named(name));
	}

	@Test
	void doesNotProvideAttributeForConsumableConfiguration() {
		val project = rootProject();
		val test = project.getConfigurations().create("test",
			configureAsConsumable().andThen(configureAttributes(attributesOf(createSubject("my-linkage")))));
		assertThat(test, attributes(not(hasEntry(equalTo(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE), named("my-linkage")))));
	}

	@Test
	void providesAttributeForResolvableConfiguration() {
		val project = rootProject();
		val test = project.getConfigurations().create("test",
			configureAsResolvable().andThen(configureAttributes(attributesOf(createSubject("my-linkage")))));
		assertThat(test, attributes(hasEntry(equalTo(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE), named("my-linkage"))));
	}

	@Test
	void hasToStringName() {
		assertThat(createSubject("my-linkage"), hasToString("my-linkage"));
	}
}
