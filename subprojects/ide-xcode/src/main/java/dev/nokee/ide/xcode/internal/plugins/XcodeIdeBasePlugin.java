package dev.nokee.ide.xcode.internal.plugins;

import dev.nokee.ide.base.internal.*;
import dev.nokee.ide.base.internal.plugins.AbstractIdePlugin;
import dev.nokee.ide.xcode.XcodeIdeProject;
import dev.nokee.ide.xcode.internal.*;
import dev.nokee.ide.xcode.internal.services.XcodeIdeGidGeneratorService;
import dev.nokee.ide.xcode.internal.tasks.GenerateXcodeIdeWorkspaceTask;
import org.gradle.api.Rule;
import org.gradle.api.Task;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.internal.Actions;
import org.gradle.plugins.ide.internal.IdeProjectMetadata;

import javax.inject.Inject;

import static dev.gradleplugins.grava.util.ProjectUtils.getPrefixableProjectPath;

public abstract class XcodeIdeBasePlugin extends AbstractIdePlugin<XcodeIdeProject> {
	public static final String XCODE_EXTENSION_NAME = "xcode";

	@Override
	public void doProjectApply(IdeProjectExtension<XcodeIdeProject> extension) {
		DefaultXcodeIdeProjectExtension projectExtension = (DefaultXcodeIdeProjectExtension) extension;

		Provider<XcodeIdeGidGeneratorService> xcodeIdeGidGeneratorService = getProject().getGradle().getSharedServices().registerIfAbsent("xcodeIdeGidGeneratorService", XcodeIdeGidGeneratorService.class, Actions.doNothing());
		projectExtension.getProjects().withType(DefaultXcodeIdeProject.class).configureEach(xcodeProject -> {
			xcodeProject.getSources().from(getBuildFiles());
			xcodeProject.getGeneratorTask().configure( task -> {
				FileSystemLocation projectLocation = getLayout().getProjectDirectory().dir(xcodeProject.getName() + ".xcodeproj");
				task.getProjectLocation().convention(projectLocation);
				task.usesService(xcodeIdeGidGeneratorService);
				task.getGidGenerator().set(xcodeIdeGidGeneratorService);
				task.getGradleCommand().set(toGradleCommand(getProject().getGradle()));
				task.getBridgeTaskPath().set(getBridgeTaskPath());
				task.getAdditionalGradleArguments().set(getAdditionalBuildArguments());
			});
		});

		getProject().getTasks().addRule(getObjects().newInstance(XcodeIdeBridge.class, this, projectExtension.getProjects(), getProject()));
	}

	@Override
	public void doWorkspaceApply(IdeWorkspaceExtension<XcodeIdeProject> extension) {
		DefaultXcodeIdeWorkspaceExtension workspaceExtension = (DefaultXcodeIdeWorkspaceExtension) extension;

		workspaceExtension.getWorkspace().getGeneratorTask().configure(task -> {
			task.getWorkspaceLocation().set(getLayout().getProjectDirectory().dir(getProject().getName() + ".xcworkspace"));
			task.getProjectReferences().set(workspaceExtension.getWorkspace().getProjects());
			task.getDerivedDataLocation().set(".gradle/XcodeDerivedData");
		});

		getCleanTask().configure(task -> {
			task.delete(workspaceExtension.getWorkspace().getGeneratorTask().flatMap(GenerateXcodeIdeWorkspaceTask::getDerivedDataLocation));
		});
	}

	@Override
	protected String getExtensionName() {
		return XCODE_EXTENSION_NAME;
	}

	@Override
	protected IdeProjectMetadata newIdeProjectMetadata(Provider<IdeProjectInternal> ideProject) {
		return new DefaultXcodeIdeProjectReference(ideProject.map(DefaultXcodeIdeProject.class::cast));
	}

	@Override
	protected Class<? extends BaseIdeProjectReference> getIdeProjectReferenceType() {
		return DefaultXcodeIdeProjectReference.class;
	}

	@Override
	protected IdeProjectMetadata newIdeCleanMetadata(Provider<? extends Task> cleanTask) {
		return new XcodeIdeCleanMetadata(cleanTask);
	}

	@Override
	protected Class<? extends BaseIdeCleanMetadata> getIdeCleanMetadataType() {
		return XcodeIdeCleanMetadata.class;
	}

	@Override
	protected IdeWorkspaceExtension<XcodeIdeProject> newIdeWorkspaceExtension() {
		return getObjects().newInstance(DefaultXcodeIdeWorkspaceExtension.class);
	}

	@Override
	protected IdeProjectExtension<XcodeIdeProject> newIdeProjectExtension() {
		return getObjects().newInstance(DefaultXcodeIdeProjectExtension.class);
	}

	@Inject
	protected abstract ProjectLayout getLayout();

	/**
	 * Returns the task name format to uses when delegating to Gradle.
	 * When Gradle is invoked with tasks following the name format, it is delegated to {@link XcodeIdeBridge} via {@link TaskContainer#addRule(Rule)}.
	 *
	 * @return a fully qualified task path format for the {@literal PBXLegacyTarget} target type to realize using the build settings from within Xcode IDE.
	 */
	private String getBridgeTaskPath() {
		return getPrefixableProjectPath(getProject()) + ":" + XcodeIdeBridge.BRIDGE_TASK_NAME;
	}
}
