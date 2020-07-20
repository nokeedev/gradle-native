package dev.nokee.ide.xcode.internal.plugins;

import dev.nokee.ide.base.internal.IdeProjectExtension;
import dev.nokee.ide.base.internal.IdeProjectInternal;
import dev.nokee.ide.base.internal.IdeWorkspaceExtension;
import dev.nokee.ide.base.internal.plugins.AbstractIdePlugin;
import dev.nokee.ide.xcode.XcodeIdeProject;
import dev.nokee.ide.xcode.internal.*;
import dev.nokee.ide.xcode.internal.services.XcodeIdeGidGeneratorService;
import dev.nokee.ide.xcode.internal.tasks.GenerateXcodeIdeWorkspaceTask;
import org.gradle.api.Rule;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.internal.Actions;
import org.gradle.plugins.ide.internal.IdeProjectMetadata;

import javax.inject.Inject;

import static dev.nokee.utils.ProjectUtils.getPrefixableProjectPath;

public abstract class XcodeIdePlugin extends AbstractIdePlugin<XcodeIdeProject> {
	public static final String XCODE_EXTENSION_NAME = "xcode";

	@Override
	public void doProjectApply(IdeProjectExtension<XcodeIdeProject> extension) {
		DefaultXcodeIdeProjectExtension projectExtension = (DefaultXcodeIdeProjectExtension) extension;

		Provider<XcodeIdeGidGeneratorService> xcodeIdeGidGeneratorService = getProject().getGradle().getSharedServices().registerIfAbsent("xcodeIdeGidGeneratorService", XcodeIdeGidGeneratorService.class, Actions.doNothing());
		projectExtension.getProjects().withType(DefaultXcodeIdeProject.class).configureEach(xcodeProject -> {
			xcodeProject.getGeneratorTask().configure( task -> {
				FileSystemLocation projectLocation = getLayout().getProjectDirectory().dir(xcodeProject.getName() + ".xcodeproj");
				task.getProjectLocation().set(projectLocation);
				task.usesService(xcodeIdeGidGeneratorService);
				task.getGidGenerator().set(xcodeIdeGidGeneratorService);
				task.getGradleCommand().set(toGradleCommand(getProject().getGradle()));
				task.getBridgeTaskPath().set(getBridgeTaskPath());
				task.getAdditionalGradleArguments().set(getAdditionalBuildArguments());
				task.getSources().from(getBuildFiles());
			});
		});

		getProject().getTasks().addRule(getObjects().newInstance(XcodeIdeBridge.class, this, projectExtension.getProjects(), getProject()));

		getProject().getPluginManager().withPlugin("dev.nokee.objective-c-ios-application", appliedPlugin -> {
			getProject().getPluginManager().apply(XcodeIdeObjectiveCIosApplicationPlugin.class);
		});
		getProject().getPluginManager().withPlugin("dev.nokee.swift-ios-application", appliedPlugin -> {
			getProject().getPluginManager().apply(XcodeIdeSwiftIosApplicationPlugin.class);
		});

		getProject().getPluginManager().withPlugin("dev.nokee.c-application", appliedPlugin -> {
			getProject().getPluginManager().apply(XcodeIdeNativeApplicationPlugin.class);
		});
		getProject().getPluginManager().withPlugin("dev.nokee.cpp-application", appliedPlugin -> {
			getProject().getPluginManager().apply(XcodeIdeNativeApplicationPlugin.class);
		});
		getProject().getPluginManager().withPlugin("dev.nokee.objective-c-application", appliedPlugin -> {
			getProject().getPluginManager().apply(XcodeIdeNativeApplicationPlugin.class);
		});
		getProject().getPluginManager().withPlugin("dev.nokee.objective-cpp-application", appliedPlugin -> {
			getProject().getPluginManager().apply(XcodeIdeNativeApplicationPlugin.class);
		});
		getProject().getPluginManager().withPlugin("dev.nokee.swift-application", appliedPlugin -> {
			getProject().getPluginManager().apply(XcodeIdeSwiftApplicationPlugin.class);
		});
		getProject().getPluginManager().withPlugin("dev.nokee.c-library", appliedPlugin -> {
			getProject().getPluginManager().apply(XcodeIdeNativeLibraryPlugin.class);
		});
		getProject().getPluginManager().withPlugin("dev.nokee.cpp-library", appliedPlugin -> {
			getProject().getPluginManager().apply(XcodeIdeNativeLibraryPlugin.class);
		});
		getProject().getPluginManager().withPlugin("dev.nokee.objective-c-library", appliedPlugin -> {
			getProject().getPluginManager().apply(XcodeIdeNativeLibraryPlugin.class);
		});
		getProject().getPluginManager().withPlugin("dev.nokee.objective-cpp-library", appliedPlugin -> {
			getProject().getPluginManager().apply(XcodeIdeNativeLibraryPlugin.class);
		});
		getProject().getPluginManager().withPlugin("dev.nokee.swift-library", appliedPlugin -> {
			getProject().getPluginManager().apply(XcodeIdeNativeLibraryPlugin.class);
		});
	}

	@Override
	public void doWorkspaceApply(IdeWorkspaceExtension<XcodeIdeProject> extension) {
		DefaultXcodeIdeWorkspaceExtension workspaceExtension = (DefaultXcodeIdeWorkspaceExtension) extension;

		workspaceExtension.getWorkspace().getGeneratorTask().configure(task -> {
			task.getWorkspaceLocation().set(getLayout().getProjectDirectory().dir(getProject().getName() + ".xcworkspace"));
			task.getProjectLocations().set(getArtifactRegistry().getIdeProjectFiles(XcodeIdeProjectMetadata.class).getElements());
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
		return new XcodeIdeProjectMetadata(ideProject.map(DefaultXcodeIdeProject.class::cast));
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
