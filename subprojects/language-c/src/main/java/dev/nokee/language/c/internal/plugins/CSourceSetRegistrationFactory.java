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
package dev.nokee.language.c.internal.plugins;

import dev.nokee.language.base.ConfigurableSourceSet;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.language.base.internal.LanguageSourceSetRegistrationFactory;
import dev.nokee.language.base.internal.ModelBackedLanguageSourceSetLegacyMixIn;
import dev.nokee.language.c.CSourceSet;
import dev.nokee.language.c.internal.tasks.CCompileTask;
import dev.nokee.language.c.tasks.CCompile;
import dev.nokee.language.nativebase.internal.*;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelRegistration;
import lombok.val;
import org.gradle.api.tasks.TaskProvider;

public final class CSourceSetRegistrationFactory {
	private final LanguageSourceSetRegistrationFactory sourceSetRegistrationFactory;
	private final HeadersPropertyRegistrationActionFactory headersPropertyFactory;
	private final HeaderSearchPathsConfigurationRegistrationActionFactory resolvableHeadersRegistrationFactory;
	private final NativeCompileTaskRegistrationActionFactory compileTaskRegistrationFactory;

	public CSourceSetRegistrationFactory(LanguageSourceSetRegistrationFactory sourceSetRegistrationFactory, HeadersPropertyRegistrationActionFactory headersPropertyFactory, HeaderSearchPathsConfigurationRegistrationActionFactory resolvableHeadersRegistrationFactory, NativeCompileTaskRegistrationActionFactory compileTaskRegistrationFactory) {
		this.sourceSetRegistrationFactory = sourceSetRegistrationFactory;
		this.headersPropertyFactory = headersPropertyFactory;
		this.resolvableHeadersRegistrationFactory = resolvableHeadersRegistrationFactory;
		this.compileTaskRegistrationFactory = compileTaskRegistrationFactory;
	}

	public ModelRegistration create(LanguageSourceSetIdentifier identifier) {
		return create(identifier, false);
	}

	public ModelRegistration create(LanguageSourceSetIdentifier identifier, boolean isLegacy) {
		val builder = sourceSetRegistrationFactory.create(identifier, CSourceSet.class, DefaultCSourceSet.class);
		if (!isLegacy) {
			builder.action(headersPropertyFactory.create(identifier))
				.action(compileTaskRegistrationFactory.create(identifier, CCompile.class, CCompileTask.class))
				.action(resolvableHeadersRegistrationFactory.create(identifier))
				.action(new AttachHeaderSearchPathsToCompileTaskRule(identifier))
				.action(new NativeCompileTaskDefaultConfigurationRule(identifier))
			;
		}
		return builder.build();
	}

	public static class DefaultCSourceSet implements CSourceSet, ModelBackedLanguageSourceSetLegacyMixIn<CSourceSet> {
		public ConfigurableSourceSet getSource() {
			return ModelProperties.getProperty(this, "source").as(ConfigurableSourceSet.class).get();
		}

		public ConfigurableSourceSet getHeaders() {
			return ModelProperties.getProperty(this, "headers").as(ConfigurableSourceSet.class).get();
		}

		public TaskProvider<CCompile> getCompileTask() {
			return ModelProperties.getProperty(this, "compileTask").as(TaskProvider.class).get();
		}

		@Override
		public String toString() {
			return "C sources '" + getName() + "'";
		}
	}
}
