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

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.*;
import dev.nokee.platform.base.internal.*;
import dev.nokee.platform.base.internal.dependencies.ConfigurationBucketRegistryImpl;
import dev.nokee.platform.base.internal.dependencies.DefaultComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketFactoryImpl;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.FrameworkAwareDependencyBucketFactory;
import dev.nokee.platform.nativebase.internal.dependencies.VariantComponentDependencies;
import dev.nokee.platform.nativebase.internal.rules.BuildableDevelopmentVariantConvention;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;

import java.util.Iterator;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import static dev.nokee.model.internal.core.ModelActions.executeUsingProjection;
import static dev.nokee.model.internal.core.ModelNodes.*;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.NodePredicate.allDirectDescendants;
import static dev.nokee.model.internal.core.NodePredicate.self;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.maven;
import static dev.nokee.platform.base.internal.LanguageSourceSetConventionSupplier.withConventionOf;
import static dev.nokee.platform.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static dev.nokee.platform.nativebase.internal.plugins.NativeComponentBasePlugin.nativeLibraryProjection;
import static dev.nokee.platform.nativebase.internal.plugins.NativeLibraryPlugin.nativeLibraryVariant;
import static dev.nokee.utils.TransformerUtils.transformEach;

public final class NativeLibraryComponentModelRegistrationFactory {
	private final Class<? extends Component> componentType;
	private final BiConsumer<? super ModelNode, ? super ModelPath> sourceRegistration;
	private final Project project;

	public NativeLibraryComponentModelRegistrationFactory(Class<? extends Component> componentType, Project project, BiConsumer<? super ModelNode, ? super ModelPath> sourceRegistration) {
		this.componentType = componentType;
		this.sourceRegistration = sourceRegistration;
		this.project = project;
	}

	public NodeRegistration create(String name) {
		return NodeRegistration.of(name, of(componentType))
			// TODO: Should configure FileCollection on CApplication
			//   and link FileCollection to source sets
			.action(allDirectDescendants(mutate(of(LanguageSourceSet.class)))
				.apply(executeUsingProjection(of(LanguageSourceSet.class), withConventionOf(maven(ComponentName.of(name)))::accept)))
			.withComponent(IsComponent.tag())
			.withComponent(createdUsing(of(DefaultNativeLibraryComponent.class), nativeLibraryProjection(name, project)))
			.action(self(discover()).apply(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), (entity, path) -> {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);

				sourceRegistration.accept(entity, path);

				// TODO: Should be created as ModelProperty (readonly) with VariantView<NativeLibrary> projection
				val identifier = ComponentIdentifier.of(ComponentName.of(name), DefaultNativeLibraryComponent.class, ProjectIdentifier.of(project));
				val dependencyContainer = project.getObjects().newInstance(DefaultComponentDependencies.class, identifier, new FrameworkAwareDependencyBucketFactory(project.getObjects(), new DependencyBucketFactoryImpl(new ConfigurationBucketRegistryImpl(project.getConfigurations()), project.getDependencies())));
				val dependencies = project.getObjects().newInstance(DefaultNativeLibraryComponentDependencies.class, dependencyContainer);
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("dependencies"))
					.withComponent(IsModelProperty.tag())
					.withComponent(ModelProjections.ofInstance(dependencies))
					.build());

				val api = registry.register(ModelRegistration.builder()
					.withComponent(path.child("api"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getApi().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getApi()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getApi().getAsConfiguration().getName())))
					.build());
				val implementation = registry.register(ModelRegistration.builder()
					.withComponent(path.child("implementation"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getImplementation().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getImplementation()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getImplementation().getAsConfiguration().getName())))
					.build());
				val compileOnly = registry.register(ModelRegistration.builder()
					.withComponent(path.child("compileOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getCompileOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getCompileOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getCompileOnly().getAsConfiguration().getName())))
					.build());
				val linkOnly = registry.register(ModelRegistration.builder()
					.withComponent(path.child("linkOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getLinkOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getLinkOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getLinkOnly().getAsConfiguration().getName())))
					.build());
				val runtimeOnly = registry.register(ModelRegistration.builder()
					.withComponent(path.child("runtimeOnly"))
					.withComponent(IsDependencyBucket.tag())
					.withComponent(createdUsing(of(Configuration.class), () -> dependencies.getRuntimeOnly().getAsConfiguration()))
					.withComponent(createdUsing(of(DependencyBucket.class), () -> dependencies.getRuntimeOnly()))
					.withComponent(createdUsing(of(NamedDomainObjectProvider.class), () -> project.getConfigurations().named(dependencies.getRuntimeOnly().getAsConfiguration().getName())))
					.build());
				registry.register(propertyFactory.create(path.child("dependencies").child("api"), ModelNodes.of(api)));
				registry.register(propertyFactory.create(path.child("dependencies").child("implementation"), ModelNodes.of(implementation)));
				registry.register(propertyFactory.create(path.child("dependencies").child("compileOnly"), ModelNodes.of(compileOnly)));
				registry.register(propertyFactory.create(path.child("dependencies").child("linkOnly"), ModelNodes.of(linkOnly)));
				registry.register(propertyFactory.create(path.child("dependencies").child("runtimeOnly"), ModelNodes.of(runtimeOnly)));

				registry.register(project.getExtensions().getByType(ComponentVariantsPropertyRegistrationFactory.class).create(path.child("variants"), NativeLibrary.class));

				// TODO: Should be created as ModelProperty (readonly) with BinaryView<Binary> projection
				registry.register(ModelRegistration.builder()
					.withComponent(path.child("binaries"))
					.withComponent(IsModelProperty.tag())
					.withComponent(createdUsing(of(BinaryView.class), () -> new BinaryViewAdapter<>(new ViewAdapter<>(Binary.class, new ModelNodeBackedViewStrategy(project.getProviders(), () -> ModelStates.finalize(entity))))))
					.build());
			})))
			.action(self(stateOf(ModelState.Finalized)).apply(new CalculateNativeApplicationVariantAction(project)))
			;
	}

	private static class CalculateNativeApplicationVariantAction extends ModelActionWithInputs.ModelAction1<ModelPath> {
		private final Project project;

		private CalculateNativeApplicationVariantAction(Project project) {
			this.project = project;
		}

		@Override
		protected void execute(ModelNode entity, ModelPath path) {
			val component = ModelNodeUtils.get(entity, ModelType.of(DefaultNativeLibraryComponent.class));
			component.finalizeExtension(null);
			component.getDevelopmentVariant().convention(project.provider(new BuildableDevelopmentVariantConvention<>(() -> component.getVariants().get())));

			val variants = ImmutableMap.<BuildVariant, ModelNode>builder();
			component.getBuildVariants().get().forEach(new Consumer<BuildVariantInternal>() {
				private final ModelLookup modelLookup = project.getExtensions().getByType(ModelLookup.class);

				@Override
				public void accept(BuildVariantInternal buildVariant) {
					val variantIdentifier = VariantIdentifier.builder().withBuildVariant(buildVariant).withComponentIdentifier(component.getIdentifier()).withType(DefaultNativeLibraryVariant.class).build();
					val variant = ModelNodeUtils.register(entity, nativeLibraryVariant(variantIdentifier, component, project));

					variants.put(buildVariant, ModelNodes.of(variant));
					onEachVariantDependencies(variant.as(NativeLibrary.class), ModelNodes.of(variant).getComponent(ModelComponentType.componentOf(VariantComponentDependencies.class)));
				}

				private void onEachVariantDependencies(DomainObjectProvider<NativeLibrary> variant, VariantComponentDependencies<?> dependencies) {
					if (NativeLibrary.class.isAssignableFrom(DefaultNativeLibraryVariant.class)) {
						if (modelLookup.anyMatch(ModelSpecs.of(withType(ModelType.of(SwiftSourceSet.class))))) {
							dependencies.getOutgoing().getExportedSwiftModule().convention(variant.flatMap(it -> {
								List<? extends Provider<RegularFile>> result = it.getBinaries().withType(NativeBinary.class).flatMap(binary -> {
									List<? extends Provider<RegularFile>> modules = binary.getCompileTasks().withType(SwiftCompileTask.class).map(task -> task.getModuleFile()).get();
									return modules;
								}).get();
								return one(result);
							}));
						}
						dependencies.getOutgoing().getExportedHeaders().from(sourceViewOf(component).filter(it -> (it instanceof NativeHeaderSet) && it.getName().equals("public")).map(transformEach(LanguageSourceSet::getSourceDirectories)));
					}
					dependencies.getOutgoing().getExportedBinary().convention(variant.flatMap(it -> it.getDevelopmentBinary()));
				}

				private <T> T one(Iterable<T> c) {
					Iterator<T> iterator = c.iterator();
					Preconditions.checkArgument(iterator.hasNext(), "collection needs to have one element, was empty");
					T result = iterator.next();
					Preconditions.checkArgument(!iterator.hasNext(), "collection needs to only have one element, more than one element found");
					return result;
				}
			});
			entity.addComponent(new NativeLibraryComponentVariants(variants.build()));
		}
	}
}
