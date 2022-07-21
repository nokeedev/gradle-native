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
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.provider.Provider;

import java.util.Set;

import static dev.nokee.model.internal.core.ModelProperties.add;

@DomainObjectEntities.Tag({IsDependencyBucket.class, ConsumableDependencyBucketTag.class, ConfigurableTag.class})
public class ConsumableDependencyBucketSpec implements ConsumableDependencyBucket, ModelNodeAware
	, DependencyBucketMixIn
{
	private final ModelNode entity = ModelNodeContext.getCurrentModelNode();

	@Override
	public ConsumableDependencyBucket artifact(Object artifact) {
		val entity = ModelNodes.of(this).get(BucketArtifactsProperty.class).get();
		add(entity, new PublishedArtifactElement((Provider<?>) artifact));
		return this;
	}

	@Override
	public Provider<Set<PublishArtifact>> getArtifacts() {
		return ProviderUtils.supplied(() -> ModelStates.finalize(ModelNodes.of(this)).get(BucketArtifacts.class).get());
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
