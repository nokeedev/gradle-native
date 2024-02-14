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

import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.util.internal.LazyPublishArtifact;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;
import java.util.Set;

public /*final*/ abstract class ConsumableDependencyBucketSpec extends ModelElementSupport implements ConsumableDependencyBucket
	, DependencyBucketMixIn
{
	@Inject
	public ConsumableDependencyBucketSpec(ConfigurationFactory configurations) {
		getExtensions().add("$configuration", configurations.newConsumable(getIdentifier()));
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
	protected String getTypeName() {
		return "consumable dependency bucket";
	}
}
