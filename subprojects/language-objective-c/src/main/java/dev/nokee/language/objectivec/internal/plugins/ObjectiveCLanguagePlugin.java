/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.language.objectivec.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetIdentity;
import dev.nokee.language.nativebase.internal.NativeLanguagePlugin;
import dev.nokee.language.nativebase.internal.NativeLanguageRegistrationFactory;
import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAwareTag;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.language.objectivec.internal.ObjectiveCSourceSetExtensible;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.language.base.internal.SourceSetExtensible.discoveringInstanceOf;
import static dev.nokee.model.internal.core.ModelActions.matching;
import static dev.nokee.model.internal.core.ModelActions.once;

public class ObjectiveCLanguagePlugin implements Plugin<Project>, NativeLanguagePlugin {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ObjectiveCLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		val modelConfigurer = project.getExtensions().getByType(ModelConfigurer.class);
		modelConfigurer.configure(matching(discoveringInstanceOf(ObjectiveCSourceSetExtensible.class), once(ModelActionWithInputs.of(ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(ModelPathComponent.class), (entity, parentEntity, path) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			registry.register(project.getExtensions().getByType(ObjectiveCSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(parentEntity.get().get(IdentifierComponent.class).get(), "objectiveC"), true));
		}))));

		project.getExtensions().add("__nokee_defaultObjectiveCFactory", new DefaultObjectiveCSourceSetRegistrationFactory(project.getExtensions().getByType(ObjectiveCSourceSetRegistrationFactory.class)));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(NativeLanguageSourceSetAwareTag.class), (entity, identifier, tag) -> {
			project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(getRegistrationFactoryType()).create(identifier.get()));
		})));
	}

	@Override
	public Class<? extends NativeLanguageRegistrationFactory> getRegistrationFactoryType() {
		return DefaultObjectiveCSourceSetRegistrationFactory.class;
	}

	private static final class DefaultObjectiveCSourceSetRegistrationFactory implements NativeLanguageRegistrationFactory {
		private final ObjectiveCSourceSetRegistrationFactory factory;

		private DefaultObjectiveCSourceSetRegistrationFactory(ObjectiveCSourceSetRegistrationFactory factory) {
			this.factory = factory;
		}

		@Override
		public ModelRegistration create(DomainObjectIdentifier owner) {
			return ModelRegistration.builder().mergeFrom(factory.create(LanguageSourceSetIdentifier.of(owner, LanguageSourceSetIdentity.of("objectiveC", "Objective-C sources")))).withComponent(new DisplayNameComponent("Objective-C sources")).build();
		}
	}
}
