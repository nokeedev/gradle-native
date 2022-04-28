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

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedDependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.dependencies.ResolvableComponentDependencies;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.NativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelComponentType.componentOf;

public class DefaultNativeApplicationVariant extends BaseNativeVariant implements NativeApplication, VariantInternal, ModelNodeAware
	, ModelBackedDependencyAwareComponentMixIn<NativeApplicationComponentDependencies, ModelBackedNativeApplicationComponentDependencies>
	, ModelBackedBinaryAwareComponentMixIn
	, ModelBackedTaskAwareComponentMixIn
	, ModelBackedNamedMixIn
{
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Inject
	public DefaultNativeApplicationVariant(VariantIdentifier identifier, ObjectFactory objects, ProviderFactory providers, TaskProvider<Task> assembleTask) {
		super(identifier, objects, providers, assembleTask);
	}

	public ResolvableComponentDependencies getResolvableDependencies() {
		return node.getComponent(componentOf(VariantComponentDependencies.class)).getIncoming();
	}

	@Override
	@SuppressWarnings("unchecked")
	public BinaryView<Binary> getBinaries() {
		return (BinaryView<Binary>) ModelProperties.getProperty(this, "binaries").as(BinaryView.class).get();
	}

	@Override
	public ModelNode getNode() {
		return node;
	}
}
