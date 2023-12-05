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

import dev.nokee.model.internal.ModelElement;
import org.gradle.api.plugins.ExtensionAware;

import java.util.function.Predicate;

public interface SupportLanguageSourceSet {
	static Predicate<ModelElement> hasLanguageSupport(Class<? extends SupportLanguageSourceSet> supportType) {
		return element -> element.instanceOf(supportType) || element.safeAs(ExtensionAware.class).map(it -> it.getExtensions().findByType(supportType) != null).getOrElse(false);
	}
}
