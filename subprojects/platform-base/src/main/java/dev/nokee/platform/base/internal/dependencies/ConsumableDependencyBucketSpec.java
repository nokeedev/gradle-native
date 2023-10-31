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

import dev.nokee.model.DependencyFactory;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.util.internal.LazyPublishArtifact;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.util.Set;

@DomainObjectEntities.Tag({IsDependencyBucket.class, ConsumableDependencyBucketTag.class, ConfigurableTag.class})
public abstract class ConsumableDependencyBucketSpec extends ModelElementSupport implements ConsumableDependencyBucket, ModelNodeAware
	, DependencyBucketMixIn
{
	private final ModelNode entity = ModelNodeContext.getCurrentModelNode();
	private final DependencyFactory factory;

	@Inject
	public ConsumableDependencyBucketSpec(DependencyHandler handler) {
		this.factory = DependencyFactory.forHandler(handler);
	}

	@Override
	public ConsumableDependencyBucket artifact(Object artifact) {
		getAsConfiguration().getOutgoing().getArtifacts().add(new LazyPublishArtifact((Provider<?>) artifact));
		return this;
	}

	@Override
	public Provider<Set<PublishArtifact>> getArtifacts() {
		return getProviders().provider(() -> getAsConfiguration().getOutgoing().getArtifacts());
	}

	@Override
	public DependencyFactory getDependencyFactory() {
		return factory;
	}

	@Override
	public ModelNode getNode() {
		return entity;
	}

	@Override
	public String toString() {
		return "consumable dependency bucket '" + getName() + "'";
	}
}
