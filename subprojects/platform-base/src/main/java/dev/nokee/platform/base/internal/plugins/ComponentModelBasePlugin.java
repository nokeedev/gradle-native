/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.platform.base.internal.plugins;

import dev.nokee.internal.Factory;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelMap;
import dev.nokee.model.internal.ModelMapFactory;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.HasDevelopmentBinary;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.View;
import dev.nokee.platform.base.internal.DimensionPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.ModelNodeBackedViewStrategy;
import dev.nokee.platform.base.internal.TaskViewFactory;
import dev.nokee.platform.base.internal.VariantViewFactory;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.platform.base.internal.assembletask.AssembleTaskCapabilityPlugin;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketCapabilityPlugin;
import dev.nokee.platform.base.internal.dependencies.LegacyConfigurationFactory;
import dev.nokee.platform.base.internal.dependencies.ModernConfigurationFactory;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import dev.nokee.platform.base.internal.mixins.ApiDependencyBucketMixIn;
import dev.nokee.platform.base.internal.mixins.CompileOnlyDependencyBucketMixIn;
import dev.nokee.platform.base.internal.mixins.ImplementationDependencyBucketMixIn;
import dev.nokee.platform.base.internal.mixins.RuntimeOnlyDependencyBucketMixIn;
import dev.nokee.platform.base.internal.rules.BaseNameConfigurationRule;
import dev.nokee.platform.base.internal.rules.DevelopmentBinaryConventionRule;
import dev.nokee.platform.base.internal.rules.ExtendsFromImplementationDependencyBucketAction;
import dev.nokee.platform.base.internal.rules.ExtendsFromParentDependencyBucketAction;
import dev.nokee.platform.base.internal.rules.ImplementationExtendsFromApiDependencyBucketAction;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.reflect.TypeOf;
import org.gradle.util.GradleVersion;

import static dev.nokee.model.internal.ModelElementAction.withElement;
import static dev.nokee.model.internal.TypeFilteringAction.ofType;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.instantiator;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.mapOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.tasks;

public class ComponentModelBasePlugin implements Plugin<Project> {
	private static final TypeOf<ExtensiblePolymorphicDomainObjectContainer<Component>> COMPONENT_CONTAINER_TYPE = new TypeOf<ExtensiblePolymorphicDomainObjectContainer<Component>>() {};
	private static final TypeOf<ExtensiblePolymorphicDomainObjectContainer<Variant>> VARIANT_CONTAINER_TYPE = new TypeOf<ExtensiblePolymorphicDomainObjectContainer<Variant>>() {};
	private static final TypeOf<ExtensiblePolymorphicDomainObjectContainer<DependencyBucket>> DEPENDENCY_BUCKET_CONTAINER_TYPE = new TypeOf<ExtensiblePolymorphicDomainObjectContainer<DependencyBucket>>() {};
	private static final TypeOf<ExtensiblePolymorphicDomainObjectContainer<Artifact>> ARTIFACT_CONTAINER_TYPE = new TypeOf<ExtensiblePolymorphicDomainObjectContainer<Artifact>>() {};

	public static ModelMap<Component> components(ExtensionAware target) {
		return model(target, mapOf(Component.class));
	}

	public static ModelMap<Variant> variants(ExtensionAware target) {
		return model(target, mapOf(Variant.class));
	}

	public static ModelMap<DependencyBucket> dependencyBuckets(ExtensionAware target) {
		return model(target, mapOf(DependencyBucket.class));
	}

	public static ModelMap<Artifact> artifacts(ExtensionAware target) {
		return model(target, mapOf(Artifact.class));
	}

	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);
		project.getPluginManager().apply("lifecycle-base");

		if (GradleVersion.current().compareTo(GradleVersion.version("8.4")) >= 0) {
			model(project).getExtensions().add("__nokee_configurationFactory", instantiator(project).newInstance(ModernConfigurationFactory.class));
		} else {
			model(project).getExtensions().add("__nokee_configurationFactory", instantiator(project).newInstance(LegacyConfigurationFactory.class));
		}

		{
			final ExtensiblePolymorphicDomainObjectContainer<Component> container = project.getObjects().polymorphicDomainObjectContainer(Component.class);
			project.getExtensions().add(COMPONENT_CONTAINER_TYPE, "$components", container);
			model(project).getExtensions().add("components", model(project).getExtensions().getByType(ModelMapFactory.class).create(Component.class, container));
		}
		{
			final ExtensiblePolymorphicDomainObjectContainer<Variant> container = project.getObjects().polymorphicDomainObjectContainer(Variant.class);
			project.getExtensions().add(VARIANT_CONTAINER_TYPE, "$variants", container);
			model(project).getExtensions().add("variants", model(project).getExtensions().getByType(ModelMapFactory.class).create(Variant.class, container));
		}
		{
			final ExtensiblePolymorphicDomainObjectContainer<DependencyBucket> container = project.getObjects().polymorphicDomainObjectContainer(DependencyBucket.class);
			project.getExtensions().add(DEPENDENCY_BUCKET_CONTAINER_TYPE, "$dependencyBuckets", container);
			model(project).getExtensions().add("dependencyBuckets", model(project).getExtensions().getByType(ModelMapFactory.class).create(DependencyBucket.class, container));
		}
		{
			final ExtensiblePolymorphicDomainObjectContainer<Artifact> container = project.getObjects().polymorphicDomainObjectContainer(Artifact.class);
			project.getExtensions().add(ARTIFACT_CONTAINER_TYPE, "$artifacts", container);
			model(project).getExtensions().add("artifacts", model(project).getExtensions().getByType(ModelMapFactory.class).create(Artifact.class, container));
		}

		model(project, factoryRegistryOf(DependencyBucket.class)).registerFactory(ConsumableDependencyBucketSpec.class);
		model(project, factoryRegistryOf(DependencyBucket.class)).registerFactory(ResolvableDependencyBucketSpec.class);
		model(project, factoryRegistryOf(DependencyBucket.class)).registerFactory(DeclarableDependencyBucketSpec.class);

		model(project, objects()).configureEach(ofType(new TypeOf<DependencyAwareComponent<?>>() {}, withElement(new ExtendsFromParentDependencyBucketAction<ApiDependencyBucketMixIn>() {
			@Override
			protected DeclarableDependencyBucketSpec bucketOf(ApiDependencyBucketMixIn dependencies) {
				return dependencies.getApi();
			}
		})));
		model(project, objects()).configureEach(ofType(new TypeOf<DependencyAwareComponent<?>>() {}, withElement(new ExtendsFromParentDependencyBucketAction<ImplementationDependencyBucketMixIn>() {
			@Override
			protected DeclarableDependencyBucketSpec bucketOf(ImplementationDependencyBucketMixIn dependencies) {
				return dependencies.getImplementation();
			}
		})));
		model(project, objects()).configureEach(ofType(new TypeOf<DependencyAwareComponent<?>>() {}, withElement(new ExtendsFromParentDependencyBucketAction<CompileOnlyDependencyBucketMixIn>() {
			@Override
			protected DeclarableDependencyBucketSpec bucketOf(CompileOnlyDependencyBucketMixIn dependencies) {
				return dependencies.getCompileOnly();
			}
		})));
		model(project, objects()).configureEach(ofType(new TypeOf<DependencyAwareComponent<?>>() {}, withElement(new ExtendsFromParentDependencyBucketAction<RuntimeOnlyDependencyBucketMixIn>() {
			@Override
			protected DeclarableDependencyBucketSpec bucketOf(RuntimeOnlyDependencyBucketMixIn dependencies) {
				return dependencies.getRuntimeOnly();
			}
		})));
		model(project, objects()).configureEach(ofType(new TypeOf<DependencyAwareComponent<?>>() {}, new ImplementationExtendsFromApiDependencyBucketAction()));
		model(project, objects()).configureEach(ofType(new TypeOf<DependencyAwareComponent<?>>() {}, new ExtendsFromImplementationDependencyBucketAction<CompileOnlyDependencyBucketMixIn>() {
			@Override
			protected DeclarableDependencyBucketSpec bucketOf(CompileOnlyDependencyBucketMixIn dependencies) {
				return dependencies.getCompileOnly();
			}
		}));
		model(project, objects()).configureEach(ofType(new TypeOf<DependencyAwareComponent<?>>() {}, new ExtendsFromImplementationDependencyBucketAction<RuntimeOnlyDependencyBucketMixIn>() {
			@Override
			protected DeclarableDependencyBucketSpec bucketOf(RuntimeOnlyDependencyBucketMixIn dependencies) {
				return dependencies.getRuntimeOnly();
			}
		}));

		project.getPluginManager().apply(DependencyBucketCapabilityPlugin.class);
		project.getPluginManager().apply(AssembleTaskCapabilityPlugin.class);

		model(project).getExtensions().add(new TypeOf<Factory<View<Binary>>>() {}, "__nokeeService_binaryFactory", (Factory<View<Binary>>) () -> {
			final ModelObjectIdentifier identifier = ModelElementSupport.nextIdentifier();
			return instantiator(project).newInstance(ViewAdapter.class, Binary.class, new ModelNodeBackedViewStrategy(artifacts(project), identifier));
		});

		model(project).getExtensions().add(TaskViewFactory.class, "__nokeeService_taskViewFactory", new TaskViewFactory() {
			@Override
			public <T extends Task> View<T> create(Class<T> elementType) {
				final ModelObjectIdentifier identifier = ModelElementSupport.nextIdentifier();
				return instantiator(project).newInstance(ViewAdapter.class, elementType, new ModelNodeBackedViewStrategy(tasks(project), identifier));
			}
		});

		model(project).getExtensions().add(VariantViewFactory.class, "__nokeeService_variantViewFactory", new VariantViewFactory() {
			@Override
			public <T extends Variant> View<T> create(Class<T> elementType) {
				final ModelObjectIdentifier identifier = ModelElementSupport.nextIdentifier();
				return instantiator(project).newInstance(ViewAdapter.class, elementType, new ModelNodeBackedViewStrategy(variants(project), identifier));
			}
		});

		model(project).getExtensions().add("__nokeeService_dimensionPropertyFactory", new DimensionPropertyRegistrationFactory(project.getObjects()));

		model(project, objects()).configureEach(ofType(HasBaseName.class, withElement(new BaseNameConfigurationRule(project.getProviders()))));
		components(project).configureEach(HasDevelopmentBinary.class, new DevelopmentBinaryConventionRule(project.getProviders()));
	}
}
