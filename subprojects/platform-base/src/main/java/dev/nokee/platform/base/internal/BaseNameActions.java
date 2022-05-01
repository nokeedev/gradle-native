/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import org.gradle.api.Action;

import java.util.function.BiConsumer;

public final class BaseNameActions {

	public static <SELF extends HasBaseName> Action<SELF> baseName(BiConsumer<? super SELF, ? super PropertyUtils.Property<String>> action) {
		return self -> action.accept(self, PropertyUtils.wrap(self.getBaseName()));
	}
}
