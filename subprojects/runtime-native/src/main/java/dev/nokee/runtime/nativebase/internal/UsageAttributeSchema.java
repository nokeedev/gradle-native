package dev.nokee.runtime.nativebase.internal;

import com.google.common.collect.ImmutableSet;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.attributes.*;

final class UsageAttributeSchema implements Action<AttributeMatchingStrategy<Usage>> {
	@Override
	public void execute(AttributeMatchingStrategy<Usage> strategy) {
		strategy.getCompatibilityRules().add(CompatibilityRules.class);
		strategy.getDisambiguationRules().add(SelectionRules.class);
	}

	static /*final*/ class CompatibilityRules implements AttributeCompatibilityRule<Usage> {
		@Override
		public void execute(CompatibilityCheckDetails<Usage> details) {
			val producerValues = ImmutableSet.copyOf(details.getProducerValue().getName().split("\\+"));
			if (producerValues.contains(details.getConsumerValue().getName())) {
				details.compatible();
			}
		}
	}

	static /*final*/ class SelectionRules implements AttributeDisambiguationRule<Usage> {
		@Override
		public void execute(MultipleCandidatesDetails<Usage> details) {
			if (details.getCandidateValues().contains(details.getConsumerValue())) {
				details.closestMatch(details.getConsumerValue());
			}
		}
	}
}
