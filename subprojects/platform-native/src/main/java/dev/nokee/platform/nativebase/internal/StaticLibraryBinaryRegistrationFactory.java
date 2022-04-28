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
import dev.nokee.model.internal.actions.ConfigurableTag;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelElements;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeAware;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.ModelBackedHasBaseNameMixIn;
import dev.nokee.platform.base.internal.ModelBackedNamedMixIn;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.archiving.HasCreateTask;
import dev.nokee.platform.nativebase.tasks.CreateStaticLibrary;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
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

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.utils.TransformerUtils.transformEach;

public final class StaticLibraryBinaryRegistrationFactory {
	private final ObjectFactory objectFactory;

	public StaticLibraryBinaryRegistrationFactory(ObjectFactory objectFactory) {
		this.objectFactory = objectFactory;
	}

	public ModelRegistration create(BinaryIdentifier identifier) {
		return ModelRegistration.builder()
			.withComponent(new IdentifierComponent(identifier))
			.withComponent(IsBinary.tag())
			.withComponent(ConfigurableTag.tag())
			.withComponent(NativeLanguageSourceSetAwareTag.tag())
			.withComponent(createdUsing(of(ModelBackedStaticLibraryBinary.class), () -> new ModelBackedStaticLibraryBinary(objectFactory)))
			.build();
	}

	public static final class ModelBackedStaticLibraryBinary implements StaticLibraryBinary, HasPublicType, ModelNodeAware
		, ModelBackedNamedMixIn
		, ModelBackedHasBaseNameMixIn
		, HasHeaderSearchPaths
		, HasCreateTask
		, HasObjectFilesToBinaryTask
	{
		private final ModelNode node = ModelNodeContext.getCurrentModelNode();
		private final NativeBinaryBuildable isBuildable = new NativeBinaryBuildable(this);
		private final ObjectFactory objectFactory;

		public ModelBackedStaticLibraryBinary(ObjectFactory objectFactory) {
			this.objectFactory = objectFactory;
		}

		@Override
		@SuppressWarnings("unchecked")
		public TaskView<SourceCompile> getCompileTasks() {
			return ModelProperties.getProperty(this, "compileTasks").as(TaskView.class).get();
		}

		@Override
		public TaskProvider<CreateStaticLibrary> getCreateTask() {
			return (TaskProvider<CreateStaticLibrary>) ModelElements.of(this).element("create", CreateStaticLibrary.class).asProvider();
		}

		@Override
		public boolean isBuildable() {
			return isBuildable.get();
		}

		@Override
		public TaskDependency getBuildDependencies() {
			return TaskDependencyUtils.composite(TaskDependencyUtils.ofIterable(getCompileTasks().getElements()), TaskDependencyUtils.of(getCreateTask()));
		}

		@Override
		public TypeOf<?> getPublicType() {
			return TypeOf.typeOf(StaticLibraryBinary.class);
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
		public TaskProvider<CreateStaticLibraryTask> getCreateOrLinkTask() {
			return (TaskProvider<CreateStaticLibraryTask>) ModelElements.of(this).element("create", CreateStaticLibraryTask.class).asProvider();
		}

		@Override
		public String toString() {
			return "static library binary '" + getName() + "'";
		}
	}
}
