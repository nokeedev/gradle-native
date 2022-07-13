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
import org.gradle.api.file.FileCollection;

import javax.inject.Inject;

@DomainObjectEntities.Tag({IsDependencyBucket.class, ResolvableDependencyBucketTag.class, ConfigurableTag.class})
public class ResolvableDependencyBucketSpec implements ResolvableDependencyBucket, ModelNodeAware
	, DependencyBucketMixIn
{
	private final ModelNode entity = ModelNodeContext.getCurrentModelNode();
	private final IncomingArtifacts incoming;

	@Inject
	public ResolvableDependencyBucketSpec() {
		this.incoming = ModelNodeUtils.get(entity, IncomingArtifacts.class);
	}

	@Override
	public FileCollection getAsLenientFileCollection() {
		return incoming.getAsLenient();
	}

	@Override
	public FileCollection getAsFileCollection() {
		return incoming.get();
	}

	@Override
	public ModelNode getNode() {
		return entity;
	}
}
