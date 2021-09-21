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
package dev.nokee.runtime.darwin.internal;

import org.gradle.api.Action;
import org.gradle.api.attributes.*;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.util.Objects;

import static org.gradle.api.attributes.LibraryElements.HEADERS_CPLUSPLUS;

final class FrameworkElementAttributeSchema implements Action<AttributeMatchingStrategy<LibraryElements>> {
	private final ObjectFactory objects;

	FrameworkElementAttributeSchema(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void execute(AttributeMatchingStrategy<LibraryElements> strategy) {
		strategy.getCompatibilityRules().add(CompatibilityRules.class, it -> it.params(objects.named(LibraryElements.class, HEADERS_CPLUSPLUS), objects.named(LibraryElements.class, DarwinLibraryElements.FRAMEWORK_BUNDLE)));
	}

	static /*final*/ class CompatibilityRules implements AttributeCompatibilityRule<LibraryElements> {
		private final LibraryElements headersElement;
		private final LibraryElements frameworkElement;

		@Inject
		CompatibilityRules(LibraryElements headersElement, LibraryElements frameworkElement) {
			this.headersElement = headersElement;
			this.frameworkElement = frameworkElement;
		}

		@Override
		public void execute(CompatibilityCheckDetails<LibraryElements> details) {
			if (Objects.equals(details.getConsumerValue(), headersElement) && Objects.equals(details.getProducerValue(), frameworkElement)) {
				details.compatible();
			}
		}
	}

	static /*final*/ class SelectionRules implements AttributeDisambiguationRule<LibraryElements> {
		private final LibraryElements headersElement;
		private final LibraryElements frameworkElement;

		@Inject
		SelectionRules(LibraryElements headersElement, LibraryElements frameworkElement) {
			this.headersElement = headersElement;
			this.frameworkElement = frameworkElement;
		}

		@Override
		public void execute(MultipleCandidatesDetails<LibraryElements> details) {
			if (details.getConsumerValue() == null) {
				if (details.getCandidateValues().contains(headersElement)) {
					details.closestMatch(headersElement);
				} else if (details.getCandidateValues().contains(frameworkElement)) {
					details.closestMatch(frameworkElement);
				}
			} else if (details.getCandidateValues().contains(details.getConsumerValue())) {
				details.closestMatch(details.getConsumerValue());
			} else if (details.getConsumerValue().equals(headersElement) && details.getCandidateValues().contains(frameworkElement)) {
				details.closestMatch(frameworkElement);
			}
		}
	}
}
