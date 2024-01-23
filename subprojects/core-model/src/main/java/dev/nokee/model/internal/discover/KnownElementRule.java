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

import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.ModelObjectIdentity;
import lombok.EqualsAndHashCode;
import org.gradle.api.Project;

import static dev.nokee.model.internal.type.ModelType.of;

@EqualsAndHashCode
public final class KnownElementRule implements DisRule {
	private final ModelObjectIdentity<?> identity;

	public KnownElementRule(ModelObjectIdentity<?> identity) {
		this.identity = identity;
	}

	public ModelObjectIdentity<?> getIdentity() {
		return identity;
	}

	@Override
	public void execute(Details details) {
		if (details.getCandidate().getType().equals(of(Project.class))) {
			details.newCandidate(identity);
		}
	}

	@Override
	public String toString() {
		return ModelObjectIdentifiers.asFullyQualifiedName(identity.getIdentifier()) + " (" + identity.getType().getConcreteType().getSimpleName() + ")";
	}
}
