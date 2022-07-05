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
package dev.nokee.platform.base.internal.dependencies;

import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.DomainObjectIdentifierUtils;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.ConfigurationNamer;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Namer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.tags.ModelTags.tag;

public final class DeclarableDependencyBucketRegistrationFactory {
	private final NamedDomainObjectRegistry<Configuration> configurationRegistry;
	private final DependencyBucketFactory bucketFactory;
	private final ModelLookup lookup;
	private final Namer<DependencyBucketIdentifier> namer = ConfigurationNamer.INSTANCE;

	public DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry<Configuration> configurationRegistry, DependencyBucketFactory bucketFactory, ModelLookup lookup) {
		this.configurationRegistry = configurationRegistry;
		this.bucketFactory = bucketFactory;
		this.lookup = lookup;
	}

	public ModelRegistration create(DependencyBucketIdentifier identifier) {
		val bucket = new DefaultDeclarableDependencyBucket(bucketFactory.create(identifier));
		return ModelRegistration.builder()
			.withComponent(new ElementNameComponent(identifier.getName()))
			.withComponent(new ParentComponent(lookup.get(DomainObjectIdentifierUtils.toPath(identifier.getOwnerIdentifier()))))
			.withComponent(tag(IsDependencyBucket.class))
			.withComponent(tag(ConfigurableTag.class))
			.withComponent(ofInstance(bucket))
			.withComponent(tag(DeclarableDependencyBucketTag.class))
			.build();
	}

	private static final class DefaultDeclarableDependencyBucket implements DeclarableDependencyBucket {
		private final DependencyBucket delegate;

		private DefaultDeclarableDependencyBucket(DependencyBucket delegate) {
			this.delegate = delegate;
		}

		@Override
		public String getName() {
			return delegate.getName();
		}

		@Override
		public void addDependency(Object notation) {
			delegate.addDependency(notation);
		}

		@Override
		public void addDependency(Object notation, Action<? super ModuleDependency> action) {
			delegate.addDependency(notation, action);
		}

		@Override
		public Configuration getAsConfiguration() {
			return delegate.getAsConfiguration();
		}
	}
}
