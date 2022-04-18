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
package dev.nokee.language.c.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetIdentity;
import dev.nokee.language.c.internal.CSourceSetExtensible;
import dev.nokee.language.nativebase.internal.NativeLanguagePlugin;
import dev.nokee.language.nativebase.internal.NativeLanguageRegistrationFactory;
import dev.nokee.language.nativebase.internal.toolchains.NokeeStandardToolChainsPlugin;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelPathComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.language.base.internal.SourceSetExtensible.discoveringInstanceOf;
import static dev.nokee.model.internal.core.ModelActions.matching;
import static dev.nokee.model.internal.core.ModelActions.once;

public class CLanguagePlugin implements Plugin<Project>, NativeLanguagePlugin {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(CLanguageBasePlugin.class);
		project.getPluginManager().apply(NokeeStandardToolChainsPlugin.class);

		val modelConfigurer = project.getExtensions().getByType(ModelConfigurer.class);
		modelConfigurer.configure(matching(discoveringInstanceOf(CSourceSetExtensible.class), once(ModelActionWithInputs.of(ModelComponentReference.of(ParentComponent.class), ModelComponentReference.of(ModelPathComponent.class), (entity, parentEntity, path) -> {
			val registry = project.getExtensions().getByType(ModelRegistry.class);

			registry.register(project.getExtensions().getByType(CSourceSetRegistrationFactory.class).create(LanguageSourceSetIdentifier.of(parentEntity.get().getComponent(DomainObjectIdentifier.class), "c"), true));
		}))));

		project.getExtensions().add("__nokee_defaultCSourceSetFactory", new DefaultCSourceSetRegistrationFactory(project.getExtensions().getByType(CSourceSetRegistrationFactory.class)));
	}

	@Override
	public Class<? extends NativeLanguageRegistrationFactory> getRegistrationFactoryType() {
		return DefaultCSourceSetRegistrationFactory.class;
	}

	private static final class DefaultCSourceSetRegistrationFactory implements NativeLanguageRegistrationFactory {
		private final CSourceSetRegistrationFactory factory;

		private DefaultCSourceSetRegistrationFactory(CSourceSetRegistrationFactory factory) {
			this.factory = factory;
		}

		@Override
		public ModelRegistration create(DomainObjectIdentifier owner) {
			return factory.create(LanguageSourceSetIdentifier.of(owner, LanguageSourceSetIdentity.of("c", "C sources")));
		}
	}
}
