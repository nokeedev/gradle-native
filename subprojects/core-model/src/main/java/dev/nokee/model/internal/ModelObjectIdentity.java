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

import dev.nokee.model.internal.type.ModelType;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class ModelObjectIdentity<ObjectType> {
	private final ModelObjectIdentifier identifier;
	private final ModelType<ObjectType> implementationType;

	public ModelObjectIdentity(ModelObjectIdentifier identifier, ModelType<ObjectType> implementationType) {
		this.identifier = identifier;
		this.implementationType = implementationType;
	}

	public static <T> ModelObjectIdentity<T> ofIdentity(ModelObjectIdentifier identifier, Class<T> type) {
		return new ModelObjectIdentity<>(identifier, ModelType.of(type));
	}

	public static <T> ModelObjectIdentity<T> ofIdentity(ModelObjectIdentifier identifier, ModelType<T> type) {
		return new ModelObjectIdentity<>(identifier, type);
	}

	public String getName() {
		return ModelObjectIdentifiers.asFullyQualifiedName(identifier).toString();
	}

	public ModelObjectIdentifier getIdentifier() {
		return identifier;
	}

	public ModelType<ObjectType> getType() {
		return implementationType;
	}

	@Override
	public String toString() {
		return "ModelObjectIdentity{" +
			"identifier=" + ModelObjectIdentifiers.asPath(identifier) +
			", implementationType=" + implementationType +
			'}';
	}
}