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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.internal.BaseVariant;
import dev.nokee.platform.base.internal.VariantIdentifier;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Task;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.tasks.TaskProvider;

public class BaseNativeVariant extends BaseVariant {
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	private final TaskProvider<Task> assembleTask;

	public BaseNativeVariant(VariantIdentifier identifier, ObjectFactory objects, ProviderFactory providers, TaskProvider<Task> assembleTask) {
		super(identifier, objects);
		this.providers = providers;
		this.assembleTask = assembleTask;
	}

	public TaskProvider<Task> getAssembleTask() {
		return assembleTask;
	}
}
