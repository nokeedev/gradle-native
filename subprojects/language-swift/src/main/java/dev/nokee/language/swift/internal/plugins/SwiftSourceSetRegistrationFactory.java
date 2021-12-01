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
package dev.nokee.language.swift.internal.plugins;

import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetRegistrationFactory;
import dev.nokee.language.base.internal.ModelBackedLanguageSourceSetLegacyMixIn;
import dev.nokee.language.nativebase.internal.NativeCompileTaskRegistrationActionFactory;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.tasks.SwiftCompile;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.type.ModelType;
import lombok.val;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.tasks.TaskProvider;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;

public final class SwiftSourceSetRegistrationFactory {
	private final LanguageSourceSetRegistrationFactory sourceSetRegistrationFactory;
	private final ImportModulesConfigurationRegistrationActionFactory importModulesRegistrationFactory;
	private final NativeCompileTaskRegistrationActionFactory compileTaskRegistrationFactory;
	private final ObjectFactory objectFactory;

	public SwiftSourceSetRegistrationFactory(LanguageSourceSetRegistrationFactory sourceSetRegistrationFactory, ImportModulesConfigurationRegistrationActionFactory importModulesRegistrationFactory, NativeCompileTaskRegistrationActionFactory compileTaskRegistrationFactory, ObjectFactory objectFactory) {
		this.sourceSetRegistrationFactory = sourceSetRegistrationFactory;
		this.importModulesRegistrationFactory = importModulesRegistrationFactory;
		this.compileTaskRegistrationFactory = compileTaskRegistrationFactory;
		this.objectFactory = objectFactory;
	}

	public ModelRegistration create(LanguageSourceSetIdentifier identifier) {
		return create(identifier, false);
	}

	public ModelRegistration create(LanguageSourceSetIdentifier identifier, boolean isLegacy) {
		val builder = sourceSetRegistrationFactory.create(identifier);
		if (!isLegacy) {
			builder.action(compileTaskRegistrationFactory.create(identifier, SwiftCompile.class, SwiftCompileTask.class))
				.action(new AttachImportModulesToCompileTaskRule(identifier))
				.action(importModulesRegistrationFactory.create(identifier))
				.action(new SwiftCompileTaskDefaultConfigurationRule(identifier))
			;
		}
		builder.action(ModelActionWithInputs.of(ModelComponentReference.of(LanguageSourceSetIdentifier.class), ModelComponentReference.of(ModelState.IsAtLeastRegistered.class), (entity, id, ignored) -> {
			if (id.equals(identifier)) {
				entity.addComponent(createdUsing(ModelType.of(SwiftSourceSet.class), () -> objectFactory.newInstance(DefaultSwiftSourceSet.class)));
			}
		}));
		return builder.build();
	}

	public static class DefaultSwiftSourceSet implements SwiftSourceSet, ModelBackedLanguageSourceSetLegacyMixIn<SwiftSourceSet> {
		public ConfigurableSourceSet getSource() {
			return ModelProperties.getProperty(this, "source").as(ConfigurableSourceSet.class).get();
		}

		public TaskProvider<SwiftCompile> getCompileTask() {
			return ModelProperties.getProperty(this, "compileTask").as(TaskProvider.class).get();
		}

		@Override
		public String toString() {
			return "Swift sources '" + getName() + "'";
		}
	}
}
