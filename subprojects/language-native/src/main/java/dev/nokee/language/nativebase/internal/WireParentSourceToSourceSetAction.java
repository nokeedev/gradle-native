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

package dev.nokee.language.nativebase.internal;

import dev.nokee.language.base.HasSource;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.View;
import org.gradle.api.Action;
import org.gradle.api.plugins.ExtensionAware;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Callable;

public final class WireParentSourceToSourceSetAction<T> implements Action<T> {
	private final String sourceExtensionName;

	public WireParentSourceToSourceSetAction(String sourceExtensionName) {
		this.sourceExtensionName = sourceExtensionName;
	}

	@Override
	public void execute(T t) {
		// TODO: check if it's a native variant?
		if (t instanceof SourceAwareComponent && ((SourceAwareComponent<?>) t).getSources() instanceof View) {
			@SuppressWarnings("unchecked")
			final View<LanguageSourceSet> sources = (View<LanguageSourceSet>) ((SourceAwareComponent<?>) t).getSources();
			sources.configureEach(sourceSet -> {
				if (sourceSet instanceof HasSource) {
					((HasSource) sourceSet).getSource().from((Callable<Object>) () -> {
						return Optional.ofNullable(((ExtensionAware) t).getExtensions().findByName(sourceExtensionName)).orElse(Collections.emptyList());
					});
				}
			});
		}
	}
}
