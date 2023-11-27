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
import dev.nokee.model.internal.ModelMapAdapters;
import dev.nokee.model.internal.ModelObject;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.decorators.DecoratorHandlers;
import dev.nokee.model.internal.decorators.ModelMixInSupport;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.HasDevelopmentBinary;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantDimensions;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.BinaryViewAdapter;
import dev.nokee.platform.base.internal.DefaultVariantDimensions;
import dev.nokee.platform.base.internal.DimensionPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.ModelNodeBackedViewStrategy;
import dev.nokee.platform.base.internal.TaskViewAdapter;
import dev.nokee.platform.base.internal.VariantViewAdapter;
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
import dev.nokee.platform.base.internal.tasks.TaskName;
import org.gradle.api.ExtensiblePolymorphicDomainObjectContainer;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.Task;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.reflect.TypeOf;

import java.lang.reflect.ParameterizedType;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.function.Function;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.instantiator;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.mapOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.registryOf;

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

		model(project, objects()).register(model(project).getExtensions().create("components", ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, Component.class, components(project), ProjectIdentifier.of(project), instantiator(project)));
		model(project, objects()).register(model(project).getExtensions().create("variants", ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, Variant.class, variants(project), ProjectIdentifier.of(project), instantiator(project)));
		model(project, objects()).register(model(project).getExtensions().create("dependencyBuckets", ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, DependencyBucket.class, dependencyBuckets(project), ProjectIdentifier.of(project), instantiator(project)));
		model(project, objects()).register(model(project).getExtensions().create("artifacts", ModelMapAdapters.ForExtensiblePolymorphicDomainObjectContainer.class, Artifact.class, artifacts(project), ProjectIdentifier.of(project), instantiator(project)));

		// FIXME: This is temporary until we convert all entity
		project.afterEvaluate(__ -> {
			model(project, mapOf(Variant.class)).whenElementKnow(it -> it.realizeNow()); // Because outgoing configuration are created when variant realize
		});

		model(project, factoryRegistryOf(DependencyBucket.class)).registerFactory(ConsumableDependencyBucketSpec.class, name -> {
			return instantiator(project).newInstance(ConsumableDependencyBucketSpec.class, model(project, registryOf(Configuration.class)));
		});
		model(project, factoryRegistryOf(DependencyBucket.class)).registerFactory(ResolvableDependencyBucketSpec.class, name -> {
			return instantiator(project).newInstance(ResolvableDependencyBucketSpec.class, model(project, registryOf(Configuration.class)));
		});
		model(project, factoryRegistryOf(DependencyBucket.class)).registerFactory(DeclarableDependencyBucketSpec.class, name -> {
			return instantiator(project).newInstance(DeclarableDependencyBucketSpec.class, model(project, registryOf(Configuration.class)));
		});

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

		project.getExtensions().getByType(DecoratorHandlers.class).nestedObject(new Consumer<DecoratorHandlers.NestedObjectContext>() {
			private final Factory<BinaryView<Binary>> binariesFactory = () -> {
				Named.Namer namer = new Named.Namer();
				ModelObjectIdentifier identifier = ModelElementSupport.nextIdentifier();
				Runnable realizeNow = () -> {};
				return new BinaryViewAdapter<>(new ViewAdapter<>(Binary.class, new ModelNodeBackedViewStrategy(it -> namer.determineName((Binary) it), artifacts(project), project.getProviders(), project.getObjects(), realizeNow, identifier)));
			};

			@Override
			public void accept(DecoratorHandlers.NestedObjectContext context) {
				if (context.getNestedType().isSubtypeOf(BinaryView.class)) {
					context.mixIn(binariesFactory.create());
				}
			}
		});

		project.getExtensions().getByType(DecoratorHandlers.class).nestedObject(new Consumer<DecoratorHandlers.NestedObjectContext>() {
			private <T extends Task> TaskView<T> create(Class<T> elementType) {
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
				return new TaskViewAdapter<>(new ViewAdapter<>(elementType, new ModelNodeBackedViewStrategy(it -> namer.determineName((Task) it), project.getTasks(), project.getProviders(), project.getObjects(), realizeNow, identifier)));
			};

			@Override
			public void accept(DecoratorHandlers.NestedObjectContext context) {
				if (context.getNestedType().isSubtypeOf(TaskView.class)) {
					final Class<? extends Task> elementType = (Class<? extends Task>) ((ParameterizedType) context.getNestedType().getType()).getActualTypeArguments()[0];
					context.mixIn(create(elementType));
				}
			}
		});

		project.getExtensions().getByType(DecoratorHandlers.class).nestedObject(new Consumer<DecoratorHandlers.NestedObjectContext>() {
			private final VariantViewFactory variantsFactory = new VariantViewFactory() {
				@Override
				public <T extends Variant> VariantView<T> create(Class<T> elementType) {
					Named.Namer namer = new Named.Namer();
					ModelObjectIdentifier identifier = ModelElementSupport.nextIdentifier();
					Runnable realizeNow = () -> {};
					return new VariantViewAdapter<>(new ViewAdapter<>(elementType, new ModelNodeBackedViewStrategy(it -> namer.determineName((Variant) it), variants(project), project.getProviders(), project.getObjects(), realizeNow, identifier)));
				}
			};

			@Override
			public void accept(DecoratorHandlers.NestedObjectContext context) {
				if (context.getNestedType().isSubtypeOf(VariantView.class)) {
					final Class<? extends Variant> elementType = (Class<? extends Variant>) ((ParameterizedType) context.getNestedType().getType()).getActualTypeArguments()[0];
					context.mixIn(variantsFactory.create(elementType));
				}
			}
		});

		project.getExtensions().getByType(DecoratorHandlers.class).nestedObject(new Consumer<DecoratorHandlers.NestedObjectContext>() {
			private final DimensionPropertyRegistrationFactory dimensionPropertyFactory = new DimensionPropertyRegistrationFactory(project.getObjects());
			private final Factory<DefaultVariantDimensions> dimensionsFactory = () -> {
				return instantiator(project).newInstance(DefaultVariantDimensions.class, dimensionPropertyFactory);
			};

			@Override
			public void accept(DecoratorHandlers.NestedObjectContext context) {
				if (context.getNestedType().isSubtypeOf(VariantDimensions.class)) {
					context.mixIn(dimensionsFactory.create());
				}
			}
		});

		project.getExtensions().getByType(DecoratorHandlers.class).nestedObject(context -> {
			if (context.getNestedType().isSubtypeOf(ComponentDependencies.class)) {
				final Class<?> type = context.getNestedType().getRawType();
				ModelObjectIdentifier identifier = context.getIdentifier();
				if (!context.getAnnotation().value().isEmpty()) {
					identifier = identifier.child(context.getAnnotation().value());
				}

				final ModelObjectIdentifier id = identifier;
				if (Arrays.stream(type.getConstructors()).anyMatch(it -> it.getParameterCount() == 0)) {
					context.mixIn(ModelMixInSupport.newInstance(identifier, () -> instantiator(project).newInstance(type)));
				} else if (Arrays.stream(type.getConstructors()).anyMatch(it -> it.getParameterCount() == 1)) {
					context.mixIn(ModelMixInSupport.newInstance(identifier, () -> instantiator(project).newInstance(type, id)));
				} else if (Arrays.stream(type.getConstructors()).anyMatch(it -> it.getParameterCount() == 2)) {
					context.mixIn(ModelMixInSupport.newInstance(identifier, () -> instantiator(project).newInstance(type, id, model(project, registryOf(DependencyBucket.class)))));
				}
			}
		});
		project.getExtensions().getByType(DecoratorHandlers.class).nestedObject(new ModelObjectDecorator<>(model(project, registryOf(Task.class))));
		project.getExtensions().getByType(DecoratorHandlers.class).nestedObject(new ModelObjectDecorator<>(model(project, registryOf(DependencyBucket.class))));
		project.getExtensions().getByType(DecoratorHandlers.class).nestedObject(new ModelObjectDecorator<>(model(project, registryOf(Artifact.class))));

		model(project, objects()).configureEach(HasBaseName.class, new BaseNameConfigurationRule(project.getProviders()));
		model(project, mapOf(Component.class)).configureEach(HasDevelopmentBinary.class, new DevelopmentBinaryConventionRule(project.getProviders()));
	}

	private static final class ModelObjectDecorator<T> implements Consumer<DecoratorHandlers.NestedObjectContext> {
		private final ModelObjectRegistry<T> registry;
		private final Function<String, ElementName> namer;

		public ModelObjectDecorator(ModelObjectRegistry<T> registry) {
			this.registry = registry;

			if (registry.getRegistrableTypes().canRegisterType(Task.class)) {
				this.namer = propertyName -> {
					String taskName = propertyName;
					if (taskName.endsWith("Task")) {
						taskName = taskName.substring(0, taskName.length() - "Task".length());
					}
					return TaskName.of(taskName);
				};
			} else {
				this.namer = ElementName::of;
			}
		}

		@Override
		@SuppressWarnings("unchecked")
		public void accept(DecoratorHandlers.NestedObjectContext context) {
			Class<? extends T> objectType = null;
			Function<ModelObject<? extends T>, Object> mapper = null;
			if (context.getNestedType().isSubtypeOf(NamedDomainObjectProvider.class)) {
				final Class<?> candidateType = (Class<?>) ((ParameterizedType) context.getNestedType().getType()).getActualTypeArguments()[0];
				if (registry.getRegistrableTypes().canRegisterType(candidateType)) {
					objectType = (Class<? extends T>) candidateType;
					mapper = ModelObject::asProvider;
				}
			} else if (context.getNestedType().isSubtypeOf(ModelObject.class)) {
				final Class<?> candidateType = (Class<?>) ((ParameterizedType) context.getNestedType().getType()).getActualTypeArguments()[0];
				if (registry.getRegistrableTypes().canRegisterType(candidateType)) {
					objectType = (Class<? extends T>) candidateType;
					mapper = it -> it;
				}
			} else if (registry.getRegistrableTypes().canRegisterType(context.getNestedType().getConcreteType())) {
				objectType = (Class<? extends T>) context.getNestedType().getConcreteType();
				mapper = ModelObject::get;
			}

			if (objectType != null) {
				final ElementName elementName = namer.apply(context.getPropertyName());
				context.mixIn(mapper.apply(registry.register(context.getIdentifier().child(elementName), objectType)));
			}
		}
	}
}
