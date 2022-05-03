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

import dev.nokee.model.DependencyFactory;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.ExcludeFromQualifyingNameTag;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.DimensionPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.dependencybuckets.CompileOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.ImplementationConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.LinkOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.RuntimeOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.tasks.ModelBackedTaskRegistry;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.rules.DevelopmentVariantConvention;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.BuildType;
import dev.nokee.runtime.nativebase.TargetBuildType;
import dev.nokee.runtime.nativebase.TargetLinkage;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import dev.nokee.runtime.nativebase.internal.TargetBuildTypes;
import dev.nokee.runtime.nativebase.internal.TargetLinkages;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.internal.Cast;

import java.util.function.BiConsumer;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.declarable;

public final class IosApplicationComponentModelRegistrationFactory {
	private final Class<Component> componentType;
	private final Class<Component> implementationComponentType;
	private final BiConsumer<? super ModelNode, ? super ModelPath> sourceRegistration;
	private final Project project;

	@SuppressWarnings("unchecked")
	public <T extends Component> IosApplicationComponentModelRegistrationFactory(Class<? super T> componentType, Class<T> implementationComponentType, Project project, BiConsumer<? super ModelNode, ? super ModelPath> sourceRegistration) {
		this.componentType = (Class<Component>) componentType;
		this.implementationComponentType = (Class<Component>) implementationComponentType;
		this.sourceRegistration = sourceRegistration;
		this.project = project;
	}

	public ModelRegistration create(ComponentIdentifier identifier) {
		val entityPath = ModelPath.path(identifier.getName().get());
		val builder = ModelRegistration.builder()
			.withComponent(new ModelPathComponent(entityPath))
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(createdUsing(of(implementationComponentType), () -> project.getObjects().newInstance(implementationComponentType)))
			.withComponent(IsComponent.tag())
			.withComponent(ConfigurableTag.tag())
			.withComponent(IosApplicationComponentTag.tag())
			.withComponent(createdUsing(of(DefaultIosApplicationComponent.class), () -> create(identifier.getName().get(), project)))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(ModelState.class), new ModelActionWithInputs.A2<ModelPathComponent, ModelState>() {
				private boolean alreadyExecuted = false;

				@Override
				public void execute(ModelNode entity, ModelPathComponent path, ModelState state) {
					if (entityPath.equals(path.get()) && state.isAtLeast(ModelState.Registered) && !alreadyExecuted) {
						alreadyExecuted = true;
						val registry = project.getExtensions().getByType(ModelRegistry.class);

						sourceRegistration.accept(entity, path.get());

						val bucketFactory = new DeclarableDependencyBucketRegistrationFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DefaultDependencyBucketFactory(NamedDomainObjectRegistry.of(project.getConfigurations()), DependencyFactory.forProject(project))));

						val implementation = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("implementation"), identifier)));
						val compileOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("compileOnly"), identifier)));
						val linkOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("linkOnly"), identifier)));
						val runtimeOnly = registry.register(bucketFactory.create(DependencyBucketIdentifier.of(declarable("runtimeOnly"), identifier)));

						entity.addComponent(new ImplementationConfigurationComponent(ModelNodes.of(implementation)));
						entity.addComponent(new CompileOnlyConfigurationComponent(ModelNodes.of(compileOnly)));
						entity.addComponent(new LinkOnlyConfigurationComponent(ModelNodes.of(linkOnly)));
						entity.addComponent(new RuntimeOnlyConfigurationComponent(ModelNodes.of(runtimeOnly)));

						val dimensions = project.getExtensions().getByType(DimensionPropertyRegistrationFactory.class);
						registry.register(dimensions.newAxisProperty(ModelPropertyIdentifier.of(identifier, "targetLinkages"))
							.elementType(TargetLinkage.class)
							.axis(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)
							.defaultValue(TargetLinkages.EXECUTABLE)
							.build());
						registry.register(dimensions.newAxisProperty(ModelPropertyIdentifier.of(identifier, "targetBuildTypes"))
							.elementType(TargetBuildType.class)
							.axis(BuildType.BUILD_TYPE_COORDINATE_AXIS)
							.defaultValue(TargetBuildTypes.named("Default"))
							.build());
						registry.register(dimensions.newAxisProperty(ModelPropertyIdentifier.of(identifier, "targetMachines"))
							.axis(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS)
							.defaultValue(NativeRuntimeBasePlugin.TARGET_MACHINE_FACTORY.os("ios").getX86_64())
							.build());
					}
				}
			}))
			;

		if (identifier.isMainComponent()) {
			builder.withComponent(ExcludeFromQualifyingNameTag.tag());
		}

		return builder.build();
	}

	@SuppressWarnings("unchecked")
	private static DefaultIosApplicationComponent create(String name, Project project) {
		val identifier = ComponentIdentifier.of(ComponentName.of(name), ProjectIdentifier.of(project));
		val result = new DefaultIosApplicationComponent(Cast.uncheckedCast(identifier), project.getObjects(), project.getProviders(), project.getLayout(), project.getConfigurations(), project.getDependencies(), ModelBackedTaskRegistry.newInstance(project), project.getExtensions().getByType(ModelRegistry.class));
		result.getDevelopmentVariant().convention((Provider<? extends DefaultIosApplicationVariant>) project.getProviders().provider(new DevelopmentVariantConvention<>(() -> (Iterable<? extends VariantInternal>) result.getVariants().map(VariantInternal.class::cast).get())));
		return result;
	}
}
