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

import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelElementProviderSourceComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.ConfigurationNamer;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Namer;
import org.gradle.api.artifacts.ArtifactView;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.ExtensionAware;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;

public final class ResolvableDependencyBucketRegistrationFactory {
	private final NamedDomainObjectRegistry<Configuration> configurationRegistry;
	private final DependencyBucketFactory bucketFactory;
	private final Namer<DependencyBucketIdentifier> namer = ConfigurationNamer.INSTANCE;

	public ResolvableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry<Configuration> configurationRegistry, DependencyBucketFactory bucketFactory) {
		this.configurationRegistry = configurationRegistry;
		this.bucketFactory = bucketFactory;
	}

	public ModelRegistration create(DependencyBucketIdentifier identifier) {
		val configurationProvider = configurationRegistry.registerIfAbsent(namer.determineName(identifier));
		val incoming = new IncomingArtifacts(configurationProvider);
		val bucket = new DefaultResolvableDependencyBucket(bucketFactory.create(identifier), incoming);
		configurationProvider.configure(configuration -> {
			val extension = ((ExtensionAware) configuration).getExtensions().findByName("__bucket");
			if (extension == null) {
				((ExtensionAware) configuration).getExtensions().add(ResolvableDependencyBucket.class, "__bucket", bucket);
			} else if (!(extension instanceof ResolvableDependencyBucket)) {
				throw new IllegalStateException("Bucket registration mismatch!");
			}
		});
		val entityPath = toPath(identifier);
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(tag(IsDependencyBucket.class))
			.withComponent(tag(ConfigurableTag.class))
			.withComponent(new ModelElementProviderSourceComponent(configurationProvider))
			.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> configurationProvider))
			.withComponent(createdUsing(of(Configuration.class), configurationProvider::get))
			.withComponent(ofInstance(bucket))
			.withComponent(ofInstance(incoming))
			.withComponent(tag(ResolvableDependencyBucketTag.class))
			.withComponent(new ConfigurationComponent(configurationProvider))
			.build();
	}

	private static final class DefaultResolvableDependencyBucket implements ResolvableDependencyBucket {
		private final DependencyBucket delegate;
		private final IncomingArtifacts incoming;

		private DefaultResolvableDependencyBucket(DependencyBucket delegate, IncomingArtifacts incoming) {
			this.delegate = delegate;
			this.incoming = incoming;
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
