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
package dev.nokee.language.objectivecpp.internal.plugins;

import dev.nokee.language.base.internal.SourcePropertyComponent;
import dev.nokee.language.nativebase.internal.HasPrivateHeadersMixIn;
import dev.nokee.language.nativebase.internal.LanguageNativeBasePlugin;
import dev.nokee.language.nativebase.internal.NativeCompileTypeComponent;
import dev.nokee.language.nativebase.internal.NativeHeaderLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.NativeLanguageRegistrationFactory;
import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAwareTag;
import dev.nokee.language.nativebase.internal.NativeSourcesAwareTag;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivecpp.ObjectiveCppSourceSet;
import dev.nokee.language.objectivecpp.internal.HasObjectiveCppSourcesMixIn;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourceSetTag;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourcesComponent;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourcesPropertyComponent;
import dev.nokee.language.objectivecpp.internal.tasks.ObjectiveCppCompileTask;
import dev.nokee.model.internal.core.GradlePropertyComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
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
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.internal.DomainObjectEntities;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareComponent;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.scripts.DefaultImporter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;

import java.util.Collections;
import java.util.concurrent.Callable;

import static dev.nokee.language.nativebase.internal.SupportLanguageSourceSet.has;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static dev.nokee.utils.Optionals.stream;
import static dev.nokee.utils.ProviderUtils.disallowChanges;

public class ObjectiveCppLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(NativeHeaderLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		DefaultImporter.forProject(project)
			.defaultImport(ObjectiveCppSourceSet.class);

		// No need to register anything as ObjectiveCSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.

		val registrationFactory = new DefaultObjectiveCppSourceSetRegistrationFactory();
		project.getExtensions().add("__nokee_defaultObjectiveCppFactory", registrationFactory);
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(ObjectiveCppSourceSetSpec.Tag.class), (entity, ignored) -> {
			entity.addComponent(new NativeCompileTypeComponent(ObjectiveCppCompileTask.class));
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(NativeLanguageSourceSetAwareTag.class), ModelComponentReference.of(ParentComponent.class), (entity, identifier, tag, parent) -> {
			ParentUtils.stream(parent).filter(it -> it.hasComponent(typeOf(SupportObjectiveCppSourceSetTag.class))).findFirst().ifPresent(ignored -> {
				val sourceSet = project.getExtensions().getByType(ModelRegistry.class).register(registrationFactory.create(entity));
				entity.addComponent(new ObjectiveCppSourceSetComponent(ModelNodes.of(sourceSet)));
			});
		})));

		// ComponentFromEntity<GradlePropertyComponent> read-write on ObjectiveCppSourcesPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ObjectiveCppSourcesPropertyComponent.class), ModelComponentReference.of(FullyQualifiedNameComponent.class), (entity, objcppSources, fullyQualifiedName) -> {
			((ConfigurableFileCollection) objcppSources.get().get(GradlePropertyComponent.class).get()).from("src/" + fullyQualifiedName.get() + "/objectiveCpp", "src/" + fullyQualifiedName.get() + "/objcpp");
		}));
		// ComponentFromEntity<GradlePropertyComponent> read-write on ObjectiveCppSourcesPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ObjectiveCppSourcesPropertyComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, objcppSources, parent) -> {
			((ConfigurableFileCollection) objcppSources.get().get(GradlePropertyComponent.class).get()).from((Callable<?>) () -> {
				return ParentUtils.stream(parent).map(ModelStates::finalize).flatMap(it -> stream(it.find(ObjectiveCppSourcesComponent.class))).findFirst().map(it -> (Object) it.get()).orElse(Collections.emptyList());
			});
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(HasObjectiveCppSourcesMixIn.Tag.class), (entity, ignored) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val property = ModelStates.register(registry.instantiate(ModelRegistration.builder()
				.withComponent(new ElementNameComponent("objectiveCppSources"))
				.withComponent(new ParentComponent(entity))
				.mergeFrom(ModelPropertyRegistrationFactory.fileCollectionProperty())
				.build()));
			entity.addComponent(new ObjectiveCppSourcesPropertyComponent(property));
		})));
		// ComponentFromEntity<GradlePropertyComponent> read-write on SourcePropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(ObjectiveCppSourceSetTag.class), ModelComponentReference.of(SourcePropertyComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, ignored1, source, parent) -> {
			((ConfigurableFileCollection) source.get().get(GradlePropertyComponent.class).get()).from((Callable<?>) () -> {
				ModelStates.finalize(parent.get());
				return ParentUtils.stream(parent).flatMap(it -> stream(it.find(ObjectiveCppSourcesComponent.class))).findFirst()
					.map(it -> (Object) it.get()).orElse(Collections.emptyList());
			});
		}));
		// ComponentFromEntity<GradlePropertyComponent> read-write on ObjectiveCppSourcesPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ObjectiveCppSourcesPropertyComponent.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), (entity, swiftSources, ignored1) -> {
			ModelStates.finalize(swiftSources.get());
			val sources = (ConfigurableFileCollection) swiftSources.get().get(GradlePropertyComponent.class).get();
			// Note: We should be able to use finalizeValueOnRead but Gradle discard task dependencies
			entity.addComponent(new ObjectiveCppSourcesComponent(/*finalizeValueOnRead*/(disallowChanges(sources))));
		}));
		// ComponentFromEntity<GradlePropertyComponent> read-write on ObjectiveCppSourcesPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ObjectiveCppSourcesPropertyComponent.class), ModelComponentReference.of(ExtensionAwareComponent.class), (entity, objcppSources, extensions) -> {
			extensions.get().add(ConfigurableFileCollection.class, "objectiveCppSources", (ConfigurableFileCollection) objcppSources.get().get(GradlePropertyComponent.class).get());
		}));

		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(NativeSourcesAwareTag.class), ModelComponentReference.of(ParentComponent.class), (entity, ignored, parent) -> {
			ParentUtils.stream(parent).filter(has(SupportObjectiveCppSourceSetTag.class)).findFirst().ifPresent(__ -> {
				entity.addComponent(tag(SupportObjectiveCppSourceSetTag.class));
			});
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(NativeSourcesAwareTag.class), ModelTags.referenceOf(SupportObjectiveCppSourceSetTag.class), (entity, ignored1, ignored2) -> {
			entity.addComponent(tag(HasObjectiveCppSourcesMixIn.Tag.class));
			entity.addComponent(tag(HasPrivateHeadersMixIn.Tag.class));
		}));
	}

	static final class DefaultObjectiveCppSourceSetRegistrationFactory implements NativeLanguageRegistrationFactory {
		@Override
		public ModelRegistration create(ModelNode owner) {
			return DomainObjectEntities.newEntity("objectiveCpp", ObjectiveCppSourceSetSpec.class, it -> it.ownedBy(owner).displayName("Objective-C++ sources"));
		}
	}
}
