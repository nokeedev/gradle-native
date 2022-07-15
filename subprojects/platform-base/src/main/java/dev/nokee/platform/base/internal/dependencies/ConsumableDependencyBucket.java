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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.provider.Provider;

import java.util.Set;

/**
 * Represent a bucket of consumable dependencies also known as outgoing dependencies.
 *
 * @since 0.5
 */
public interface ConsumableDependencyBucket extends DependencyBucket {
	ConsumableDependencyBucket artifact(Object artifact);

	Provider<Set<PublishArtifact>> getArtifacts();
}
