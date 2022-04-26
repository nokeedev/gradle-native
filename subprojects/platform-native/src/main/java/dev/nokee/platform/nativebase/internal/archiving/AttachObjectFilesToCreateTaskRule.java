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
package dev.nokee.platform.nativebase.internal.archiving;

import dev.nokee.model.internal.actions.ModelAction;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.platform.nativebase.internal.compiling.ObjectFiles;
import dev.nokee.platform.nativebase.internal.linking.NativeLinkTask;
import dev.nokee.platform.nativebase.tasks.ObjectLink;
import dev.nokee.utils.ActionUtils;
import org.gradle.api.Task;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.tasks.CreateStaticLibrary;

import java.util.function.BiConsumer;

import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;

final class AttachObjectFilesToCreateTaskRule extends ModelActionWithInputs.ModelAction2<ObjectFiles, NativeArchiveTask> {
	private final ModelRegistry registry;

	public AttachObjectFilesToCreateTaskRule(ModelRegistry registry) {
		this.registry = registry;
	}

	@Override
	protected void execute(ModelNode entity, ObjectFiles objectFiles, NativeArchiveTask linkTask) {
		registry.instantiate(ModelAction.configure(linkTask.get().getId(), CreateStaticLibrary.class, configureSource(from(objectFiles))));
	}

	//region Task sources
	public static <SELF extends Task> ActionUtils.Action<SELF> configureSource(BiConsumer<? super SELF, ? super PropertyUtils.FileCollectionProperty> action) {
		return self -> action.accept(self, sourceProperty(self));
	}

	private static PropertyUtils.FileCollectionProperty sourceProperty(Task task) {
		if (task instanceof AbstractLinkTask) {
			return wrap(((AbstractLinkTask) task).getSource());
		} else if (task instanceof CreateStaticLibrary) {
			return new PropertyUtils.FileCollectionProperty() {
				private final CreateStaticLibrary createTask = (CreateStaticLibrary) task;

				@Override
				public void from(Object... paths) {
					createTask.source(paths);
				}

				@Override
				public void add(Object value) {
					createTask.source(value);
				}

				@Override
				public void addAll(Iterable<?> values) {
					createTask.source(values);
				}

				@Override
				public void set(Object value) {
					throw new UnsupportedOperationException();
				}

				@Override
				public void finalizeValue() {
					// do nothing
				}

				@Override
				public void finalizeValueOnRead() {
					// do nothing
				}

				@Override
				public void disallowChanges() {
					// do nothing
				}
			};
		} else {
			throw new IllegalArgumentException("Could not configure the source of " + task);
		}
	}
	//endregion
}
