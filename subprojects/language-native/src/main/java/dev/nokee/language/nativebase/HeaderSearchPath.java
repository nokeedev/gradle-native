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
package dev.nokee.language.nativebase;

import java.io.File;

/**
 * Represent of entry in the header search path.
 * The entry can be a user, system, or framework search path.
 *
 * @since 0.3
 */
public interface HeaderSearchPath {
	/**
	 * Returns the location of this search path, as an absolute {@link File}.
	 *
	 * @return a absolute {@link File} representing the location of the search path.
	 */
	File getAsFile();
}
