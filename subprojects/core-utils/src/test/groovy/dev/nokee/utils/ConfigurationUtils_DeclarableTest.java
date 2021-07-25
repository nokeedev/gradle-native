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

class ConfigurationUtils_DeclarableTest {
	@Test
	void onlyMatchDeclarableBucket() {
		assertAll(
			() -> assertThat(declarable().isSatisfiedBy(testConfiguration(configureAsConsumable())), is(false)),
			() -> assertThat(declarable().isSatisfiedBy(testConfiguration(configureAsResolvable())), is(false)),
			() -> assertThat(declarable().isSatisfiedBy(testConfiguration(configureAsDeclarable())), is(true))
		);
	}

	@Test
	void returnsEnhancedSpec() {
		assertThat(declarable(), isA(SpecUtils.Spec.class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(declarable(), declarable())
			.addEqualityGroup(resolvable())
			.addEqualityGroup(consumable())
			.addEqualityGroup((Spec<Configuration>) it -> true)
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(declarable(), hasToString("ConfigurationUtils.declarable()"));
	}
}
