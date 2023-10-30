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

import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.language.nativebase.internal.NativeLanguageSourceSetAwareTag;
import dev.nokee.language.nativebase.tasks.NativeSourceCompile;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelElements;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.MainProjectionComponent;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.linking.HasLinkLibrariesDependencyBucket;
import dev.nokee.platform.nativebase.internal.linking.HasLinkTaskMixIn;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.utils.TaskDependencyUtils;
import lombok.val;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import java.util.Set;
import java.util.concurrent.Callable;

import static dev.nokee.utils.TransformerUtils.transformEach;

public final class SharedLibraryBinaryRegistrationFactory {
	public ModelRegistration create(ModelObjectIdentifier identifier) {
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponentTag(IsBinary.class)
			.withComponentTag(ConfigurableTag.class)
			.withComponentTag(NativeLanguageSourceSetAwareTag.class)
			.withComponent(new MainProjectionComponent(ModelBackedSharedLibraryBinary.class))
			.build();
	}

	public static final class ModelBackedSharedLibraryBinary implements SharedLibraryBinary, HasPublicType, ModelNodeAware
		, ModelBackedNamedMixIn
		, ModelBackedHasBaseNameMixIn
		, HasHeaderSearchPaths
		, HasLinkLibrariesDependencyBucket
		, HasRuntimeLibrariesDependencyBucket
		, HasLinkTaskMixIn<LinkSharedLibrary>
		, HasObjectFilesToBinaryTask
	{
		private final ModelNode node = ModelNodeContext.getCurrentModelNode();
		private final NativeBinaryBuildable isBuildable = new NativeBinaryBuildable(this);
		private final ObjectFactory objectFactory;

		public ModelBackedSharedLibraryBinary(ObjectFactory objectFactory) {
			this.objectFactory = objectFactory;
		}

		@Override
		@SuppressWarnings("unchecked")
		public TaskView<SourceCompile> getCompileTasks() {
			return ModelProperties.getProperty(this, "compileTasks").as(TaskView.class).get();
		}

		@Override
		public TaskProvider<LinkSharedLibrary> getLinkTask() {
			return (TaskProvider<LinkSharedLibrary>) ModelElements.of(this).element("link", LinkSharedLibrary.class).asProvider();
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
		public TypeOf<?> getPublicType() {
			return TypeOf.typeOf(SharedLibraryBinary.class);
		}

		@Override
		public ModelNode getNode() {
			return node;
		}

		@Override
		public Provider<Set<FileSystemLocation>> getHeaderSearchPaths() {
			val result = objectFactory.fileCollection();
			result.from((Callable<Object>) getCompileTasks().withType(NativeSourceCompile.class).map(it -> it.getHeaderSearchPaths().map(transformEach(path -> path.getAsFile())))::get);
			return result.getElements();
		}

		@Override
		public TaskProvider<LinkSharedLibraryTask> getCreateOrLinkTask() {
			return (TaskProvider<LinkSharedLibraryTask>) ModelElements.of(this).element("link", LinkSharedLibraryTask.class).asProvider();
		}

		@Override
		public String toString() {
			return "shared library binary '" + getName() + "'";
		}
	}
}
