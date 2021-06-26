package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.base.DisambiguationRulesTester;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import org.junit.jupiter.api.Test;

class BinaryLinkageAttributeSchema_SelectionRulesTest {
	private final DisambiguationRulesTester<BinaryLinkage> tester = DisambiguationRulesTester.of(new BinaryLinkageAttributeSchema.SelectionRules());

	@Test
	void alwaysPreferSharedBinaryLinkageWhenAvailable() {
		tester.whenConsuming(null).fromCandidates(BinaryLinkage.STATIC, BinaryLinkage.SHARED).assertClosestMatch(BinaryLinkage.SHARED);
	}

	@Test
	void noPreferredBinaryLinkageWhenSharedBinaryLinkageIsUnavailable() {
		tester.whenConsuming(null).fromCandidates(BinaryLinkage.STATIC, "some-other-linkage").assertNoMatch();
	}
}
