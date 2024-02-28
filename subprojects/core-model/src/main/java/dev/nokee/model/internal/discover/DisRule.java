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

public interface DisRule {
	void execute(Details details);

	interface Details {
		CandidateElement getCandidate();

		void newCandidate(ModelObjectIdentity<?> knownIdentity);

		void newCandidate(ModelObjectIdentity<?> knownIdentity, CandidateElement.DiscoverChain.Act action);

		void newCandidate(ElementName elementName, ModelType<?> produceType);

		void newCandidate(ElementName elementName, ModelType<?> produceType, CandidateElement.DiscoverChain.Act action);
	}
}
