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
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.VariantAwareComponent;
import org.gradle.api.Action;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.plugins.ExtensionAware;

import java.util.Collections;
import java.util.Optional;
import java.util.concurrent.Callable;

public final class ExtendsFromParentNativeSourcesRule<TargetType extends Component> implements Action<TargetType> {
	private final SourcePropertyName propertyName;

	public ExtendsFromParentNativeSourcesRule(SourcePropertyName propertyName) {
		this.propertyName = propertyName;
	}

	@Override
	public void execute(TargetType target) {
		if (target instanceof VariantAwareComponent) {
			((VariantAwareComponent<?>) target).getVariants().configureEach(variant -> {
				final ConfigurableFileCollection sources = (ConfigurableFileCollection) ((ExtensionAware) variant).getExtensions().findByName(propertyName.asExtensionName());
				if (sources != null) {
					sources.from((Callable<?>) () -> {
						return Optional.ofNullable(((ExtensionAware) target).getExtensions().findByName(propertyName.asExtensionName())).orElse(Collections.emptyList());
					});
				}
			});
		}
	}
}
