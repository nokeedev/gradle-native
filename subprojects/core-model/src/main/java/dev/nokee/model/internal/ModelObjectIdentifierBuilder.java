/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.model.internal;

import dev.nokee.model.internal.names.ElementName;

import java.util.Objects;

import static java.util.Objects.requireNonNull;

public final class ModelObjectIdentifierBuilder {
	private ElementName name;
	private String displayName;
	private ModelObjectIdentifier ownerIdentifier;

	public ModelObjectIdentifierBuilder name(ElementName name) {
		this.name = Objects.requireNonNull(name);
		return this;
	}

	public ModelObjectIdentifierBuilder displayName(String displayName) {
		this.displayName = requireNonNull(displayName);
		return this;
	}

	public ModelObjectIdentifierBuilder withParent(ModelObjectIdentifier ownerIdentifier) {
		this.ownerIdentifier = requireNonNull(ownerIdentifier);
		return this;
	}

	public ModelObjectIdentifier build() {
		return new DefaultModelObjectIdentifier(name, displayName, ownerIdentifier);
	}
}
