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

import dev.nokee.model.internal.ModelObjectIdentity;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

import static dev.nokee.model.internal.discover.CandidateElement.DiscoverChain.Act.REALIZE;

@EqualsAndHashCode
public final class RealizeRule implements DisRule {
	private final DisRule delegate;

	public RealizeRule(DisRule delegate) {
		this.delegate = delegate;
	}

	@Override
	public void execute(Details details) {
		if (!details.getCandidate().isRealized()) {
			delegate.execute(new Details() {
				@Override
				public CandidateElement getCandidate() {
					return details.getCandidate();
				}

				@Override
				public void newCandidate(ModelObjectIdentity<?> knownIdentity) {
					details.newCandidate(knownIdentity, REALIZE);
				}

				@Override
				public void newCandidate(ModelObjectIdentity<?> knownIdentity, CandidateElement.DiscoverChain.Act action) {
					assert action.equals(REALIZE);
					details.newCandidate(knownIdentity, action);
				}

				@Override
				public void newCandidate(ElementName elementName, ModelType<?> produceType) {
					details.newCandidate(elementName, produceType, REALIZE);
				}

				@Override
				public void newCandidate(ElementName elementName, ModelType<?> produceType, CandidateElement.DiscoverChain.Act action) {
					assert action.equals(REALIZE);
					details.newCandidate(elementName, produceType, action);
				}
			});
		}
	}

	@Override
	public String toString() {
		return "on realize of " + delegate;
	}
}
