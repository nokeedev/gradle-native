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

import dev.nokee.language.base.internal.SourcePropertyComponent;
import dev.nokee.language.nativebase.internal.HasPrivateHeadersMixIn;
import dev.nokee.language.nativebase.internal.LanguageNativeBasePlugin;
import dev.nokee.language.nativebase.internal.NativeCompileTypeComponent;
import dev.nokee.language.nativebase.internal.NativeLanguageRegistrationFactory;
import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAwareTag;
import dev.nokee.language.nativebase.internal.NativeSourcesAwareTag;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
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
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareComponent;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.scripts.DefaultImporter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

import java.util.Collections;
import java.util.concurrent.Callable;

import static dev.nokee.language.nativebase.internal.SupportLanguageSourceSet.has;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static dev.nokee.utils.Optionals.stream;
import static dev.nokee.utils.ProviderUtils.disallowChanges;

public class SwiftLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(SwiftCompilerPlugin.class);

		DefaultImporter.forProject(project).defaultImport(SwiftSourceSet.class);

		// No need to register anything as ObjectiveCSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.

		project.getExtensions().getByType(ModelConfigurer.class).configure(new ImportModulesConfigurationRegistrationAction(project.getExtensions().getByType(ModelRegistry.class), project.getObjects()));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new AttachImportModulesToCompileTaskRule(project.getExtensions().getByType(ModelRegistry.class)));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new SwiftCompileTaskDefaultConfigurationRule(project.getExtensions().getByType(ModelRegistry.class)));

		val registrationFactory = new DefaultSwiftSourceSetRegistrationFactory();
		project.getExtensions().add("__nokee_defaultSwiftFactory", registrationFactory);
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction1<ModelComponentTag<SwiftSourceSetSpec.Tag>>() {
			protected void execute(ModelNode entity, ModelComponentTag<SwiftSourceSetSpec.Tag> ignored) {
				entity.addComponent(new NativeCompileTypeComponent(SwiftCompileTask.class));
			}
		});
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new ModelActionWithInputs.ModelAction3<IdentifierComponent, ModelComponentTag<NativeLanguageSourceSetAwareTag>, ParentComponent>() {
			protected void execute(ModelNode entity, IdentifierComponent identifier, ModelComponentTag<NativeLanguageSourceSetAwareTag> tag, ParentComponent parent) {
				ParentUtils.stream(parent).filter(it -> it.hasComponent(typeOf(SupportSwiftSourceSetTag.class))).findFirst().ifPresent(ignored -> {
					val sourceSet = project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(DefaultSwiftSourceSetRegistrationFactory.class).create(entity));
					entity.addComponent(new SwiftSourceSetComponent(ModelNodes.of(sourceSet)));
				});
			}
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction2<SwiftSourcesPropertyComponent, FullyQualifiedNameComponent>() {
			// ComponentFromEntity<GradlePropertyComponent> read-write on SwiftSourcesPropertyComponent
			protected void execute(ModelNode entity, SwiftSourcesPropertyComponent swiftSources, FullyQualifiedNameComponent fullyQualifiedName) {
				((ConfigurableFileCollection) swiftSources.get().get(GradlePropertyComponent.class).get()).from("src/" + fullyQualifiedName.get() + "/swift");
			}
		});
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction2<SwiftSourcesPropertyComponent, ParentComponent>() {
			// ComponentFromEntity<GradlePropertyComponent> read-write on SwiftSourcesPropertyComponent
			protected void execute(ModelNode entity, SwiftSourcesPropertyComponent swiftSources, ParentComponent parent) {
				((ConfigurableFileCollection) swiftSources.get().get(GradlePropertyComponent.class).get()).from((Callable<?>) () -> {
					return ParentUtils.stream(parent).map(ModelStates::finalize).flatMap(it -> stream(it.find(SwiftSourcesComponent.class))).findFirst().map(it -> (Object) it.get()).orElse(Collections.emptyList());
				});
			}
		});
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new ModelActionWithInputs.ModelAction1<ModelComponentTag<HasSwiftSourcesMixIn.Tag>>() {
			protected void execute(ModelNode entity, ModelComponentTag<HasSwiftSourcesMixIn.Tag> ignored) {
				val registry = project.getExtensions().getByType(ModelRegistry.class);
				val property = ModelStates.register(registry.instantiate(ModelRegistration.builder()
					.withComponent(new ElementNameComponent("swiftSources"))
					.withComponent(new ParentComponent(entity))
					.mergeFrom(ModelPropertyRegistrationFactory.fileCollectionProperty())
					.build()));
				entity.addComponent(new SwiftSourcesPropertyComponent(property));
			}
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction3<ModelComponentTag<SwiftSourceSetTag>, SourcePropertyComponent, ParentComponent>() {
			// ComponentFromEntity<GradlePropertyComponent> read-write on SourcePropertyComponent
			protected void execute(ModelNode entity, ModelComponentTag<SwiftSourceSetTag> ignored1, SourcePropertyComponent source, ParentComponent parent) {
				((ConfigurableFileCollection) source.get().get(GradlePropertyComponent.class).get()).from((Callable<?>) () -> {
					ModelStates.finalize(parent.get());
					return ParentUtils.stream(parent).flatMap(it -> stream(it.find(SwiftSourcesComponent.class))).findFirst()
						.map(it -> (Object) it.get()).orElse(Collections.emptyList());
				});
			}
		});
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction2<SwiftSourcesPropertyComponent, ModelState.IsAtLeastFinalized>() {
			// ComponentFromEntity<GradlePropertyComponent> read-write on SwiftSourcesPropertyComponent
			protected void execute(ModelNode entity, SwiftSourcesPropertyComponent swiftSources, ModelState.IsAtLeastFinalized ignored1) {
				ModelStates.finalize(swiftSources.get());
				val sources = (ConfigurableFileCollection) swiftSources.get().get(GradlePropertyComponent.class).get();
				// Note: We should be able to use finalizeValueOnRead but Gradle discard task dependencies
				entity.addComponent(new SwiftSourcesComponent(/*finalizeValueOnRead*/(disallowChanges(sources))));
			}
		});
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction2<SwiftSourcesPropertyComponent, ExtensionAwareComponent>() {
			// ComponentFromEntity<GradlePropertyComponent> read-write on SwiftSourcesPropertyComponent
			protected void execute(ModelNode entity, SwiftSourcesPropertyComponent swiftSources, ExtensionAwareComponent extensions) {
				extensions.get().add(ConfigurableFileCollection.class, "swiftSources", (ConfigurableFileCollection) swiftSources.get().get(GradlePropertyComponent.class).get());
			}
		});

		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction2<ModelComponentTag<NativeSourcesAwareTag>, ParentComponent>() {
			protected void execute(ModelNode entity, ModelComponentTag<NativeSourcesAwareTag> ignored, ParentComponent parent) {
				ParentUtils.stream(parent).filter(has(SupportSwiftSourceSetTag.class)).findFirst().ifPresent(__ -> {
					entity.addComponentTag(SupportSwiftSourceSetTag.class);
				});
			}
		});
		project.getExtensions().getByType(ModelConfigurer.class).configure(new ModelActionWithInputs.ModelAction2<ModelComponentTag<NativeSourcesAwareTag>, ModelComponentTag<SupportSwiftSourceSetTag>>() {
			protected void execute(ModelNode entity, ModelComponentTag<NativeSourcesAwareTag> ignored1, ModelComponentTag<SupportSwiftSourceSetTag> ignored2) {
				entity.addComponentTag(HasSwiftSourcesMixIn.Tag.class);
				entity.addComponentTag(HasPrivateHeadersMixIn.Tag.class);
			}
		});
	}

	static final class DefaultSwiftSourceSetRegistrationFactory implements NativeLanguageRegistrationFactory {
		@Override
		public ModelRegistration create(ModelNode owner) {
			return DomainObjectEntities.newEntity("swift", SwiftSourceSetSpec.class, it -> it.ownedBy(owner).displayName("Swift sources"));
		}
	}
}
