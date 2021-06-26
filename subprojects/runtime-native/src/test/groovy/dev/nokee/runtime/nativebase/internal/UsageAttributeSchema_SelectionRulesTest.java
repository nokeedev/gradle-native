package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.base.DisambiguationRulesTester;
import org.gradle.api.attributes.Usage;
import org.junit.jupiter.api.Test;

class UsageAttributeSchema_SelectionRulesTest {
	private final DisambiguationRulesTester<Usage> tester = DisambiguationRulesTester.of(new UsageAttributeSchema.SelectionRules());

	@Test
	void selectsExactMatchingWhenAvailable() {
		tester.whenConsuming(Usage.C_PLUS_PLUS_API)
			.fromCandidates(Usage.C_PLUS_PLUS_API, Usage.C_PLUS_PLUS_API + "+" + Usage.NATIVE_LINK)
			.assertClosestMatch(Usage.C_PLUS_PLUS_API);
	}
}
