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

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.SourceView;
import dev.nokee.language.base.internal.SourceViewAdapter;
import dev.nokee.language.nativebase.internal.NativeSourcesAware;
import dev.nokee.language.nativebase.internal.NativeSourcesAwareTag;
import dev.nokee.model.capabilities.variants.IsVariant;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.BaseVariant;
import dev.nokee.platform.base.internal.DependencyAwareComponentMixIn;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.ModelBackedBinaryAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedSourceAwareComponentMixIn;
import dev.nokee.platform.base.internal.ModelBackedTaskAwareComponentMixIn;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskMixIn;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.testing.nativebase.NativeTestSuiteVariant;
import org.gradle.api.Task;

import javax.inject.Inject;

@DomainObjectEntities.Tag({NativeSourcesAwareTag.class, IsVariant.class})
public /*final*/ abstract class DefaultNativeTestSuiteVariant extends BaseVariant implements NativeTestSuiteVariant, VariantInternal, ModelNodeAware
	, NativeSourcesAware
	, DependencyAwareComponentMixIn<NativeComponentDependencies>
	, ModelBackedBinaryAwareComponentMixIn
	, ModelBackedSourceAwareComponentMixIn<SourceView<LanguageSourceSet>, SourceViewAdapter<LanguageSourceSet>>
	, ModelBackedTaskAwareComponentMixIn
	, AssembleTaskMixIn
{
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Inject
	public DefaultNativeTestSuiteVariant(ModelObjectRegistry<DependencyBucket> bucketRegistry, ModelObjectRegistry<Task> taskRegistry) {
		getExtensions().create("dependencies", DefaultNativeComponentDependencies.class, getIdentifier(), bucketRegistry);
		getExtensions().add("assembleTask", taskRegistry.register(getIdentifier().child(TaskName.of("assemble")), Task.class).asProvider());
	}

	@Override
	public DefaultNativeComponentDependencies getDependencies() {
		return (DefaultNativeComponentDependencies) DependencyAwareComponentMixIn.super.getDependencies();
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
