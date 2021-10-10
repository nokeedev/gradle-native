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

import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactory;
import dev.nokee.utils.ActionUtils;
import lombok.val;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.PublishArtifact;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ListProperty;
import org.gradle.api.provider.Provider;

import static dev.nokee.model.internal.core.ModelActions.initialize;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Bucket.ofConsumable;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Subject.ofName;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.*;

public final class ConsumableDependencyBucketRegistrationFactory implements NodeRegistrationFactory {
	private final ProjectConfigurationRegistry configurationRegistry;
	private final ConfigurationNamingScheme namingScheme;
	private final ConfigurationDescriptionScheme descriptionScheme;

	public ConsumableDependencyBucketRegistrationFactory(ProjectConfigurationRegistry configurationRegistry, ConfigurationNamingScheme namingScheme, ConfigurationDescriptionScheme descriptionScheme) {
		this.configurationRegistry = configurationRegistry;
		this.namingScheme = namingScheme;
		this.descriptionScheme = descriptionScheme;
	}

	@Override
	public NodeRegistration create(String name) {
		return NodeRegistration.of(name, of(ConsumableDependencyBucket.class))
			.action(self(initialize(context -> {
				context.withProjection(managed(of(DependencyBucketProjection.class), name));
				val outgoingArtifacts = context.withProjection(managed(of(OutgoingArtifacts.class)));

				context.withProjection(ofInstance(configurationRegistry.createIfAbsent(nameOf(name),
					asConsumable().andThen(attachOutgoingArtifactToConfiguration(outgoingArtifacts)).andThen(descriptionOf(name)))));
			})));
	}

	private String nameOf(String name) {
		return namingScheme.configurationName(name);
	}

	private ActionUtils.Action<Configuration> descriptionOf(String name) {
		return description(descriptionScheme.description(ofName(name), ofConsumable()));
	}

	interface OutgoingArtifacts {
		ListProperty<PublishArtifact> getArtifacts();
	}

	private static ActionUtils.Action<Configuration> attachOutgoingArtifactToConfiguration(KnownDomainObject<? extends OutgoingArtifacts> provider) {
		return withObjectFactory((configuration, objectFactory) -> {
			configuration.getOutgoing().getArtifacts().addAllLater(asCollectionProvider(objectFactory, provider.flatMap(OutgoingArtifacts::getArtifacts)));
		});
	}

	private static <T> Provider<? extends Iterable<T>> asCollectionProvider(ObjectFactory objectFactory, Provider<? extends Iterable<T>> provider) {
		val result = (ListProperty<T>) objectFactory.listProperty(Object.class);
		result.addAll(provider);
		return result;
	}
}
