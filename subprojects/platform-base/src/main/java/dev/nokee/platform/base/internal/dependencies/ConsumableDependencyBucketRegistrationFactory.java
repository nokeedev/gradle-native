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
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.ConfigurationNamer;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.utils.ActionUtils;
import dev.nokee.utils.ConfigurationUtils;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Namer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.internal.artifacts.dsl.LazyPublishArtifact;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.dependencies.DependencyBuckets.toDescription;

public final class ConsumableDependencyBucketRegistrationFactory {
	private final NamedDomainObjectRegistry<Configuration> configurationRegistry;
	private final DependencyBucketFactory bucketFactory;
	private final ObjectFactory objects;
	private final Namer<DependencyBucketIdentifier> namer = ConfigurationNamer.INSTANCE;

	public ConsumableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry<Configuration> configurationRegistry, DependencyBucketFactory bucketFactory, ObjectFactory objects) {
		this.configurationRegistry = configurationRegistry;
		this.bucketFactory = bucketFactory;
		this.objects = objects;
	}

	public ModelRegistration create(DependencyBucketIdentifier identifier) {
		val outgoing = objects.newInstance(OutgoingArtifacts.class);
		val bucket = new DefaultConsumableDependencyBucket(bucketFactory.create(identifier), outgoing);
		val configurationProvider = configurationRegistry.registerIfAbsent(namer.determineName(identifier));
		configurationProvider.configure(ConfigurationUtils.configureAsConsumable());
		configurationProvider.configure(ConfigurationUtils.configureDescription(toDescription(identifier)));
		configurationProvider.configure(configuration -> {
			val extension = ((ExtensionAware) configuration).getExtensions().findByName("__bucket");
			if (extension == null) {
				((ExtensionAware) configuration).getExtensions().add(ConsumableDependencyBucket.class, "__bucket", bucket);
			} else if (!(extension instanceof ConsumableDependencyBucket)) {
				throw new IllegalStateException("Bucket registration mismatch!");
			}
		});
		configurationProvider.configure(attachOutgoingArtifactToConfiguration(outgoing));
		val entityPath = toPath(identifier);
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(tag(IsDependencyBucket.class))
			.withComponent(tag(ConfigurableTag.class))
			.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> configurationProvider))
			.withComponent(createdUsing(of(Configuration.class), configurationProvider::get))
			.withComponent(ofInstance(bucket))
			.withComponent(ofInstance(outgoing))
			.withComponent(tag(ConsumableDependencyBucketTag.class))
			.withComponent(new ConfigurationComponent(configurationProvider))
			.build();
	}

	private static final class DefaultConsumableDependencyBucket implements ConsumableDependencyBucket {
		private final DependencyBucket delegate;
		private final OutgoingArtifacts outgoing;

		private DefaultConsumableDependencyBucket(DependencyBucket delegate, OutgoingArtifacts outgoing) {
			this.delegate = delegate;
			this.outgoing = outgoing;
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
	}

	interface OutgoingArtifacts {
		ListProperty<PublishArtifact> getArtifacts();
	}

	private static ActionUtils.Action<Configuration> attachOutgoingArtifactToConfiguration(OutgoingArtifacts outgoing) {
		return configuration -> {
			configuration.getOutgoing().getArtifacts().addAllLater(outgoing.getArtifacts());
		};
	}
}
