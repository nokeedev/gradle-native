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

import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import org.gradle.api.Buildable;

/**
 * A shared library built from 1 or more native language.
 *
 * @since 0.3
 */
public interface SharedLibraryBinary extends NativeBinary, Buildable, HasBaseName, HasLinkTask<LinkSharedLibrary> {
	/**
	 * Returns whether or not this binary can be built in the current environment.
	 *
	 * @return {@code true} if this binary can be built in the current environment or {@code false} otherwise.
	 * @since 0.4
	 */
	boolean isBuildable();
}
