/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
