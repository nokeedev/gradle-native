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

import dev.nokee.runtime.nativebase.BuildType;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.attributes.*;

import java.util.Locale;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Predicate;
import java.util.function.Supplier;

final class BuildTypeAttributeSchema implements Action<AttributeMatchingStrategy<BuildType>> {
	@Override
	public void execute(AttributeMatchingStrategy<BuildType> strategy) {
		strategy.getCompatibilityRules().add(CompatibilityRules.class);
		strategy.getDisambiguationRules().add(SelectionRules.class);
	}

	static /*final*/ class CompatibilityRules implements AttributeCompatibilityRule<BuildType> {
		@Override
		public void execute(CompatibilityCheckDetails<BuildType> details) {
			details.compatible(); // Just mark everything as compatible and let the selection rule take over
		}
	}

	static /*final*/ class SelectionRules implements AttributeDisambiguationRule<BuildType> {
		@Override
		public void execute(MultipleCandidatesDetails<BuildType> details) {
			if (details.getConsumerValue() == null) {
				// Choose something that is close to "debug"
				findFirstMatchingDebugishBuildType(details.getCandidateValues()).ifPresent(details::closestMatch);
			} else if (details.getCandidateValues().contains(details.getConsumerValue())) {
				// Select the exact matching value, if available.
				details.closestMatch(details.getConsumerValue());
			} else if (details.getCandidateValues().size() == 1) {
				// If only one candidate, let's assume it's a match.
				// In theory build type should just dictate debuggability and optimizability which should be interchangeable.
				details.closestMatch(details.getCandidateValues().iterator().next());
			} else {
				// Choose something that is similar to consumer value
				or(findFirstMatchingSameBuildType(details),
					() -> findFirstMatchingDebugishBuildType(details.getCandidateValues())).ifPresent(details::closestMatch);
			}
		}

		// TODO: Move to JDK9+ shim
		private static <T> Optional<T> or(Optional<T> self, Supplier<? extends Optional<? extends T>> supplier) {
			Objects.requireNonNull(supplier);
			if (self.isPresent()) {
				return self;
			} else {
				@SuppressWarnings("unchecked")
				Optional<T> r = (Optional<T>) supplier.get();
				return Objects.requireNonNull(r);
			}
		}

		//region Debug-ish build type
		private static Optional<BuildType> findFirstMatchingDebugishBuildType(Set<BuildType> candidateValues) {
			return candidateValues.stream().filter(isDebugish()).findFirst();
		}

		private static Predicate<BuildType> isDebugish() {
			return buildType -> buildType.getName().toLowerCase(Locale.CANADA).contains("debug");
		}
		//endregion

		//region Same build type
		private static Optional<BuildType> findFirstMatchingSameBuildType(MultipleCandidatesDetails<BuildType> details) {
			return details.getCandidateValues().stream().filter(isSame(details.getConsumerValue())).findFirst();
		}

		private static Predicate<BuildType> isSame(BuildType lhs) {
			val lowerCasedName = lhs.getName().toLowerCase(Locale.CANADA);
			return rhs -> rhs.getName().toLowerCase(Locale.CANADA).equals(lowerCasedName);
		}
		//endregion
	}
}
