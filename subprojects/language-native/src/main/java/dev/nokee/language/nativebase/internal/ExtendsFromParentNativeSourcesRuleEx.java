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

import dev.nokee.language.base.internal.SourcePropertyName;
import dev.nokee.model.internal.ModelElement;
import dev.nokee.platform.base.internal.ParentAware;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.plugins.ExtensionAware;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;

public final class ExtendsFromParentNativeSourcesRuleEx implements BiConsumer<ModelElement, ExtensionAware> {
	private final SourcePropertyName propertyName;

	public ExtendsFromParentNativeSourcesRuleEx(SourcePropertyName propertyName) {
		this.propertyName = propertyName;
	}

	@Override
	public void accept(ModelElement element, ExtensionAware target) {
		final ConfigurableFileCollection sources = findExtension(target);
		if (sources != null && target instanceof ParentAware) {
			sources.from((Callable<?>) () -> {
				return element.getParents().map(it -> it.safeAs(ExtensionAware.class).map(this::findExtension).getOrElse(null)).findFirst().map(t -> (Iterable<?>) t).orElse(Collections.emptyList());
			});
		}
	}

	@Nullable
	private ConfigurableFileCollection findExtension(ExtensionAware target) {
		return (ConfigurableFileCollection) target.getExtensions().findByName(propertyName.asExtensionName());
	}
}
