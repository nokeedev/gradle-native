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

import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.dependencies.ResolvableComponentDependencies;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.internal.rules.NativeDevelopmentBinaryConvention;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import lombok.Getter;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;

public class DefaultNativeApplicationVariant extends BaseNativeVariant implements NativeApplication, VariantInternal {
	@Getter private final DefaultNativeApplicationComponentDependencies dependencies;
	@Getter private final ResolvableComponentDependencies resolvableDependencies;

	@Inject
	public DefaultNativeApplicationVariant(VariantIdentifier<?> identifier, VariantComponentDependencies<DefaultNativeApplicationComponentDependencies> dependencies, ObjectFactory objects, ProviderFactory providers, TaskProvider<Task> assembleTask, BinaryViewFactory binaryViewFactory) {
		super(identifier, objects, providers, assembleTask, binaryViewFactory);
		this.dependencies = dependencies.getDependencies();
		this.resolvableDependencies = dependencies.getIncoming();

		getDevelopmentBinary().convention(getBinaries().getElements().flatMap(NativeDevelopmentBinaryConvention.of(getBuildVariant().getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS))));
	}
}
