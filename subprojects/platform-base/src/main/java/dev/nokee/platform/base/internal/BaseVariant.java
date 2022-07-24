/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;

public class BaseVariant implements ModelNodeAware {
	private final ModelNode entity = ModelNodeContext.getCurrentModelNode();
	@Getter private final VariantIdentifier identifier;
	@Getter(AccessLevel.PROTECTED) private final ObjectFactory objects;

	protected BaseVariant(VariantIdentifier identifier, ObjectFactory objects) {
		this.identifier = identifier;
		this.objects = objects;
	}

	public BuildVariantInternal getBuildVariant() {
		return (BuildVariantInternal) identifier.getBuildVariant();
	}

	public BinaryView<Binary> getBinaries() {
		throw new UnsupportedOperationException();
	}

	public Property<Binary> getDevelopmentBinary() {
		throw new UnsupportedOperationException();
	}

	@Override
	public ModelNode getNode() {
		return entity;
	}
}
