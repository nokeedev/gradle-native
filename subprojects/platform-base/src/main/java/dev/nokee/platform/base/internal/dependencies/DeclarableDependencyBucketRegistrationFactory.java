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

import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactory;
import dev.nokee.utils.ActionUtils;
import org.gradle.api.artifacts.Configuration;

import static dev.nokee.model.internal.core.ModelActions.initialize;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Bucket.ofDeclarable;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Subject.ofName;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.asDeclarable;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.description;

public final class DeclarableDependencyBucketRegistrationFactory implements NodeRegistrationFactory<DeclarableDependencyBucket> {
	private final ProjectConfigurationRegistry configurationRegistry;
	private final ConfigurationNamingScheme namingScheme;
	private final ConfigurationDescriptionScheme descriptionScheme;

	public DeclarableDependencyBucketRegistrationFactory(ProjectConfigurationRegistry configurationRegistry, ConfigurationNamingScheme namingScheme, ConfigurationDescriptionScheme descriptionScheme) {
		this.configurationRegistry = configurationRegistry;
		this.namingScheme = namingScheme;
		this.descriptionScheme = descriptionScheme;
	}

	@Override
	public NodeRegistration<DeclarableDependencyBucket> create(String name) {
		return NodeRegistration.of(name, of(DeclarableDependencyBucket.class))
			.action(self(initialize(context -> {
				context.withProjection(managed(of(DependencyBucketProjection.class), name));
				context.withProjection(ofInstance(configurationRegistry.createIfAbsent(nameOf(name),
					asDeclarable().andThen(descriptionOf(name)))));
			})));
	}

	private String nameOf(String name) {
		return namingScheme.configurationName(name);
	}

	private ActionUtils.Action<Configuration> descriptionOf(String name) {
		return description(descriptionScheme.description(ofName(name), ofDeclarable()));
	}
}
