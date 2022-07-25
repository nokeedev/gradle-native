/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.nativebase.internal.linking;

import dev.nokee.model.internal.core.ModelElements;
import dev.nokee.platform.nativebase.HasLinkTask;
import dev.nokee.platform.nativebase.tasks.ObjectLink;
import org.gradle.api.tasks.TaskProvider;

public interface HasLinkTaskMixIn<T extends ObjectLink> extends HasLinkTask<T> {
	@SuppressWarnings("unchecked") // IntelliJ is retarded
	default TaskProvider<T> getLinkTask() {
		return (TaskProvider<T>) ModelElements.of(this, NativeLinkTask.class).as(ObjectLink.class).asProvider();
	}
}
