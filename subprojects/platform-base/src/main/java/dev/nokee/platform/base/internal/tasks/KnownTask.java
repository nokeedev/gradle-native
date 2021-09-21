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
package dev.nokee.platform.base.internal.tasks;

import dev.nokee.model.internal.AbstractKnownDomainObject;
import lombok.ToString;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;

@ToString
public final class KnownTask<T extends Task> extends AbstractKnownDomainObject<Task, T> {
	KnownTask(TaskIdentifier<T> identifier, Provider<T> provider, TaskConfigurer configurer) {
		super(identifier, provider, configurer);
	}
}
