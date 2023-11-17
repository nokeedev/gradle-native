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
package dev.nokee.platform.nativebase.internal;

import dev.nokee.internal.Factory;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAware;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import dev.nokee.platform.base.internal.tasks.TaskName;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.internal.linking.HasLinkLibrariesDependencyBucket;
import dev.nokee.platform.nativebase.internal.linking.LinkTaskMixIn;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import dev.nokee.platform.nativebase.tasks.internal.LinkExecutableTask;
import dev.nokee.utils.TaskDependencyUtils;
import lombok.val;
import org.gradle.api.Task;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import javax.inject.Inject;
import java.util.Set;
import java.util.concurrent.Callable;

import static dev.nokee.utils.TransformerUtils.transformEach;

public final class ExecutableBinaryRegistrationFactory {
	public ModelRegistration create(ModelObjectIdentifier identifier) {
		throw new UnsupportedOperationException();
	}

	public static /*final*/ abstract class ModelBackedExecutableBinary extends ModelElementSupport implements ExecutableBinary
		, NativeLanguageSourceSetAware
		, HasHeaderSearchPaths
		, HasLinkLibrariesDependencyBucket
		, HasRuntimeLibrariesDependencyBucket
		, LinkTaskMixIn<LinkExecutable, LinkExecutableTask>
		, HasObjectFilesToBinaryTask
	{
		private final NativeBinaryBuildable isBuildable = new NativeBinaryBuildable(this);
		private final ObjectFactory objectFactory;

		@Inject
		public ModelBackedExecutableBinary(ModelObjectRegistry<Task> taskRegistry, ModelObjectRegistry<DependencyBucket> bucketRegistry, Factory<TaskView<SourceCompile>> compileTasksFactory, ObjectFactory objectFactory) {
			getExtensions().add("linkTask", taskRegistry.register(getIdentifier().child(TaskName.of("link")), LinkExecutableTask.class).asProvider());
			getExtensions().add("linkLibraries", bucketRegistry.register(getIdentifier().child("linkLibraries"), ResolvableDependencyBucketSpec.class).get());
			getExtensions().add("runtimeLibraries", bucketRegistry.register(getIdentifier().child("runtimeLibraries"), ResolvableDependencyBucketSpec.class).get());
			getExtensions().add("compileTasks", compileTasksFactory.create());
			this.objectFactory = objectFactory;
		}

		@Override
		public boolean isBuildable() {
			return isBuildable.get();
		}

		@Override
		public TaskDependency getBuildDependencies() {
			return TaskDependencyUtils.composite(TaskDependencyUtils.ofIterable(getCompileTasks().getElements()), TaskDependencyUtils.of(getLinkTask()));
		}

		@Override
		public Provider<Set<FileSystemLocation>> getHeaderSearchPaths() {
			val result = objectFactory.fileCollection();
			result.from((Callable<Object>) getCompileTasks().withType(NativeSourceCompile.class).map(it -> it.getHeaderSearchPaths().map(transformEach(path -> path.getAsFile())))::get);
			return result.getElements();
		}

		@Override
		public TaskProvider<LinkExecutableTask> getCreateOrLinkTask() {
			return getLinkTask();
		}

		@Override
		protected String getTypeName() {
			return "executable binary";
		}
	}
}
