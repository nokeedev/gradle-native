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
package dev.nokee.language.swift.internal.plugins;

import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetIdentity;
import dev.nokee.language.nativebase.internal.NativeLanguagePlugin;
import dev.nokee.language.nativebase.internal.NativeLanguageRegistrationFactory;
import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAwareTag;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.nativeplatform.toolchain.plugins.SwiftCompilerPlugin;

public class SwiftLanguagePlugin implements Plugin<Project>, NativeLanguagePlugin {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(SwiftLanguageBasePlugin.class);
		project.getPluginManager().apply(SwiftCompilerPlugin.class);

		project.getExtensions().add("__nokee_defaultSwiftFactory", new DefaultSwiftSourceSetRegistrationFactory(project.getExtensions().getByType(SwiftSourceSetRegistrationFactory.class)));
		project.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(ModelActionWithInputs.of(ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(NativeLanguageSourceSetAwareTag.class), (entity, identifier, tag) -> {
			val sourceSet = project.getExtensions().getByType(ModelRegistry.class).register(project.getExtensions().getByType(getRegistrationFactoryType()).create(identifier.get()));
			entity.addComponent(new SwiftSourceSetComponent(ModelNodes.of(sourceSet)));
		})));
	}

	@Override
	public Class<? extends NativeLanguageRegistrationFactory> getRegistrationFactoryType() {
		return DefaultSwiftSourceSetRegistrationFactory.class;
	}

	private static final class DefaultSwiftSourceSetRegistrationFactory implements NativeLanguageRegistrationFactory {
		private final SwiftSourceSetRegistrationFactory factory;

		private DefaultSwiftSourceSetRegistrationFactory(SwiftSourceSetRegistrationFactory factory) {
			this.factory = factory;
		}

		@Override
		public ModelRegistration create(DomainObjectIdentifier owner) {
			return ModelRegistration.builder().mergeFrom(factory.create(LanguageSourceSetIdentifier.of(owner, LanguageSourceSetIdentity.of("swift", "Swift sources")))).withComponent(new DisplayNameComponent("Swift sources")).build();
		}
	}
}
