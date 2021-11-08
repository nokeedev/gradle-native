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
import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.internal.BinaryIdentifier;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.platform.nativebase.tasks.internal.LinkSharedLibraryTask;
import dev.nokee.utils.TaskDependencyUtils;
import org.gradle.api.provider.Property;
import org.gradle.api.reflect.HasPublicType;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskDependency;
import org.gradle.api.tasks.TaskProvider;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.toPath;
import static dev.nokee.model.internal.core.ModelProjections.createdUsing;

public final class SharedLibraryBinaryRegistrationFactory {
	private final LinkLibrariesConfigurationRegistrationActionFactory linkLibrariesRegistrationFactory;
	private final RuntimeLibrariesConfigurationRegistrationActionFactory runtimeLibrariesRegistrationFactory;
	private final NativeLinkTaskRegistrationActionFactory linkTaskRegistrationActionFactory;
	private final BaseNamePropertyRegistrationActionFactory baseNamePropertyRegistrationActionFactory;
	private final RegisterCompileTasksPropertyActionFactory compileTasksPropertyActionFactory;

	public SharedLibraryBinaryRegistrationFactory(LinkLibrariesConfigurationRegistrationActionFactory linkLibrariesRegistrationFactory, RuntimeLibrariesConfigurationRegistrationActionFactory runtimeLibrariesRegistrationFactory, NativeLinkTaskRegistrationActionFactory linkTaskRegistrationActionFactory, BaseNamePropertyRegistrationActionFactory baseNamePropertyRegistrationActionFactory, RegisterCompileTasksPropertyActionFactory compileTasksPropertyActionFactory) {
		this.linkLibrariesRegistrationFactory = linkLibrariesRegistrationFactory;
		this.runtimeLibrariesRegistrationFactory = runtimeLibrariesRegistrationFactory;
		this.linkTaskRegistrationActionFactory = linkTaskRegistrationActionFactory;
		this.baseNamePropertyRegistrationActionFactory = baseNamePropertyRegistrationActionFactory;
		this.compileTasksPropertyActionFactory = compileTasksPropertyActionFactory;
	}

	public ModelRegistration create(BinaryIdentifier<?> identifier) {
		return ModelRegistration.builder()
			.withComponent(identifier)
			.withComponent(toPath(identifier))
			.withComponent(IsBinary.tag())
			.withComponent(createdUsing(ModelType.of(SharedLibraryBinary.class), ModelBackedSharedLibraryBinary::new))
			.action(new AttachLinkLibrariesToLinkTaskRule(identifier))
			.action(linkTaskRegistrationActionFactory.create(identifier, LinkSharedLibrary.class, LinkSharedLibraryTask.class))
			.action(baseNamePropertyRegistrationActionFactory.create(identifier))
			.action(compileTasksPropertyActionFactory.create(identifier))
			.action(new ConfigureLinkTaskFromBaseNameRule(identifier))
			.action(linkLibrariesRegistrationFactory.create(identifier))
			.action(runtimeLibrariesRegistrationFactory.create(identifier))
			.action(new AttachObjectFilesToLinkTaskRule(identifier))
			.action(new ConfigureLinkTaskDefaultsRule(identifier))
			.build();
	}

	private static final class ModelBackedSharedLibraryBinary implements SharedLibraryBinary, HasPublicType, ModelNodeAware {
		private final ModelNode node = ModelNodeContext.getCurrentModelNode();
		private final NativeBinaryBuildable isBuildable = new NativeBinaryBuildable(this);

		@Override
		public TaskView<SourceCompile> getCompileTasks() {
			return ModelProperties.getProperty(this, "compileTasks").as(TaskView.class).get();
		}

		@Override
		public TaskProvider<LinkSharedLibrary> getLinkTask() {
			return ModelProperties.getProperty(this, "linkTask").as(TaskProvider.class).get();
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
		public Property<String> getBaseName() {
			return ModelProperties.getProperty(this, "baseName").as(Property.class).get();
		}
	}
}
