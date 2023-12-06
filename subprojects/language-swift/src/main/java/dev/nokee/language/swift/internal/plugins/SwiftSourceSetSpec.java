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
package dev.nokee.language.swift.internal.plugins;

import dev.nokee.language.base.internal.SourcePropertyAware;
import dev.nokee.language.nativebase.internal.BaseNativeSourceSetSpec;
import dev.nokee.language.swift.SwiftSourceSet;
import dev.nokee.language.swift.tasks.internal.SwiftCompileTask;
import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import org.gradle.api.tasks.TaskProvider;

public /*final*/ abstract class SwiftSourceSetSpec extends BaseNativeSourceSetSpec implements SwiftSourceSet
	, HasImportModules, SourcePropertyAware
{
	@NestedObject
	public abstract TaskProvider<SwiftCompileTask> getCompileTask();

	@NestedObject
	public abstract ResolvableDependencyBucketSpec getImportModules();

	@Override
	protected String getTypeName() {
		return "Swift sources";
	}
}
