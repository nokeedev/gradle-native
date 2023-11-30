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

package dev.nokee.language.nativebase;

import dev.nokee.language.base.HasCompileTask;
import dev.nokee.language.base.HasSource;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.platform.base.DependencyAwareComponent;
import org.gradle.api.tasks.TaskProvider;

public interface NativeSourceSet extends LanguageSourceSet
	, HasSource
	, HasCompileTask
	, DependencyAwareComponent<NativeSourceSetComponentDependencies>
{
	@Override
	TaskProvider<? extends SourceCompile> getCompileTask();
}