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
package dev.nokee.platform.nativebase.internal;

import com.google.common.collect.ImmutableMap;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.model.DependencyFactory;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.PolymorphicDomainObjectRegistry;
import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.ExcludeFromQualifyingNameTag;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.ComponentName;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.Variants;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.dependencies.DefaultDependencyBucketFactory;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.dependencybuckets.CompileOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.ImplementationConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.LinkOnlyConfigurationComponent;
import dev.nokee.platform.base.internal.dependencybuckets.RuntimeOnlyConfigurationComponent;
import dev.nokee.platform.nativebase.NativeApplication;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import dev.nokee.platform.nativebase.internal.rules.RegisterAssembleLifecycleTaskRule;
import lombok.val;
import org.gradle.api.Project;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.nokee.language.base.internal.LanguageSourceSetConventionSupplier.maven;
import static dev.nokee.language.base.internal.LanguageSourceSetConventionSupplier.withConventionOf;
import static dev.nokee.model.internal.core.ModelComponentType.componentOf;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.declarable;
import static dev.nokee.platform.nativebase.internal.plugins.NativeApplicationPlugin.nativeApplicationVariant;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.nativeApplicationProjection;

public final class NativeApplicationComponentModelRegistrationFactory {
	private final Class<Component> componentType;
	private final Class<Component> implementationComponentType;
	private final BiConsumer<? super ModelNode, ? super ModelPath> sourceRegistration;
	private final Project project;

	@SuppressWarnings("unchecked")
	public <T extends Component> NativeApplicationComponentModelRegistrationFactory(Class<? super T> componentType, Class<T> implementationComponentType, Project project, BiConsumer<? super ModelNode, ? super ModelPath> sourceRegistration) {
		this.componentType = (Class<Component>) componentType;
		this.implementationComponentType = (Class<Component>) implementationComponentType;
		this.sourceRegistration = sourceRegistration;
		this.project = project;
	}

	public ModelRegistration create(ComponentIdentifier identifier) {
		val entityPath = ModelPath.path(identifier.getName().get());
		val name = entityPath.getName();
		val builder =  ModelRegistration.builder()
			.withComponent(new ModelPathComponent(entityPath))
			.withComponent(createdUsing(of(implementationComponentType), () -> project.getObjects().newInstance(implementationComponentType)))
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(IsComponent.tag())
			.withComponent(ConfigurableTag.tag())
			.withComponent(NativeApplicationTag.tag())
			// TODO: Should configure FileCollection on CApplication
			//   and link FileCollection to source sets
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.ofProjection(LanguageSourceSet.class), ModelComponentReference.of(ModelState.IsAtLeastRealized.class), (entity, path, sourceSet, ignored) -> {
				if (entityPath.isDescendant(path.get())) {
					withConventionOf(maven(ComponentName.of(name))).accept(ModelNodeUtils.get(entity, LanguageSourceSet.class));
				}
			}))
			.withComponent(createdUsing(of(DefaultNativeApplicationComponent.class), nativeApplicationProjection(name, project)))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(ModelState.class), new ModelActionWithInputs.A2<ModelPathComponent, ModelState>() {
				private boolean alreadyExecuted = false;
				@Override
				public void execute(ModelNode entity, ModelPathComponent path, ModelState state) {
					if (entityPath.equals(path.get()) && state.equals(ModelState.Registered) && !alreadyExecuted) {
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

						registry.register(project.getExtensions().getByType(ModelPropertyRegistrationFactory.class).createProperty(ModelPropertyIdentifier.of(identifier, "developmentVariant"), DefaultNativeApplicationVariant.class));
					}
				}
			}))
			.action(new RegisterAssembleLifecycleTaskRule(identifier, PolymorphicDomainObjectRegistry.of(project.getTasks()), project.getExtensions().getByType(ModelRegistry.class), project.getProviders()))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(ModelState.class), new ModelActionWithInputs.A2<ModelPathComponent, ModelState>() {
				private boolean alreadyExecuted = false;
				@Override
				@SuppressWarnings("unchecked")
				public void execute(ModelNode entity, ModelPathComponent path, ModelState state) {
					if (entityPath.equals(path.get()) && state.equals(ModelState.Registered) && !alreadyExecuted) {
						alreadyExecuted = true;
						ModelNodeUtils.get(entity, BaseComponent.class).getBaseName().convention(path.get().getName());
					}
				}
			}))
			.action(ModelActionWithInputs.of(ModelComponentReference.of(ModelPathComponent.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), (entity, path, ignored) -> {
				if (entityPath.equals(path.get())) {
					new CalculateNativeApplicationVariantAction(project).execute(entity, path);
				}
			}));

		if (identifier.isMainComponent()) {
			builder.withComponent(ExcludeFromQualifyingNameTag.tag());
		}

		return builder.build();
	}

	private static class CalculateNativeApplicationVariantAction extends ModelActionWithInputs.ModelAction1<ModelPathComponent> {
		private final Project project;

		public CalculateNativeApplicationVariantAction(Project project) {
			this.project = project;
		}

		@Override
		protected void execute(ModelNode entity, ModelPathComponent path) {
			val component = ModelNodeUtils.get(entity, ModelType.of(DefaultNativeApplicationComponent.class));

			val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
			component.getBuildVariants().get().forEach(new Consumer<BuildVariant>() {
				@Override
				public void accept(BuildVariant buildVariant) {
					val variantIdentifier = VariantIdentifier.builder().withBuildVariant((BuildVariantInternal) buildVariant).withComponentIdentifier(component.getIdentifier()).build();
					val variant = ModelNodeUtils.register(entity, nativeApplicationVariant(variantIdentifier, component, project));

					variants.put(buildVariant, ModelNodes.of(variant));
					onEachVariantDependencies(variant.as(NativeApplication.class), ModelNodes.of(variant).getComponent(componentOf(VariantComponentDependencies.class)));
				}

				private void onEachVariantDependencies(DomainObjectProvider<NativeApplication> variant, VariantComponentDependencies<?> dependencies) {
					dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
				}
			});
			entity.addComponent(new Variants(variants.build()));

			component.finalizeExtension(null);
			component.getDevelopmentVariant().set(project.getProviders().provider(new BuildableDevelopmentVariantConvention<>(() -> component.getVariants().get()))); // TODO: VariantView#get should force finalize the component.
		}
	}
}
