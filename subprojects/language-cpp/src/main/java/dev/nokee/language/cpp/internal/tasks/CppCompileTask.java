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
package dev.nokee.language.cpp.internal.tasks;

import com.google.common.collect.ImmutableSet;
import dev.nokee.language.cpp.tasks.CppCompile;
import dev.nokee.language.nativebase.HeaderSearchPath;
import dev.nokee.language.nativebase.internal.DefaultHeaderSearchPath;
import dev.nokee.language.nativebase.tasks.internal.NativeSourceCompileTask;
import org.gradle.api.provider.SetProperty;
import org.gradle.api.tasks.CacheableTask;

@CacheableTask
public abstract class CppCompileTask extends org.gradle.language.cpp.tasks.CppCompile implements NativeSourceCompileTask, CppCompile {
	public CppCompileTask() {
		getHeaderSearchPaths().set(getIncludes().getElements().map(includes -> {
			return includes.stream().map(it -> new DefaultHeaderSearchPath(it.getAsFile())).collect(ImmutableSet.toImmutableSet());
		}));
		getHeaderSearchPaths().disallowChanges();
	}

	public abstract SetProperty<HeaderSearchPath> getHeaderSearchPaths();
}
