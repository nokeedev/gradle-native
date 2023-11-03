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
package dev.nokee.language.swift.internal.plugins;

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.language.nativebase.internal.HasPrivateHeadersMixIn;
import dev.nokee.language.nativebase.internal.LanguageNativeBasePlugin;
import dev.nokee.language.nativebase.internal.NativeCompileTypeComponent;
import dev.nokee.language.nativebase.internal.NativeLanguageRegistrationFactory;
import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAwareTag;
import dev.nokee.language.nativebase.internal.NativeSourcesAwareTag;
import dev.nokee.language.nativebase.internal.WireParentSourceToSourceSetAction;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.core.ParentUtils;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.ModelObjectFactory;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.scripts.DefaultImporter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

import java.util.Collections;
import java.util.concurrent.Callable;

import static dev.nokee.language.nativebase.internal.SupportLanguageSourceSet.has;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.factoryRegistryOf;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.variants;
import static dev.nokee.utils.Optionals.stream;
import static dev.nokee.utils.ProviderUtils.disallowChanges;

public class SwiftLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(SwiftCompilerPlugin.class);

		model(project, factoryRegistryOf(LanguageSourceSet.class)).registerFactory(SwiftSourceSetSpec.class, new ModelObjectFactory<SwiftSourceSetSpec>(project, IsLanguageSourceSet.class) {
			@Override
			protected SwiftSourceSetSpec doCreate(String name) {
				return project.getObjects().newInstance(SwiftSourceSetSpec.class);
			}
		});

		DefaultImporter.forProject(project).defaultImport(SwiftSourceSet.class);

		// No need to register anything as ObjectiveCSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.

		project.getExtensions().getByType(ModelConfigurer.class).configure(new ImportModulesConfigurationRegistrationAction(project.getExtensions().getByType(ModelRegistry.class), project.getObjects()));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new AttachImportModulesToCompileTaskRule(project.getExtensions().getByType(ModelRegistry.class)));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new SwiftCompileTaskDefaultConfigurationRule(project.getExtensions().getByType(ModelRegistry.class)));

		val registrationFactory = new DefaultSwiftSourceSetRegistrationFactory();
		project.getExtensions().add("__nokee_defaultSwiftFactory", registrationFactory);
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(SwiftSourceSetSpec.Tag.class), (entity, ignored) -> {
			entity.addComponent(new NativeCompileTypeComponent(SwiftCompileTask.class));
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(NativeLanguageSourceSetAwareTag.class), ModelComponentReference.of(ParentComponent.class), (entity, identifier, tag, parent) -> {
			ParentUtils.stream(parent).filter(it -> it.hasComponent(typeOf(SupportSwiftSourceSetTag.class))).findFirst().ifPresent(ignored -> {
				val sourceSet = project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(DefaultSwiftSourceSetRegistrationFactory.class).create(entity));
				entity.addComponent(new SwiftSourceSetComponent(ModelNodes.of(sourceSet)));
			});
		})));

		// ComponentFromEntity<GradlePropertyComponent> read-write on SwiftSourcesPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(SwiftSourcesPropertyComponent.class), ModelComponentReference.of(FullyQualifiedNameComponent.class), (entity, swiftSources, fullyQualifiedName) -> {
			((ConfigurableFileCollection) swiftSources.get().get(GradlePropertyComponent.class).get()).from("src/" + fullyQualifiedName.get() + "/swift");
		}));
		// ComponentFromEntity<GradlePropertyComponent> read-write on SwiftSourcesPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(SwiftSourcesPropertyComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, swiftSources, parent) -> {
			((ConfigurableFileCollection) swiftSources.get().get(GradlePropertyComponent.class).get()).from((Callable<?>) () -> {
				return ParentUtils.stream(parent).map(ModelStates::finalize).flatMap(it -> stream(it.find(SwiftSourcesComponent.class))).findFirst().map(it -> (Object) it.get()).orElse(Collections.emptyList());
			});
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(HasSwiftSourcesMixIn.Tag.class), (entity, ignored) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val property = ModelStates.register(registry.instantiate(ModelRegistration.builder()
				.withComponent(new ElementNameComponent("swiftSources"))
				.withComponent(new ParentComponent(entity))
				.mergeFrom(ModelPropertyRegistrationFactory.fileCollectionProperty())
				.build()));
			entity.addComponent(new SwiftSourcesPropertyComponent(property));
		})));
		variants(project).configureEach(new WireParentSourceToSourceSetAction<>("swiftSources"));
		// ComponentFromEntity<GradlePropertyComponent> read-write on SwiftSourcesPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(SwiftSourcesPropertyComponent.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), (entity, swiftSources, ignored1) -> {
			ModelStates.finalize(swiftSources.get());
			val sources = (ConfigurableFileCollection) swiftSources.get().get(GradlePropertyComponent.class).get();
			// Note: We should be able to use finalizeValueOnRead but Gradle discard task dependencies
			entity.addComponent(new SwiftSourcesComponent(/*finalizeValueOnRead*/(disallowChanges(sources))));
		}));
		// ComponentFromEntity<GradlePropertyComponent> read-write on SwiftSourcesPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(SwiftSourcesPropertyComponent.class), (entity, swiftSources) -> {
			ModelNodeUtils.get(entity, ExtensionAware.class).getExtensions().add(ConfigurableFileCollection.class, "swiftSources", (ConfigurableFileCollection) swiftSources.get().get(GradlePropertyComponent.class).get());
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(NativeSourcesAwareTag.class), ModelComponentReference.of(ParentComponent.class), (entity, ignored, parent) -> {
			ParentUtils.stream(parent).filter(has(SupportSwiftSourceSetTag.class)).findFirst().ifPresent(__ -> {
				entity.addComponentTag(SupportSwiftSourceSetTag.class);
			});
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(NativeSourcesAwareTag.class), ModelTags.referenceOf(SupportSwiftSourceSetTag.class), (entity, ignored1, ignored2) -> {
			entity.addComponentTag(HasSwiftSourcesMixIn.Tag.class);
			entity.addComponentTag(HasPrivateHeadersMixIn.Tag.class);
		}));
	}

	static final class DefaultSwiftSourceSetRegistrationFactory implements NativeLanguageRegistrationFactory {
		@Override
		public ModelRegistration create(ModelNode owner) {
			return DomainObjectEntities.newEntity(owner.get(IdentifierComponent.class).get().child("swift"), SwiftSourceSetSpec.class, it -> it.ownedBy(owner).displayName("Swift sources"));
		}
	}
}
