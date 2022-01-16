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
package dev.nokee.testing.nativebase.internal;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ResolvableComponentDependencies;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.BaseNativeVariant;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.internal.rules.NativeDevelopmentBinaryConvention;
import dev.nokee.platform.objectivec.ObjectiveCApplicationSources;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.testing.nativebase.NativeTestSuiteVariant;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelComponentType.componentOf;

public class DefaultNativeTestSuiteVariant extends BaseNativeVariant implements NativeTestSuiteVariant, VariantInternal, ModelNodeAware
	, ModelBackedDependencyAwareComponentMixIn<NativeComponentDependencies>
	, ModelBackedBinaryAwareComponentMixIn
	, ModelBackedSourceAwareComponentMixIn<ObjectiveCApplicationSources>
	, ModelBackedNamedMixIn
{
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Inject
	public DefaultNativeTestSuiteVariant(VariantIdentifier<?> identifier, ObjectFactory objects, ProviderFactory providers, TaskProvider<Task> assembleTask, BinaryViewFactory binaryViewFactory) {
		super(identifier, objects, providers, assembleTask, binaryViewFactory);

		getDevelopmentBinary().convention(getBinaries().getElements().flatMap(NativeDevelopmentBinaryConvention.of(getBuildVariant().getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS))));
	}

	public ResolvableComponentDependencies getResolvableDependencies() {
		return node.getComponent(componentOf(VariantComponentDependencies.class)).getIncoming();
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
