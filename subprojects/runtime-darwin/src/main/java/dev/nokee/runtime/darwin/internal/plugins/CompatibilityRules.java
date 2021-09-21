/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.runtime.darwin.internal.plugins;

import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.attributes.*;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.language.cpp.CppBinary;
import org.gradle.nativeplatform.Linkage;

import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static dev.nokee.runtime.darwin.internal.DarwinLibraryElements.FRAMEWORK_BUNDLE;
import static org.gradle.api.attributes.LibraryElements.DYNAMIC_LIB;

public class CompatibilityRules implements Plugin<Project> {
	private static final Logger LOGGER = Logging.getLogger(CompatibilityRules.class);
	@Override
	public void apply(Project project) {
//		project.getDependencies().attributesSchema(schema -> {
//			schema.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE);
//			schema.attribute(ARTIFACT_TYPES_ATTRIBUTE);
//			schema.attribute(Usage.USAGE_ATTRIBUTE);
//			schema.attribute(CppBinary.LINKAGE_ATTRIBUTE);
//		});

		project.getDependencies().attributesSchema(schema -> {
			schema.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, matchingStrategy -> {
				matchingStrategy.getCompatibilityRules().add(LibraryElementCompatibilityRules.class);
				matchingStrategy.getDisambiguationRules().add(LibraryElementDisambiguationRules.class);
			});
			schema.attribute(CppBinary.LINKAGE_ATTRIBUTE, matchingStrategy -> {
				matchingStrategy.getDisambiguationRules().pickFirst(new Comparator<Linkage>() {
					@Override
					public int compare(Linkage lhs, Linkage rhs) {
						LOGGER.debug("Disambiguation for linkage (" + lhs.getName() + " =?= " + rhs.getName() + ")");
						if (lhs.equals(Linkage.SHARED) && rhs.equals(Linkage.SHARED)) {
							return 0;
						}
						if (lhs.equals(Linkage.SHARED)) {
							return -1;
						}
						if (rhs.equals(Linkage.SHARED)) {
							return 1;
						}
						return 0;
					}
				});
			});
			schema.attribute(CppBinary.OPTIMIZED_ATTRIBUTE, matchingStrategy -> {
				matchingStrategy.getDisambiguationRules().add(OptimizationSelectionRule.class);
			});
		});
	}

	public static class OptimizationSelectionRule implements AttributeDisambiguationRule<Boolean> {
		@Override
		public void execute(MultipleCandidatesDetails<Boolean> details) {
			LOGGER.debug("Disambiguating 'org.gradle.native.optimized' requesting '" + details.getConsumerValue() + "' for values '" + details.getCandidateValues().stream().map(Object::toString).collect(Collectors.joining(", ")) + "'");
			if (details.getCandidateValues().contains(Boolean.FALSE)) {
				details.closestMatch(Boolean.FALSE);
			}
		}
	}

	public static class LibraryElementCompatibilityRules implements AttributeCompatibilityRule<LibraryElements> {
		@Override
		public void execute(CompatibilityCheckDetails<LibraryElements> details) {
			if (details.getProducerValue().getName().startsWith("something-to-")) {
				details.incompatible();
			} else {
				details.compatible();
			}
		}
	}
	public static class LibraryElementDisambiguationRules implements AttributeDisambiguationRule<LibraryElements> {
		@Override
		public void execute(MultipleCandidatesDetails<LibraryElements> details) {
			Map<String, LibraryElements> candidates = details.getCandidateValues().stream().map(it -> new HashMap.SimpleEntry<>(it.getName(), it)).collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
			if (details.getConsumerValue() == null) {
				LOGGER.debug("no consumer value");
			}
			if (candidates.containsKey(FRAMEWORK_BUNDLE)) {
				LOGGER.debug("Pick framework " + candidates.get(FRAMEWORK_BUNDLE).getName());
				details.closestMatch(candidates.get(FRAMEWORK_BUNDLE));
			} else if (candidates.containsKey(DYNAMIC_LIB)) {
				LOGGER.debug("Pick dynamic library " + candidates.get(DYNAMIC_LIB).getName());
				details.closestMatch(candidates.get(DYNAMIC_LIB));
			} else {
				LOGGER.debug("Not sure why there is an ambiguation");
			}
		}
	}
}
