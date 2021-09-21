/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.ide.visualstudio.internal.plugins;

import dev.nokee.ide.base.internal.*;
import dev.nokee.ide.base.internal.plugins.AbstractIdePlugin;
import dev.nokee.ide.visualstudio.VisualStudioIdeProject;
import dev.nokee.ide.visualstudio.internal.*;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Rule;
import org.gradle.api.Task;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.plugins.ide.internal.IdeProjectMetadata;

import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nokee.utils.ProjectUtils.getPrefixableProjectPath;

public abstract class VisualStudioIdeBasePlugin extends AbstractIdePlugin<VisualStudioIdeProject> {
	public static final String VISUAL_STUDIO_EXTENSION_NAME = "visualStudio";

	@Override
	protected void doProjectApply(IdeProjectExtension<VisualStudioIdeProject> extension) {
		extension.getProjects().withType(DefaultVisualStudioIdeProject.class).configureEach(visualStudioProject -> {
			visualStudioProject.getTargets().configureEach(it -> {
				it.getProperties().put("OutDir", ".vs\\derived-data\\$(ProjectName)-$(NokeeUniqueIdentifier)\\$(PlatformName)\\$(Configuration)\\");
				it.getItemProperties().maybeCreate("BuildLog").put("Path", ".vs\\derived-data\\$(ProjectName)-$(NokeeUniqueIdentifier)\\$(IntDir)$(MSBuildProjectName).log");
			});
			visualStudioProject.getBuildFiles().from(getBuildFiles());
			visualStudioProject.getGeneratorTask().configure( task -> {
				RegularFile projectLocation = getLayout().getProjectDirectory().file(visualStudioProject.getName() + ".vcxproj");
				task.getProjectLocation().convention(projectLocation);
				task.getGradleCommand().set(toGradleCommand(getProject().getGradle()));
				task.getBridgeTaskPath().set(getBridgeTaskPath());
				task.getAdditionalGradleArguments().set(getAdditionalBuildArguments());
			});
		});

		// Clean *.vcxproj.filters and *.vcxproj.user files
		getCleanTask().configure(task -> {
			task.delete(getProviders().provider(() -> extension.getProjects().stream().map(it -> it.getLocation().get().getAsFile().getAbsolutePath()).flatMap(it -> Stream.of(it + ".filters", it + ".user")).collect(Collectors.toList())));
		});

		getProject().getTasks().addRule(getObjects().newInstance(VisualStudioIdeBridge.class, this, extension.getProjects(), getProject()));
	}

	@Override
	protected void doWorkspaceApply(IdeWorkspaceExtension<VisualStudioIdeProject> extension) {
		DefaultVisualStudioIdeWorkspaceExtension workspaceExtension = (DefaultVisualStudioIdeWorkspaceExtension) extension;

		workspaceExtension.getWorkspace().getGeneratorTask().configure(task -> {
			task.getSolutionLocation().set(getLayout().getProjectDirectory().file(getProject().getName() + ".sln"));
			task.getProjectReferences().set(workspaceExtension.getSolution().getProjects());
		});

		// Clean .vs directory and warn user if solution is locked
		getCleanTask().configure(task -> {
			task.delete(".vs");
			task.doFirst(new Action<Task>() {
				@Override
				public void execute(Task task) {
					if (VisualStudioIdeUtils.isSolutionCurrentlyOpened(extension.getWorkspace().getLocation().get().getAsFile())) {
						throw new IllegalStateException(String.format("Please close your Visual Studio IDE before executing '%s'.", task.getName()));
					}
				}
			});
		});

		// Warn users when Visual Studio IDE holds a lock on the generated solution
		getLifecycleTask().configure(task -> {
			task.doLast(new Action<Task>() {
				@Override
				public void execute(Task task) {
					val solutionFile = extension.getWorkspace().getLocation().get().getAsFile();
					if (VisualStudioIdeUtils.isSolutionCurrentlyOpened(solutionFile)) {
						val message = "\n"
							+ "============\n"
							+ String.format("Visual Studio is currently holding the solution '%s' open.\n", getProject().relativePath(solutionFile.getAbsolutePath()))
							+ "This may impact features such as code navigation and code editing.\n"
							+ "We recommend manually triggering a solution rescan from the Visual Studio via Project > Rescan Solution.\n"
							+ "In the future, try closing your solution before executing the visualStudio task.\n"
							+ "To learn more about this issue, visit https://docs.nokee.dev/intellisense-reconcilation\n"
							+ "============\n";
						task.getLogger().warn(message);
					}
				}
			});
		});
	}

	@Override
	protected IdeWorkspaceExtension<VisualStudioIdeProject> newIdeWorkspaceExtension() {
		return getObjects().newInstance(DefaultVisualStudioIdeWorkspaceExtension.class);
	}

	@Override
	protected IdeProjectExtension<VisualStudioIdeProject> newIdeProjectExtension() {
		return getObjects().newInstance(DefaultVisualStudioIdeProjectExtension.class);
	}

	@Override
	protected IdeProjectMetadata newIdeProjectMetadata(Provider<IdeProjectInternal> ideProject) {
		return new DefaultVisualStudioIdeProjectReference(ideProject.map(DefaultVisualStudioIdeProject.class::cast));
	}

	@Override
	protected Class<? extends BaseIdeProjectReference> getIdeProjectReferenceType() {
		return DefaultVisualStudioIdeProjectReference.class;
	}

	@Override
	protected IdeProjectMetadata newIdeCleanMetadata(Provider<? extends Task> cleanTask) {
		return new VisualStudioIdeCleanMetadata(cleanTask);
	}

	@Override
	protected Class<? extends BaseIdeCleanMetadata> getIdeCleanMetadataType() {
		return VisualStudioIdeCleanMetadata.class;
	}

	@Override
	protected String getExtensionName() {
		return VISUAL_STUDIO_EXTENSION_NAME;
	}

	/**
	 * Returns the task name format to use when delegating to Gradle.
	 * When Gradle is invoked with tasks following the name format, it is delegated to {@link VisualStudioIdeBridge} via {@link TaskContainer#addRule(Rule)}.
	 *
	 * @return a fully qualified task path format for the Gradle delegation to realize using the macros from within Visual Studio IDE.
	 */
	private String getBridgeTaskPath() {
		return getPrefixableProjectPath(getProject()) + ":" + VisualStudioIdeBridge.BRIDGE_TASK_NAME;
	}
}
