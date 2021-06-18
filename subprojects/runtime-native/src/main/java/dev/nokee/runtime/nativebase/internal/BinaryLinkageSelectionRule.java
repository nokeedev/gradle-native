package dev.nokee.runtime.nativebase.internal;

import dev.nokee.runtime.nativebase.BinaryLinkage;
import org.gradle.api.attributes.AttributeDisambiguationRule;
import org.gradle.api.attributes.MultipleCandidatesDetails;

import javax.inject.Inject;

/*final*/ abstract class BinaryLinkageSelectionRule implements AttributeDisambiguationRule<BinaryLinkage> {
	private static final BinaryLinkage SHARED = BinaryLinkage.named(BinaryLinkage.SHARED);
	@Inject
	public BinaryLinkageSelectionRule() {}

	@Override
	public void execute(MultipleCandidatesDetails<BinaryLinkage> details) {
		if (details.getCandidateValues().contains(SHARED)) {
			details.closestMatch(SHARED);
		}
	}
}
