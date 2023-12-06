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

package dev.nokee.language.base.internal;

import dev.nokee.model.internal.ModelElementSupport;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;

public abstract /*final*/ class SourceProperty extends ModelElementSupport implements LanguageSourcePropertySpec {
	private final Provider<String> sourceName;

	@Inject
	public SourceProperty(ProviderFactory providers) {
		sourceName = providers.provider(() -> {
			final String elementName = getIdentifier().getName().toString();
			return elementName.substring(0, elementName.length() - "Sources".length());
		});
	}

	@Override
	public Provider<String> getSourceName() {
		return sourceName;
	}
}
