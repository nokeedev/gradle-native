/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import org.gradle.api.provider.MapProperty;

public final class ModelBackedNativeComponentDependencies implements NativeComponentDependencies, ModelNodeAware {
	private final ModelNode node = ModelNodeContext.getCurrentModelNode();

	@Override
	@SuppressWarnings("unchecked")
	public DependencyBucket getCompileOnly() {
		return ((MapProperty<String, DependencyBucket>) node.get(GradlePropertyComponent.class).get()).getting("compileOnly").get();
	}

	@Override
	@SuppressWarnings("unchecked")
	public DependencyBucket getImplementation() {
		return ((MapProperty<String, DependencyBucket>) node.get(GradlePropertyComponent.class).get()).getting("implementation").get();
	}

	@Override
	@SuppressWarnings("unchecked")
	public DependencyBucket getRuntimeOnly() {
		return ((MapProperty<String, DependencyBucket>) node.get(GradlePropertyComponent.class).get()).getting("runtimeOnly").get();
	}

	@Override
	@SuppressWarnings("unchecked")
	public DependencyBucket getLinkOnly() {
		return ((MapProperty<String, DependencyBucket>) node.get(GradlePropertyComponent.class).get()).getting("linkOnly").get();
	}

	@Override
	public ModelNode getNode() {
		return node;
	}
}
