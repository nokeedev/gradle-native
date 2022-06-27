/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.internal.DomainObjectEntities;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.tags.ModelTag;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

import javax.inject.Inject;

import static dev.nokee.model.internal.buffers.ModelBuffers.typeOf;

@DomainObjectEntities.Tag({DeclarableDependencyBucketSpec.Tag.class, ConfigurableTag.class, IsDependencyBucket.class})
public /*final*/ class DeclarableDependencyBucketSpec implements DeclarableDependencyBucket, ModelNodeAware {
	private final ModelNode node;

	@Inject
	public DeclarableDependencyBucketSpec() {
		this(ModelNodeContext.getCurrentModelNode());
	}

	DeclarableDependencyBucketSpec(ModelNode entity) {
		this.node = entity;
	}

	@Override
	public String getName() {
		return node.get(FullyQualifiedNameComponent.class).get().toString();
	}

	@Override
	public void addDependency(Object notation) {
		node.setComponent(node.getComponent(typeOf(DependencyElement.class)).appended(new DependencyElement(notation)));
	}

	@Override
	public void addDependency(Object notation, Action<? super ModuleDependency> action) {
		node.setComponent(node.getComponent(typeOf(DependencyElement.class)).appended(new DependencyElement(notation, action)));
	}

	@Override
	public Configuration getAsConfiguration() {
		return ModelNodeUtils.get(node, Configuration.class);
	}

	@Override
	public ModelNode getNode() {
		return node;
	}

	public interface Tag extends ModelTag {}
}
