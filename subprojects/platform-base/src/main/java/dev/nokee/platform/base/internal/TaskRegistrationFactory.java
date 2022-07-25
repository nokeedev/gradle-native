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
package dev.nokee.platform.base.internal;

import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.platform.base.internal.tasks.TaskIdentifier;
import dev.nokee.platform.base.internal.tasks.TaskTypeComponent;
import org.gradle.api.Task;

import static dev.nokee.model.internal.tags.ModelTags.tag;

public final class TaskRegistrationFactory {
	@SuppressWarnings("unchecked")
	public <T extends Task> ModelRegistration.Builder create(TaskIdentifier<?> identifier, Class<T> type) {
		return ModelRegistration.builder()
			.withComponent(new ElementNameComponent(identifier.getName()))
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(tag(IsTask.class))
			.withComponent(tag(ConfigurableTag.class))
			.withComponent(new TaskTypeComponent((Class<Task>) type))
			;
	}
}
