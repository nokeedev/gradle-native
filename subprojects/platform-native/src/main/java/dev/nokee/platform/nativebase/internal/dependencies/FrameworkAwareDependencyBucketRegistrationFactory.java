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
package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;

import static dev.nokee.model.internal.tags.ModelTags.tag;

public final class FrameworkAwareDependencyBucketRegistrationFactory {
	private final DeclarableDependencyBucketRegistrationFactory delegate;

	public FrameworkAwareDependencyBucketRegistrationFactory(DeclarableDependencyBucketRegistrationFactory delegate) {
		this.delegate = delegate;
	}

	public ModelRegistration create(DependencyBucketIdentifier identifier) {
		return ModelRegistration.builder()
			.mergeFrom(delegate.create(identifier))
			.withComponent(tag(FrameworkAwareDependencyBucketTag.class))
			.build();
	}
}
