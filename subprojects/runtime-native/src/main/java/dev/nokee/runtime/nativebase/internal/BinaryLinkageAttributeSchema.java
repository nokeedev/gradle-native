package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.BinaryLinkage;
import org.gradle.api.Action;
import org.gradle.api.attributes.AttributeDisambiguationRule;
import org.gradle.api.attributes.AttributeMatchingStrategy;
import org.gradle.api.attributes.MultipleCandidatesDetails;

final class BinaryLinkageAttributeSchema implements Action<AttributeMatchingStrategy<BinaryLinkage>> {
	@Override
	public void execute(AttributeMatchingStrategy<BinaryLinkage> strategy) {
		strategy.getDisambiguationRules().add(SelectionRules.class);
	}

	static /*final*/ class SelectionRules implements AttributeDisambiguationRule<BinaryLinkage> {
		private static final BinaryLinkage SHARED = BinaryLinkage.named(BinaryLinkage.SHARED);

		@Override
		public void execute(MultipleCandidatesDetails<BinaryLinkage> details) {
			if (details.getCandidateValues().contains(SHARED)) {
				details.closestMatch(SHARED);
			}
		}
	}
}
