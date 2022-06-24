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

import dev.nokee.language.nativebase.internal.LanguageNativeBasePlugin;
import dev.nokee.language.nativebase.internal.NativeCompileTypeComponent;
import dev.nokee.language.nativebase.internal.NativeLanguageRegistrationFactory;
import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAwareTag;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.DomainObjectEntities;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.core.ParentUtils;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.scripts.DefaultImporter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

import static dev.nokee.model.internal.tags.ModelTags.typeOf;

public class SwiftLanguageBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(LanguageNativeBasePlugin.class);
		project.getPluginManager().apply(SwiftCompilerPlugin.class);

		DefaultImporter.forProject(project).defaultImport(SwiftSourceSet.class);

		// No need to register anything as ObjectiveCSourceSet are managed instance compatible,
		//   but don't depend on this behaviour.

		project.getExtensions().getByType(ModelConfigurer.class).configure(new ImportModulesConfigurationRegistrationAction(project.getExtensions().getByType(ModelRegistry.class), project.getExtensions().getByType(ResolvableDependencyBucketRegistrationFactory.class), project.getObjects()));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new AttachImportModulesToCompileTaskRule(project.getExtensions().getByType(ModelRegistry.class)));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new SwiftCompileTaskDefaultConfigurationRule(project.getExtensions().getByType(ModelRegistry.class)));

		val registrationFactory = new DefaultSwiftSourceSetRegistrationFactory();
		project.getExtensions().add("__nokee_defaultSwiftFactory", registrationFactory);
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelTags.referenceOf(SwiftSourceSetSpec.Tag.class), (entity, ignored) -> {
			entity.addComponent(new NativeCompileTypeComponent(SwiftCompileTask.class));
		}));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(NativeLanguageSourceSetAwareTag.class), ModelComponentReference.of(ParentComponent.class), (entity, identifier, tag, parent) -> {
			ParentUtils.stream(parent).filter(it -> it.hasComponent(typeOf(SwiftSourceSetTag.class))).findFirst().ifPresent(ignored -> {
				val sourceSet = project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(DefaultSwiftSourceSetRegistrationFactory.class).create(entity));
				entity.addComponent(new SwiftSourceSetComponent(ModelNodes.of(sourceSet)));
			});
		})));
	}

	static final class DefaultSwiftSourceSetRegistrationFactory implements NativeLanguageRegistrationFactory {
		@Override
		public ModelRegistration create(ModelNode owner) {
			return DomainObjectEntities.newEntity("swift", SwiftSourceSetSpec.class).ownedBy(owner).displayName("Swift sources").build();
		}
	}
}
