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
