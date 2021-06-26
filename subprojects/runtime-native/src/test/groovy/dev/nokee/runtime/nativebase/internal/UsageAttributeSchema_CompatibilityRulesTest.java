package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.base.CompatibilityRulesTester;
import org.gradle.api.attributes.Usage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

class UsageAttributeSchema_CompatibilityRulesTest {
	private final CompatibilityRulesTester<Usage> tester = CompatibilityRulesTester.of(new UsageAttributeSchema.CompatibilityRules());

	@ParameterizedTest
	@ValueSource(strings = {Usage.C_PLUS_PLUS_API, Usage.NATIVE_LINK})
	void markCompatibleWhenConsumerUsageIsPartOfCompoundProducerUsage(String usage) {
		tester.whenConsuming(usage).fromProducer(Usage.C_PLUS_PLUS_API + "+" + Usage.NATIVE_LINK).assertMarkedAsCompatible();
	}

	@Test
	void doNothingWhenConsumerUsageIsSubstringOfCompoundProducerUsage() {
		tester.whenConsuming("api").fromProducer(Usage.C_PLUS_PLUS_API + "+" + Usage.NATIVE_LINK).assertDidNothing();
	}

	@Test
	void doNothingWhenConsumerUsageIsNotPartOfCompoundProducerUsage() {
		tester.whenConsuming(Usage.NATIVE_RUNTIME).fromProducer(Usage.C_PLUS_PLUS_API + "+" + Usage.NATIVE_LINK).assertDidNothing();
	}
}
