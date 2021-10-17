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
package dev.nokee.platform.ios.internal;

import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ResolvableComponentDependencies;
import dev.nokee.platform.ios.IosApplication;
import dev.nokee.platform.ios.internal.rules.IosDevelopmentBinaryConvention;
import dev.nokee.platform.nativebase.internal.BaseNativeVariant;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import lombok.Getter;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public class DefaultIosApplicationVariant extends BaseNativeVariant implements IosApplication, VariantInternal, ModelNodeAware {
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();
	@Getter private final ResolvableComponentDependencies resolvableDependencies;
	@Getter private final Property<String> productBundleIdentifier;

	@Inject
	public DefaultIosApplicationVariant(VariantIdentifier<?> identifier, ResolvableComponentDependencies resolvableDependencies, ObjectFactory objects, ProviderFactory providers, TaskProvider<Task> assembleTask, BinaryViewFactory binaryViewFactory) {
		super(identifier, objects, providers, assembleTask, binaryViewFactory);
		this.resolvableDependencies = resolvableDependencies;
		this.productBundleIdentifier = objects.property(String.class);

		getDevelopmentBinary().convention(getBinaries().getElements().flatMap(IosDevelopmentBinaryConvention.INSTANCE));
	}

	@Override
	public DefaultNativeComponentDependencies getDependencies() {
		return ModelProperties.getProperty(this, "dependencies").as(DefaultNativeComponentDependencies.class).get();
	}

	@Override
	public BinaryView<Binary> getBinaries() {
		return ModelProperties.getProperty(this, "binaries").as(BinaryView.class).get();
	}

	@Override
	public ModelNode getNode() {
		return node;
	}
}
