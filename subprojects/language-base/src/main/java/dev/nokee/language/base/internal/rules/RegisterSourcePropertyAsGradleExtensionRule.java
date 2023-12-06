/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.language.base.internal.rules;

import dev.nokee.language.base.internal.ISourceProperty;
import dev.nokee.language.base.internal.LanguagePropertiesAware;
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.plugins.ExtensionContainer;

public final class RegisterSourcePropertyAsGradleExtensionRule implements Action<LanguagePropertiesAware> {
	@Override
	public void execute(LanguagePropertiesAware target) {
		target.getSourceProperties().all(registerOn(target.getExtensions()));
	}

	private Action<ISourceProperty> registerOn(ExtensionContainer extensions) {
		return property -> extensions.add(ConfigurableFileCollection.class, property.getName(), property.getSource());
	}
}
