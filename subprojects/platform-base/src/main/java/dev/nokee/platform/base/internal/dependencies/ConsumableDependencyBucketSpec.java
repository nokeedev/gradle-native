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
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

@DomainObjectEntities.Tag({IsDependencyBucket.class, ConsumableDependencyBucketTag.class, ConfigurableTag.class})
public class ConsumableDependencyBucketSpec implements ConsumableDependencyBucket, ModelNodeAware
	, ModelBackedNamedMixIn
	, DependencyBucketMixIn
{
	private final ModelNode entity = ModelNodeContext.getCurrentModelNode();
	private final OutgoingArtifacts outgoing;

	@Inject
	public ConsumableDependencyBucketSpec() {
		this.outgoing = ModelNodeUtils.get(entity, OutgoingArtifacts.class);
	}

	@Override
	public ConsumableDependencyBucket artifact(Object artifact) {
		outgoing.getArtifacts().add(new LazyPublishArtifact((Provider<?>) artifact));
		return this;
	}

	@Override
	public ModelNode getNode() {
		return entity;
	}
}
