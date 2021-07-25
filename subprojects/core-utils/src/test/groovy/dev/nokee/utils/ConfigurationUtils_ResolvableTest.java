package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.specs.Spec;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.utils.ConfigurationTestUtils.testConfiguration;
import static dev.nokee.utils.ConfigurationUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.assertAll;

class ConfigurationUtils_ResolvableTest {
	@Test
	void onlyMatchResolvableBucket() {
		assertAll(
			() -> assertThat(resolvable().isSatisfiedBy(testConfiguration(configureAsConsumable())), is(false)),
			() -> assertThat(resolvable().isSatisfiedBy(testConfiguration(configureAsResolvable())), is(true)),
			() -> assertThat(resolvable().isSatisfiedBy(testConfiguration(configureAsDeclarable())), is(false))
		);
	}

	@Test
	void returnsEnhancedSpec() {
		assertThat(resolvable(), isA(SpecUtils.Spec.class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(resolvable(), resolvable())
			.addEqualityGroup(consumable())
			.addEqualityGroup(declarable())
			.addEqualityGroup((Spec<Configuration>) it -> true)
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(resolvable(), hasToString("ConfigurationUtils.resolvable()"));
	}
}
