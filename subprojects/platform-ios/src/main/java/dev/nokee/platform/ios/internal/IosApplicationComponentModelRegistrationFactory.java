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
package dev.nokee.platform.ios.internal;

import dev.nokee.internal.Factory;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.ExcludeFromQualifyingNameTag;
import dev.nokee.model.internal.names.MainName;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentSources;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.nativebase.internal.rules.DevelopmentVariantConvention;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.TypeOf;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.DomainObjectEntities.tagsOf;

public final class IosApplicationComponentModelRegistrationFactory {
	private final Class<Component> implementationComponentType;
	private final Project project;

	@SuppressWarnings("unchecked")
	public <T extends Component> IosApplicationComponentModelRegistrationFactory(Class<T> implementationComponentType, Project project) {
		this.implementationComponentType = (Class<Component>) implementationComponentType;
		this.project = project;
	}

	public ModelRegistration.Builder create(ModelObjectIdentifier identifier) {
		val builder = ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(createdUsing(of(implementationComponentType), () -> project.getObjects().newInstance(implementationComponentType)))
			.withComponentTag(ConfigurableTag.class)
			.withComponentTag(IosApplicationComponentTag.class)
			.mergeFrom(tagsOf(implementationComponentType))
			.withComponent(createdUsing(of(DefaultIosApplicationComponent.class), () -> create(project)))
			;

		if (identifier.getName() instanceof MainName) {
			builder.withComponentTag(ExcludeFromQualifyingNameTag.class);
		}

		return builder;
	}

	@SuppressWarnings("unchecked")
	private static DefaultIosApplicationComponent create(Project project) {
		val result = project.getObjects().newInstance(DefaultIosApplicationComponent.class, model(project, registryOf(DependencyBucket.class)), project.getExtensions().getByType(new TypeOf<Factory<BinaryView<Binary>>>() {}), project.getExtensions().getByType(new TypeOf<Factory<ComponentSources>>() {}), project.getExtensions().getByType(new TypeOf<Factory<TaskView<Task>>>() {}), model(project, registryOf(Task.class)), model(project, registryOf(Artifact.class)));
		result.getDevelopmentVariant().convention((Provider<? extends DefaultIosApplicationVariant>) project.getProviders().provider(new DevelopmentVariantConvention<>(() -> (Iterable<? extends VariantInternal>) result.getVariants().map(VariantInternal.class::cast).get())));
		return result;
	}
}
