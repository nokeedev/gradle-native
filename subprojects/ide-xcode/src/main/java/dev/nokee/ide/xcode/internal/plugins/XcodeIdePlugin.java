package dev.nokee.ide.xcode.internal.plugins;

import dev.nokee.ide.base.internal.*;
import dev.nokee.ide.base.internal.plugins.AbstractIdePlugin;
import dev.nokee.ide.xcode.XcodeIdeBuildConfiguration;
import dev.nokee.ide.xcode.XcodeIdeProject;
import dev.nokee.ide.xcode.XcodeIdeTarget;
import dev.nokee.ide.xcode.internal.*;
import dev.nokee.ide.xcode.internal.services.XcodeIdeGidGeneratorService;
import dev.nokee.ide.xcode.internal.tasks.GenerateXcodeIdeWorkspaceTask;
import dev.nokee.ide.xcode.internal.tasks.SyncXcodeIdeProduct;
import lombok.Value;
import org.gradle.api.*;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.internal.Actions;
import org.gradle.plugins.ide.internal.IdeProjectMetadata;

import javax.inject.Inject;
import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static dev.nokee.internal.ProjectUtils.getPrefixableProjectPath;

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
			getProject().getPluginManager().apply(XcodeIdeSwiftLibraryPlugin.class);
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

	/**
	 * Task rule for bridging Xcode IDE with Gradle.
	 */
	protected static abstract class XcodeIdeBridge extends IdeBridgeRule<XcodeIdeRequest> {
		public static final String BRIDGE_TASK_NAME = "_xcode__${ACTION}_${PROJECT_NAME}_${TARGET_NAME}_${CONFIGURATION}";
		private final NamedDomainObjectSet<XcodeIdeProject> xcodeProjects;
		private final Project project;

		@Inject
		public XcodeIdeBridge(Describable ide, NamedDomainObjectSet<XcodeIdeProject> xcodeProjects, Project project) {
			super(ide);
			this.xcodeProjects = xcodeProjects;
			this.project = project;
		}

		@Inject
		protected abstract ObjectFactory getObjects();

		@Override
		protected String getLifecycleTaskNamePrefix() {
			return "_xcode";
		}

		@Override
		public void doHandle(XcodeIdeRequest request) {
			final XcodeIdeTarget target = findXcodeTarget(request);
			SyncXcodeIdeProduct bridgeTask = getTasks().create(request.getTaskName(), SyncXcodeIdeProduct.class);
			bridgeProductBuild(bridgeTask, target, request);
		}

		@Override
		public XcodeIdeRequest newRequest(String taskName) {
			return getObjects().newInstance(XcodeIdeRequest.class, taskName);
		}

		private XcodeIdeTarget findXcodeTarget(XcodeIdeRequest request) {
			String projectName = request.getProjectName();
			XcodeIdeProject project = xcodeProjects.findByName(projectName);
			if (project == null) {
				throw new GradleException(String.format("Unknown Xcode IDE project '%s', try re-generating the Xcode IDE configuration using '%s:xcode' task.", projectName, getPrefixableProjectPath(this.project)));
			}

			String targetName = request.getTargetName();
			XcodeIdeTarget target = project.getTargets().findByName(targetName);
			if (target == null) {
				throw new GradleException(String.format("Unknown Xcode IDE target '%s', try re-generating the Xcode IDE configuration using '%s:xcode' task.", targetName, getPrefixableProjectPath(this.project)));
			}
			return target;
		}

		private void bridgeProductBuild(SyncXcodeIdeProduct bridgeTask, XcodeIdeTarget target, XcodeIdeRequest request) {
			// Library or executable
			final String configurationName = request.getConfiguration();
			XcodeIdeBuildConfiguration configuration = target.getBuildConfigurations().findByName(configurationName);
			if (configuration == null) {
				throw new GradleException(String.format("Unknown Xcode IDE configuration '%s', try re-generating the Xcode IDE configuration using '%s:xcode' task.", configurationName, getPrefixableProjectPath(this.project)));
			}

			// For XCTest bundle, we have to copy the binary to the BUILT_PRODUCTS_DIR, otherwise Xcode doesn't find the tests.
			// For legacy target, we can work around this by setting the CONFIGURATION_BUILD_DIR to the parent of where Gradle put the built binaries.
			// However, XCTest target are a bit more troublesome and it doesn't seem to play by the same rules as the legacy target.
			// To simplify the configuration, let's always copy the product where Xcode expect it to be.
			// It remove complexity and fragility on the Gradle side.
			final Directory builtProductsPath = request.getBuiltProductsDirectory();
			bridgeTask.getProductLocation().convention(configuration.getProductLocation());
			bridgeTask.getDestinationLocation().convention(builtProductsPath.file(target.getProductReference().get()));
		}
	}

	// TODO: Converge XcodeIdeRequest, XcodeIdeBridge and XcodeIdePropertyAdapter. All three have overlapping responsibilities.
	//  Specifically for XcodeIdeBridge, we may want to attach the product sync task directly to the XcodeIde* model an convert the lifecycle task type to Task.
	//  It would make the bridge task more dummy and open for further customization of the Xcode delegation by allowing configuring the bridge task.
	// TODO: XcodeIdeRequest should convert the action string/null to an XcodeIdeAction enum
	public static abstract class XcodeIdeRequest implements IdeRequest {
		private static final Pattern LIFECYCLE_TASK_PATTERN = Pattern.compile("_xcode__(?<action>build|clean)?_(?<project>[a-zA-Z\\-_]+)_(?<target>[a-zA-Z\\-_]+)_(?<configuration>[a-zA-Z\\-_]+)");
		private final XcodeIdePropertyAdapter properties;
		private final String taskName;

		public XcodeIdeRequest(String taskName) {
			this.taskName = taskName;
			this.properties = getObjects().newInstance(XcodeIdePropertyAdapter.class);
		}

		@Inject
		protected abstract ObjectFactory getObjects();

		public String getTaskName() {
			return taskName;
		}

		public IdeRequestAction getAction() {
			return IdeRequestAction.valueOf(properties.getAction());
		}

		public String getProjectName() {
			return properties.getProjectName();
		}

		public String getTargetName() {
			return properties.getTargetName();
		}

		public String getConfiguration() {
			return properties.getConfiguration();
		}

		public Directory getBuiltProductsDirectory() {
			return getObjects().directoryProperty().fileValue(new File(properties.getBuiltProductsDir())).get();
		}
	}
}
