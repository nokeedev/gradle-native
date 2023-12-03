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
import dev.nokee.model.internal.KnownElements;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelMapAdapters;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectIdentifiers;
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
import org.gradle.api.Named;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.reflect.TypeOf;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.instantiator;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.mapOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;

public class ComponentModelBasePlugin implements Plugin<Project> {
	private static final TypeOf<ExtensiblePolymorphicDomainObjectContainer<Component>> COMPONENT_CONTAINER_TYPE = new TypeOf<ExtensiblePolymorphicDomainObjectContainer<Component>>() {};
	private static final TypeOf<ExtensiblePolymorphicDomainObjectContainer<Variant>> VARIANT_CONTAINER_TYPE = new TypeOf<ExtensiblePolymorphicDomainObjectContainer<Variant>>() {};
	private static final TypeOf<ExtensiblePolymorphicDomainObjectContainer<DependencyBucket>> DEPENDENCY_BUCKET_CONTAINER_TYPE = new TypeOf<ExtensiblePolymorphicDomainObjectContainer<DependencyBucket>>() {};
	private static final TypeOf<ExtensiblePolymorphicDomainObjectContainer<Artifact>> ARTIFACT_CONTAINER_TYPE = new TypeOf<ExtensiblePolymorphicDomainObjectContainer<Artifact>>() {};

	public static ExtensiblePolymorphicDomainObjectContainer<Component> components(ExtensionAware target) {
		return target.getExtensions().getByType(COMPONENT_CONTAINER_TYPE);
	}

	public static ExtensiblePolymorphicDomainObjectContainer<Variant> variants(ExtensionAware target) {
		return target.getExtensions().getByType(VARIANT_CONTAINER_TYPE);
	}

	public static ExtensiblePolymorphicDomainObjectContainer<DependencyBucket> dependencyBuckets(ExtensionAware target) {
		return target.getExtensions().getByType(DEPENDENCY_BUCKET_CONTAINER_TYPE);
	}

	public static ExtensiblePolymorphicDomainObjectContainer<Artifact> artifacts(ExtensionAware target) {
		return target.getExtensions().getByType(ARTIFACT_CONTAINER_TYPE);
	}

	@Override
	@SuppressWarnings("unchecked")
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);
		project.getPluginManager().apply("lifecycle-base");

		project.getExtensions().add(COMPONENT_CONTAINER_TYPE, "$components", project.getObjects().polymorphicDomainObjectContainer(Component.class));
		project.getExtensions().add(VARIANT_CONTAINER_TYPE, "$variants", project.getObjects().polymorphicDomainObjectContainer(Variant.class));
		project.getExtensions().add(DEPENDENCY_BUCKET_CONTAINER_TYPE, "$dependencyBuckets", project.getObjects().polymorphicDomainObjectContainer(DependencyBucket.class));
		project.getExtensions().add(ARTIFACT_CONTAINER_TYPE, "$artifacts", project.getObjects().polymorphicDomainObjectContainer(Artifact.class));

		model(project, objects()).register(model(project).getExtensions().create("components", ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, Component.class, new Named.Namer(), components(project), instantiator(project), project, KnownElements.Factory.forProject(project)));
		model(project, objects()).register(model(project).getExtensions().create("variants", ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, Variant.class, new Named.Namer(), variants(project), instantiator(project), project, KnownElements.Factory.forProject(project)));
		model(project, objects()).register(model(project).getExtensions().create("dependencyBuckets", ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, DependencyBucket.class, new Named.Namer(), dependencyBuckets(project), instantiator(project), project, KnownElements.Factory.forProject(project)));
		model(project, objects()).register(model(project).getExtensions().create("artifacts", ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, Artifact.class, new Named.Namer(), artifacts(project), instantiator(project), project, KnownElements.Factory.forProject(project)));

		// FIXME: This is temporary until we convert all entity
		project.afterEvaluate(__ -> {
			model(project, mapOf(Variant.class)).whenElementKnow(it -> it.realizeNow()); // Because outgoing configuration are created when variant realize
		});

		model(project, factoryRegistryOf(DependencyBucket.class)).registerFactory(ConsumableDependencyBucketSpec.class);
		model(project, factoryRegistryOf(DependencyBucket.class)).registerFactory(ResolvableDependencyBucketSpec.class);
		model(project, factoryRegistryOf(DependencyBucket.class)).registerFactory(DeclarableDependencyBucketSpec.class);

		model(project, objects()).configureEach(new TypeOf<DependencyAwareComponent<?>>() {}, new ExtendsFromParentDependencyBucketAction<ApiDependencyBucketMixIn>() {
			@Override
			protected DeclarableDependencyBucketSpec bucketOf(ApiDependencyBucketMixIn dependencies) {
				return dependencies.getApi();
			}
		});
		model(project, objects()).configureEach(new TypeOf<DependencyAwareComponent<?>>() {}, new ExtendsFromParentDependencyBucketAction<ImplementationDependencyBucketMixIn>() {
			@Override
			protected DeclarableDependencyBucketSpec bucketOf(ImplementationDependencyBucketMixIn dependencies) {
				return dependencies.getImplementation();
			}
		});
		model(project, objects()).configureEach(new TypeOf<DependencyAwareComponent<?>>() {}, new ExtendsFromParentDependencyBucketAction<CompileOnlyDependencyBucketMixIn>() {
			@Override
			protected DeclarableDependencyBucketSpec bucketOf(CompileOnlyDependencyBucketMixIn dependencies) {
				return dependencies.getCompileOnly();
			}
		});
		model(project, objects()).configureEach(new TypeOf<DependencyAwareComponent<?>>() {}, new ExtendsFromParentDependencyBucketAction<RuntimeOnlyDependencyBucketMixIn>() {
			@Override
			protected DeclarableDependencyBucketSpec bucketOf(RuntimeOnlyDependencyBucketMixIn dependencies) {
				return dependencies.getRuntimeOnly();
			}
		});
		model(project, objects()).configureEach(new TypeOf<DependencyAwareComponent<?>>() {}, new ImplementationExtendsFromApiDependencyBucketAction());
		model(project, objects()).configureEach(new TypeOf<DependencyAwareComponent<?>>() {}, new ExtendsFromImplementationDependencyBucketAction<CompileOnlyDependencyBucketMixIn>() {
			@Override
			protected DeclarableDependencyBucketSpec bucketOf(CompileOnlyDependencyBucketMixIn dependencies) {
				return dependencies.getCompileOnly();
			}
		});
		model(project, objects()).configureEach(new TypeOf<DependencyAwareComponent<?>>() {}, new ExtendsFromImplementationDependencyBucketAction<RuntimeOnlyDependencyBucketMixIn>() {
			@Override
			protected DeclarableDependencyBucketSpec bucketOf(RuntimeOnlyDependencyBucketMixIn dependencies) {
				return dependencies.getRuntimeOnly();
			}
		});

		project.getPluginManager().apply(DependencyBucketCapabilityPlugin.class);
		project.getPluginManager().apply(AssembleTaskCapabilityPlugin.class);

		model(project).getExtensions().add(new TypeOf<Factory<View<Binary>>>() {}, "__nokeeService_binaryFactory", (Factory<View<Binary>>) () -> {
			Named.Namer namer = new Named.Namer();
			ModelObjectIdentifier identifier = ModelElementSupport.nextIdentifier();
			Runnable realizeNow = () -> {};
			return instantiator(project).newInstance(ViewAdapter.class, Binary.class, new ModelNodeBackedViewStrategy(it -> namer.determineName((Binary) it), artifacts(project), project.getProviders(), project.getObjects(), realizeNow, identifier));
		});

		model(project).getExtensions().add(TaskViewFactory.class, "__nokeeService_taskViewFactory", new TaskViewFactory() {
			@Override
			public <T extends Task> View<T> create(Class<T> elementType) {
				Task.Namer namer = new Task.Namer();
				ModelObjectIdentifier identifier = ModelElementSupport.nextIdentifier();
				Runnable realizeNow = () -> {
					if (elementType.getSimpleName().equals("SourceCompile")) {
						try {
							Class<?> LanguageSourceSet = Class.forName("dev.nokee.language.base.LanguageSourceSet");
							model(project, mapOf(LanguageSourceSet)).whenElementKnow(it -> {
								if (ModelObjectIdentifiers.descendantOf(it.getIdentifier(), identifier)) {
									it.realizeNow(); // force realize
								}
							});
						} catch (ClassNotFoundException e) {
							// ignore
						}
					}
				};
				return instantiator(project).newInstance(ViewAdapter.class, elementType, new ModelNodeBackedViewStrategy(it -> namer.determineName((Task) it), project.getTasks(), project.getProviders(), project.getObjects(), realizeNow, identifier));
			}
		});

		model(project).getExtensions().add(VariantViewFactory.class, "__nokeeService_variantViewFactory", new VariantViewFactory() {
			@Override
			public <T extends Variant> View<T> create(Class<T> elementType) {
				Named.Namer namer = new Named.Namer();
				ModelObjectIdentifier identifier = ModelElementSupport.nextIdentifier();
				Runnable realizeNow = () -> {};
				return instantiator(project).newInstance(ViewAdapter.class, elementType, new ModelNodeBackedViewStrategy(it -> namer.determineName((Variant) it), variants(project), project.getProviders(), project.getObjects(), realizeNow, identifier));
			}
		});

		model(project).getExtensions().add("__nokeeService_dimensionPropertyFactory", new DimensionPropertyRegistrationFactory(project.getObjects()));
		model(project, objects()).configureEach(HasBaseName.class, new BaseNameConfigurationRule(project.getProviders()));
		model(project, mapOf(Component.class)).configureEach(HasDevelopmentBinary.class, new DevelopmentBinaryConventionRule(project.getProviders()));
	}
}
