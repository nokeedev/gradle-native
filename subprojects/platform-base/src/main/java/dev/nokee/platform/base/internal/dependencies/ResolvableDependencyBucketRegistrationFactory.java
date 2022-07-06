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
import dev.nokee.platform.base.internal.IsDependencyBucket;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.ArtifactView;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;

public final class ResolvableDependencyBucketRegistrationFactory {
	private final DependencyBucketFactory bucketFactory;
	private final ModelLookup lookup;
	private final ObjectFactory objects;

	public ResolvableDependencyBucketRegistrationFactory(DependencyBucketFactory bucketFactory, ModelLookup lookup, ObjectFactory objects) {
		this.bucketFactory = bucketFactory;
		this.lookup = lookup;
		this.objects = objects;
	}

	public ModelRegistration create(DependencyBucketIdentifier identifier) {
		return ModelRegistration.builder()
			.withComponent(new ElementNameComponent(identifier.getName()))
			.withComponent(new ParentComponent(lookup.get(DomainObjectIdentifierUtils.toPath(identifier.getOwnerIdentifier()))))
			.withComponent(tag(IsDependencyBucket.class))
			.withComponent(tag(ConfigurableTag.class))
			.withComponent(createdUsing(of(DefaultResolvableDependencyBucket.class), () -> {
				return objects.newInstance(DefaultResolvableDependencyBucket.class, bucketFactory.create(identifier));
			}))
			.withComponent(tag(ResolvableDependencyBucketTag.class))
			.build();
	}

	public static class DefaultResolvableDependencyBucket implements ResolvableDependencyBucket, ModelNodeAware {
		private final ModelNode entity = ModelNodeContext.getCurrentModelNode();
		private final DependencyBucket delegate;
		private final IncomingArtifacts incoming;

		@Inject
		public DefaultResolvableDependencyBucket(DependencyBucket delegate) {
			this.delegate = delegate;
			this.incoming = ModelNodeUtils.get(entity, IncomingArtifacts.class);
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

	static final class IncomingArtifacts {
		private final NamedDomainObjectProvider<Configuration> delegate;

		public IncomingArtifacts(NamedDomainObjectProvider<Configuration> delegate) {
			this.delegate = delegate;
		}

		public FileCollection get() {
			return delegate.get().getIncoming().getFiles();
		}

		public FileCollection getAsLenient() {
			return delegate.get().getIncoming().artifactView(this::asLenient).getFiles();
		}

		private void asLenient(ArtifactView.ViewConfiguration view) {
			view.setLenient(true);
		}
	}
}
