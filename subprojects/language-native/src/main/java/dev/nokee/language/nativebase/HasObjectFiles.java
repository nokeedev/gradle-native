/*
 * Copyright 2021 the original author or authors.
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

import org.gradle.api.file.ConfigurableFileCollection;

/**
 * Represents an element that carries native object files.
 *
 * @since 0.5
 */
public interface HasObjectFiles {
	/**
	 * Returns the object files produced during the compilation.
	 *
	 * @return a file collection containing the object files, never null.
	 */
	ConfigurableFileCollection getObjectFiles();
}
