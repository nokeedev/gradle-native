/*
 * Copyright 2024 the original author or authors.
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

package dev.nokee.model.internal.discover;

import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.discover.DisRule;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.type.ModelType;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.stream.Collectors;

// Conditional rule based on known data
public final class SelectRule implements DisRule {
	private final ModelType<?> targetType;
	private final List<Case> cases;

	public SelectRule(ModelType<?> targetType, List<Case> cases) {
		this.targetType = targetType;
		this.cases = cases;
	}

	public List<Case> getCases() {
		return cases;
	}

	@Override
	public void execute(Details details) {
		if (details.getCandidate().getType().isSubtypeOf(targetType)) {
			if (details.getCandidate().isKnown()) {
				for (Case aCase : cases) {
					if (aCase.spec.isSatisfiedBy(details.getCandidate().getIdentifier())) {
						details.newCandidate(aCase.elementName, aCase.produceType);
						break;
					}
				}
			} else {
				for (Case aCase : cases) {
					details.newCandidate(aCase.elementName, aCase.produceType);
				}
			}
		}
	}

	@Override
	public String toString() {
		return "any subtype of " + targetType.getConcreteType().getSimpleName() + " will produce one of the following child element: " + cases.stream().map(it -> "when " + it.spec + " produce '" + it.elementName + "' of type " + it.produceType.getConcreteType().getSimpleName()).collect(Collectors.joining(", "));
	}

	public static final class Case {
		private final Spec<? super ModelObjectIdentifier> spec;
		private final ElementName elementName;
		private final ModelType<?> produceType;

		public Case(Spec<? super ModelObjectIdentifier> spec, ElementName elementName, ModelType<?> produceType) {
			this.spec = spec;
			this.elementName = elementName;
			this.produceType = produceType;
		}

		public ModelType<?> getProduceType() {
			return produceType;
		}
	}
}
