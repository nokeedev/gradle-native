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

import dev.nokee.language.base.internal.LanguageSourcePropertySpec;
import dev.nokee.model.internal.ModelElementSupport;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;

public abstract /*final*/ class NativeHeaderProperty extends ModelElementSupport implements LanguageSourcePropertySpec {
	public abstract Property<Visibility> getVisibility();

	@Override
	public Provider<String> getSourceName() {
		return getVisibility().map(Object::toString);
	}

	public interface Visibility {
	}

	public enum BasicVisibility implements Visibility {
		Private("headers"), Public("public");

		private final String sourceSetName;

		BasicVisibility(String sourceSetName) {
			this.sourceSetName = sourceSetName;
		}

		@Override
		public String toString() {
			return sourceSetName;
		}
	}
}