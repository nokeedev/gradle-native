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
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.ConfigurationNamer;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.Namer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;

public final class ConsumableDependencyBucketRegistrationFactory {
	private final NamedDomainObjectRegistry<Configuration> configurationRegistry;
	private final DependencyBucketFactory bucketFactory;
	private final ObjectFactory objects;
	private final ModelLookup lookup;
	private final Namer<DependencyBucketIdentifier> namer = ConfigurationNamer.INSTANCE;

	public ConsumableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry<Configuration> configurationRegistry, DependencyBucketFactory bucketFactory, ObjectFactory objects, ModelLookup lookup) {
		this.configurationRegistry = configurationRegistry;
		this.bucketFactory = bucketFactory;
		this.objects = objects;
		this.lookup = lookup;
	}

	public ModelRegistration create(DependencyBucketIdentifier identifier) {
		return ModelRegistration.builder()
			.withComponent(new ElementNameComponent(identifier.getName()))
			.withComponent(new ParentComponent(lookup.get(DomainObjectIdentifierUtils.toPath(identifier.getOwnerIdentifier()))))
			.withComponent(tag(IsDependencyBucket.class))
			.withComponent(tag(ConfigurableTag.class))
			.withComponent(createdUsing(of(DefaultConsumableDependencyBucket.class), () -> {
				return objects.newInstance(DefaultConsumableDependencyBucket.class, bucketFactory.create(identifier));
			}))
			.withComponent(tag(ConsumableDependencyBucketTag.class))
			.build();
	}

	public static class DefaultConsumableDependencyBucket implements ConsumableDependencyBucket, ModelNodeAware {
		private final ModelNode entity = ModelNodeContext.getCurrentModelNode();
		private final DependencyBucket delegate;
		private final OutgoingArtifacts outgoing;

		@Inject
		public DefaultConsumableDependencyBucket(DependencyBucket delegate) {
			this.delegate = delegate;
			this.outgoing = ModelNodeUtils.get(entity, OutgoingArtifacts.class);
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

	interface OutgoingArtifacts {
		ListProperty<PublishArtifact> getArtifacts();
	}
}
