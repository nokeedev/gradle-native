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

import com.google.common.annotations.VisibleForTesting;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.NodeRegistration;
import dev.nokee.model.internal.core.NodeRegistrationFactory;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.utils.ActionUtils;
import lombok.val;
import org.gradle.api.artifacts.ArtifactView;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;
import org.gradle.api.internal.artifacts.configurations.ConfigurationInternal;

import javax.inject.Inject;

import static dev.nokee.model.internal.core.ModelActions.initialize;
import static dev.nokee.model.internal.core.ModelProjections.managed;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Bucket.ofResolvable;
import static dev.nokee.platform.base.internal.dependencies.ConfigurationDescription.Subject.ofName;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.asResolvable;
import static dev.nokee.platform.base.internal.dependencies.ProjectConfigurationActions.description;

public final class ResolvableDependencyBucketRegistrationFactory implements NodeRegistrationFactory<ResolvableDependencyBucket> {
	private final ProjectConfigurationRegistry configurationRegistry;
	private final ConfigurationNamingScheme namingScheme;
	private final ConfigurationDescriptionScheme descriptionScheme;

	@VisibleForTesting
	ResolvableDependencyBucketRegistrationFactory(ProjectConfigurationRegistry configurationRegistry) {
		this(configurationRegistry, ConfigurationNamingScheme.identity(), ConfigurationDescriptionScheme.forThisProject());
	}

	public ResolvableDependencyBucketRegistrationFactory(ProjectConfigurationRegistry configurationRegistry, ConfigurationNamingScheme namingScheme, ConfigurationDescriptionScheme descriptionScheme) {
		this.configurationRegistry = configurationRegistry;
		this.namingScheme = namingScheme;
		this.descriptionScheme = descriptionScheme;
	}

	@Override
	public NodeRegistration<ResolvableDependencyBucket> create(String name) {
		return NodeRegistration.of(name, of(ResolvableDependencyBucket.class))
			.action(self(initialize(context -> {
				context.withProjection(managed(of(IncomingArtifacts.class)));
				context.withProjection(managed(of(DependencyBucketProjection.class), name));
				context.withProjection(ofInstance(configurationRegistry.createIfAbsent(nameOf(name),
					asResolvable()
						.andThen(descriptionOf(name))
						.andThen(realizeNodeBeforeResolve()))));
			})));
	}

	private String nameOf(String name) {
		return namingScheme.configurationName(name);
	}

	private ActionUtils.Action<Configuration> descriptionOf(String name) {
		return description(descriptionScheme.description(ofName(name), ofResolvable()));
	}

	private static ActionUtils.Action<Configuration> realizeNodeBeforeResolve() {
		val node = ModelNodeContext.getCurrentModelNode();
		return configuration -> {
			((ConfigurationInternal) configuration).beforeLocking(it -> ModelStates.realize(node));
		};
	}

	static class IncomingArtifacts {
		private final Configuration delegate = ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), Configuration.class);

		@Inject
		public IncomingArtifacts() {}

		public FileCollection get() {
			return delegate.getIncoming().getFiles();
		}

		public FileCollection getAsLenient() {
			return delegate.getIncoming().artifactView(this::asLenient).getFiles();
		}

		private void asLenient(ArtifactView.ViewConfiguration view) {
			view.setLenient(true);
		}
	}
}
