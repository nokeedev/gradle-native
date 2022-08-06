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
package dev.nokee.language.c.internal.plugins;

import dev.nokee.language.base.internal.SourcePropertyComponent;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.internal.CSourceSetTag;
import dev.nokee.language.c.internal.CSourcesComponent;
import dev.nokee.language.c.internal.CSourcesPropertyComponent;
import dev.nokee.language.c.internal.HasCSourcesMixIn;
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.nativebase.internal.LanguageNativeBasePlugin;
import dev.nokee.language.nativebase.internal.NativeCompileTypeComponent;
import dev.nokee.language.nativebase.internal.NativeHeaderLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.NativeLanguageRegistrationFactory;
import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAwareTag;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
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
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.scripts.DefaultImporter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;

import java.util.Collections;
import java.util.concurrent.Callable;

import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static dev.nokee.utils.Optionals.stream;
import static dev.nokee.utils.ProviderUtils.disallowChanges;

public class CLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(NativeHeaderLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		DefaultImporter.forProject(project)
			.defaultImport(CSourceSet.class);

		// No need to register anything as CHeaderSet and CSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.

		val registrationFactory = new DefaultCSourceSetRegistrationFactory();
		project.getExtensions().add("__nokee_defaultCSourceSetFactory", registrationFactory);
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(CSourceSetSpec.Tag.class), (entity, ignored) -> {
			entity.addComponent(new NativeCompileTypeComponent(CCompileTask.class));
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(NativeLanguageSourceSetAwareTag.class), ModelComponentReference.of(ParentComponent.class), (entity, identifier, tag, parent) -> {
			ParentUtils.stream(parent).filter(it -> it.hasComponent(typeOf(SupportCSourceSetTag.class))).findFirst().ifPresent(ignored -> {
				val sourceSet = project.getExtensions().getByType(ModelRegistry.class).register(registrationFactory.create(entity));
				entity.addComponent(new CSourceSetComponent(ModelNodes.of(sourceSet)));
			});
		})));

		// ComponentFromEntity<GradlePropertyComponent> read-write on CSourcesPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(CSourcesPropertyComponent.class), ModelComponentReference.of(FullyQualifiedNameComponent.class), (entity, cSources, fullyQualifiedName) -> {
			((ConfigurableFileCollection) cSources.get().get(GradlePropertyComponent.class).get()).from("src/" + fullyQualifiedName.get() + "/c");
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelTags.referenceOf(HasCSourcesMixIn.Tag.class), (entity, ignored) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);
			val property = ModelStates.register(registry.instantiate(ModelRegistration.builder()
				.withComponent(new ElementNameComponent("cSources"))
				.withComponent(new ParentComponent(entity))
				.mergeFrom(ModelPropertyRegistrationFactory.fileCollectionProperty())
				.build()));
			entity.addComponent(new CSourcesPropertyComponent(property));
		})));
		// ComponentFromEntity<GradlePropertyComponent> read-write on SourcePropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(CSourceSetTag.class), ModelComponentReference.of(SourcePropertyComponent.class), ModelComponentReference.of(ParentComponent.class), (entity, ignored1, source, parent) -> {
			((ConfigurableFileCollection) source.get().get(GradlePropertyComponent.class).get()).from((Callable<?>) () -> {
				ModelStates.finalize(parent.get());
				return ParentUtils.stream(parent).flatMap(it -> stream(it.find(CSourcesComponent.class))).findFirst()
					.map(it -> (Object) it.get()).orElse(Collections.emptyList());
			});
		}));
		// ComponentFromEntity<GradlePropertyComponent> read-write on CppSourcesPropertyComponent
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(CSourcesPropertyComponent.class), ModelComponentReference.of(ModelState.IsAtLeastFinalized.class), (entity, cSources, ignored1) -> {
			ModelStates.finalize(cSources.get());
			val sources = (ConfigurableFileCollection) cSources.get().get(GradlePropertyComponent.class).get();
			// Note: We should be able to use finalizeValueOnRead but Gradle discard task dependencies
			entity.addComponent(new CSourcesComponent(/*finalizeValueOnRead*/(disallowChanges(sources))));
		}));
	}

	static final class DefaultCSourceSetRegistrationFactory implements NativeLanguageRegistrationFactory {
		@Override
		public ModelRegistration create(ModelNode owner) {
			return DomainObjectEntities.newEntity("c", CSourceSetSpec.class, it -> it.ownedBy(owner).displayName("C sources"));
		}
	}
}
