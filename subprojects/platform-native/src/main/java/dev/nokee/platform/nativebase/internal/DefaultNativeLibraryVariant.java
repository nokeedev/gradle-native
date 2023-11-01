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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.nativebase.internal.NativeSourcesAwareTag;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.BaseVariant;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedDependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.VariantMixIn;
import dev.nokee.platform.base.internal.assembletask.HasAssembleTaskMixIn;
import dev.nokee.platform.base.internal.developmentbinary.HasDevelopmentBinaryMixIn;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.NativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeLibraryComponentDependencies;
import org.gradle.api.provider.Property;

@DomainObjectEntities.Tag(NativeSourcesAwareTag.class)
public /*final*/ abstract class DefaultNativeLibraryVariant extends BaseVariant implements NativeLibrary, VariantInternal, ModelNodeAware
	, VariantMixIn
	, ModelBackedDependencyAwareComponentMixIn<NativeLibraryComponentDependencies, ModelBackedNativeLibraryComponentDependencies>
	, ModelBackedBinaryAwareComponentMixIn
	, ModelBackedTaskAwareComponentMixIn
	, HasDevelopmentBinaryMixIn
	, HasAssembleTaskMixIn
{
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Override
	public Property<Binary> getDevelopmentBinary() {
		return HasDevelopmentBinaryMixIn.super.getDevelopmentBinary();
	}

	@Override
	@SuppressWarnings("unchecked")
	public BinaryView<Binary> getBinaries() {
		return ModelProperties.getProperty(this, "binaries").as(BinaryView.class).get();
	}

	@Override
	public ModelNode getNode() {
		return node;
	}
}
