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
package dev.nokee.platform.nativebase.internal.compiling;

import dev.nokee.language.nativebase.HasObjectFiles;
import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.model.internal.ModelObjects;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Artifact;
import dev.nokee.platform.base.internal.ComponentTasksPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.plugins.OnDiscover;
import dev.nokee.platform.base.internal.util.PropertyUtils;
import dev.nokee.platform.nativebase.internal.HasObjectFilesToBinaryTask;
import dev.nokee.utils.ActionUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Task;
import org.gradle.api.file.FileCollection;
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.PluginAware;
import org.gradle.api.provider.Provider;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.tasks.CreateStaticLibrary;

import java.util.List;
import java.util.function.BiConsumer;

import static dev.nokee.model.internal.plugins.ModelBasePlugin.model;
import static dev.nokee.model.internal.plugins.ModelBasePlugin.objects;
import static dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin.artifacts;
import static dev.nokee.platform.base.internal.util.PropertyUtils.from;
import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;
import static dev.nokee.utils.TransformerUtils.transformEach;

public class NativeCompileCapabilityPlugin<T extends ExtensionAware & PluginAware> implements Plugin<T> {
	@Override
	public void apply(T target) {
		artifacts(target).configureEach(new AttachObjectFilesToCreateOrLinkTaskAction(model(target, objects())));
		target.getExtensions().getByType(ModelConfigurer.class).configure(new OnDiscover(new RegisterCompileTasksPropertyRule(target.getExtensions().getByType(ModelRegistry.class), target.getExtensions().getByType(ComponentTasksPropertyRegistrationFactory.class))));
	}

	private static class AttachObjectFilesToCreateOrLinkTaskAction implements Action<Artifact> {
		private final ModelObjects objs;

		public AttachObjectFilesToCreateOrLinkTaskAction(ModelObjects objs) {
			this.objs = objs;
		}

		@Override
		public void execute(Artifact artifact) {
			if (artifact instanceof HasObjectFilesToBinaryTask) {
				ModelElementSupport.safeAsModelElement(artifact).map(ModelElement::getIdentifier).ifPresent(identifier -> {
					final Provider<List<FileCollection>> objectFiles = objs.get(AbstractNativeCompileTask.class, it -> {
						if (ModelObjectIdentifiers.descendantOf(it.getIdentifier(), identifier)) {
							it.get(); // realize to kickstart Task discover... we should be more smart about this
							return it.instanceOf(AbstractNativeCompileTask.class);
						} else {
							return false;
						}
					}).map(transformEach(it -> (FileCollection) ((HasObjectFiles) it).getObjectFiles()));

					((HasObjectFilesToBinaryTask) artifact).getCreateOrLinkTask().configure(configureSource(from(objectFiles)));
				});
			}
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
}
