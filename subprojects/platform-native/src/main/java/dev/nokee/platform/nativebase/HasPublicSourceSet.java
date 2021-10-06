/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.platform.nativebase;

import dev.nokee.language.base.FunctionalSourceSet;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.platform.base.ComponentSources;
import org.gradle.api.NamedDomainObjectProvider;

/**
 * Represents a component sources that carries an native header set named {@literal public}.
 *
 * @see ComponentSources
 * @see NativeHeaderSet
 * @since 0.5
 */
public interface HasPublicSourceSet {
// Note: There is no safe accessor for public {@link NativeHeaderSet} as `public` is a Java keyword. Use `get('public' CHeaderSet)`.
	/**
	 * Returns a native header set provider for the source set named {@literal public}.
	 *
	 * @return a provider for {@literal public} source set, never null
	 */
	default NamedDomainObjectProvider<NativeHeaderSet> getPublic() {
		return ((FunctionalSourceSet) this).named("public", NativeHeaderSet.class);
	}
	// Note: Use configure("public", CppHeaderSet) {}  for public headers
}
