package dev.nokee.ide.visualstudio.internal.plugins;

import dev.nokee.ide.base.internal.IdeProjectExtension;
import dev.nokee.ide.base.internal.IdeProjectInternal;
import dev.nokee.ide.base.internal.IdeWorkspaceExtension;
import dev.nokee.ide.base.internal.plugins.AbstractIdePlugin;
import dev.nokee.ide.visualstudio.*;
import dev.nokee.ide.visualstudio.internal.*;
import dev.nokee.ide.visualstudio.internal.vcxproj.VCXProperty;
import dev.nokee.internal.Cast;
import dev.nokee.platform.base.internal.Component;
import dev.nokee.platform.base.internal.ComponentCollection;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import lombok.Value;
import lombok.val;
import org.gradle.api.*;
import org.gradle.api.file.Directory;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.Copy;
import org.gradle.api.tasks.Sync;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.plugins.ide.internal.IdeProjectMetadata;

import javax.inject.Inject;
import java.io.File;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static dev.nokee.internal.ProjectUtils.getPrefixableProjectPath;

public abstract class VisualStudioIdePlugin extends AbstractIdePlugin<VisualStudioIdeProject> {
	public static final String VISUAL_STUDIO_EXTENSION_NAME = "visualStudio";

	@Override
	protected void doProjectApply(IdeProjectExtension<VisualStudioIdeProject> extension) {
		extension.getProjects().withType(DefaultVisualStudioIdeProject.class).configureEach(xcodeProject -> {
			xcodeProject.getGeneratorTask().configure( task -> {
				RegularFile projectLocation = getLayout().getProjectDirectory().file(xcodeProject.getName() + ".vcxproj");
				task.getProjectLocation().set(projectLocation);
//				task.usesService(xcodeIdeGidGeneratorService);
//				task.getGidGenerator().set(xcodeIdeGidGeneratorService);
				task.getGradleCommand().set(toGradleCommand(getProject().getGradle()));
				task.getBridgeTaskPath().set(toBridgeTaskPath(getProject()));
				task.getAdditionalGradleArguments().set(getAdditionalBuildArguments());
//				task.getSources().from(getBuildFiles());
			});
		});

		getProject().getTasks().addRule(getObjects().newInstance(VisualStudioIdeBridge.class, extension.getProjects(), getProject()));

		getProject().getPluginManager().withPlugin("dev.nokee.cpp-application", this::registerNativeApplicationProjects);
		getProject().getPluginManager().withPlugin("dev.nokee.cpp-library", this::registerNativeLibraryProjects);
	}

	private void registerNativeApplicationProjects(AppliedPlugin appliedPlugin) {
		getProject().getExtensions().getByType(VisualStudioIdeProjectExtension.class).getProjects().register(getProject().getName(), visualStudioProject -> {
			ComponentCollection<Component> components = Cast.uncheckedCast("of type erasure", getProject().getExtensions().getByType(ComponentCollection.class));
			components.configureEach(DefaultNativeApplicationComponent.class, application -> {
				val visualStudioProjectInternal = (DefaultVisualStudioIdeProject)visualStudioProject;

				application.getSourceCollection().forEach(sourceSet -> {
					visualStudioProjectInternal.getSourceFiles().from(sourceSet.getAsFileTree());
				});
				visualStudioProjectInternal.getHeaderFiles().from(getProject().fileTree("src/main/headers", it -> it.include("*")));
				visualStudioProjectInternal.getBuildFiles().from(getBuildFiles());

				visualStudioProject.target(VisualStudioIdeProjectConfiguration.of(VisualStudioIdeConfiguration.of("Default"), VisualStudioIdePlatforms.X64), target -> {
					Provider<ExecutableBinary> binary = application.getDevelopmentVariant().flatMap(it -> it.getBinaries().withType(ExecutableBinary.class).getElements().map(b -> b.iterator().next()));

					target.getProductLocation().set(binary.flatMap(it -> it.getLinkTask().get().getLinkedFile()));
					target.getProperties().put("ConfigurationType", "Application");
					target.getProperties().put("UseDebugLibraries", true);
					target.getProperties().put("PlatformToolset", "v142");
					target.getProperties().put("CharacterSet", "Unicode");
					target.getProperties().put("LinkIncremental", true);
					target.getItemProperties().maybeCreate("ClCompile").put("AdditionalIncludeDirectories", binary.flatMap(it -> ((ExecutableBinaryInternal) it).getHeaderSearchPaths().map(this::toSemiColonSeperatedPaths)));
				});
			});
		});
	}

	private void registerNativeLibraryProjects(AppliedPlugin appliedPlugin) {
		getProject().getExtensions().getByType(VisualStudioIdeProjectExtension.class).getProjects().register(getProject().getName(), visualStudioProject -> {
			ComponentCollection<Component> components = Cast.uncheckedCast("of type erasure", getProject().getExtensions().getByType(ComponentCollection.class));
			components.configureEach(DefaultNativeLibraryComponent.class, library -> {
				val visualStudioProjectInternal = (DefaultVisualStudioIdeProject)visualStudioProject;

				library.getSourceCollection().forEach(sourceSet -> {
					visualStudioProjectInternal.getSourceFiles().from(sourceSet.getAsFileTree());
				});
				visualStudioProjectInternal.getHeaderFiles().from(getProject().fileTree("src/main/headers", it -> it.include("*")));
				visualStudioProjectInternal.getHeaderFiles().from(getProject().fileTree("src/main/public", it -> it.include("*")));
				visualStudioProjectInternal.getBuildFiles().from(getBuildFiles());

				visualStudioProject.target(VisualStudioIdeProjectConfiguration.of(VisualStudioIdeConfiguration.of("Default"), VisualStudioIdePlatforms.X64), target -> {
					Provider<SharedLibraryBinary> binary = library.getDevelopmentVariant().flatMap(it -> it.getBinaries().withType(SharedLibraryBinary.class).getElements().map(b -> b.iterator().next()));

					target.getProductLocation().set(binary.flatMap(it -> it.getLinkTask().get().getLinkedFile()));
					target.getProperties().put("ConfigurationType", "DynamicLibrary");
					target.getProperties().put("UseDebugLibraries", true);
					target.getProperties().put("PlatformToolset", "v142");
					target.getProperties().put("CharacterSet", "Unicode");
					target.getProperties().put("LinkIncremental", true);
					target.getItemProperties().maybeCreate("ClCompile").put("AdditionalIncludeDirectories", binary.flatMap(it -> ((SharedLibraryBinaryInternal) it).getHeaderSearchPaths().map(this::toSemiColonSeperatedPaths)));
				});
			});
		});
	}

	private String toSemiColonSeperatedPaths(Iterable<? extends FileSystemLocation> it) {
		return StreamSupport.stream(it.spliterator(), false).map(a -> "\"" + a.getAsFile().getAbsolutePath() + "\"").collect(Collectors.joining(";"));
	}

	@Override
	protected void doWorkspaceApply(IdeWorkspaceExtension<VisualStudioIdeProject> extension) {
		DefaultVisualStudioIdeWorkspaceExtension workspaceExtension = (DefaultVisualStudioIdeWorkspaceExtension) extension;

		workspaceExtension.getWorkspace().getGeneratorTask().configure(task -> {
			task.getSolutionLocation().set(getLayout().getProjectDirectory().file(getProject().getName() + ".sln"));
			task.getProjectInformations().set(getArtifactRegistry().getIdeProjects(VisualStudioIdeProjectMetadata.class).stream().map(it -> new VisualStudioIdeProjectInformation(it.get())).collect(Collectors.toList()));
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
		return new VisualStudioIdeProjectMetadata(ideProject);
	}

	@Override
	protected String getExtensionName() {
		return VISUAL_STUDIO_EXTENSION_NAME;
	}

	/**
	 * Returns the task name format to use when delegating to Gradle.
	 * When Gradle is invoked with tasks following the name format, it is delegated to {@link VisualStudioIdeBridge} via {@link TaskContainer#addRule(Rule)}.
	 *
	 * @param project the {@link Project} instance the task belongs to
	 * @return a fully qualified task path format for the Gradle delegation to realize using the macros from within Visual Studio IDE.
	 */
	private static String toBridgeTaskPath(Project project) {
		return getPrefixableProjectPath(project) + ":_visualStudio__%s_$(ProjectName)_$(Configuration)_$(Platform)";
	}

	/**
	 * Task rule for bridging Xcode IDE with Gradle.
	 */
	protected static abstract class VisualStudioIdeBridge implements Rule {
		private final NamedDomainObjectSet<VisualStudioIdeProject> visualStudioProjects;
		private final Project project;
		private final VisualStudioIdePropertyAdapter visualStudioPropertyAdapter;

		@Inject
		public VisualStudioIdeBridge(NamedDomainObjectSet<VisualStudioIdeProject> visualStudioProjects, Project project) {
			this.visualStudioProjects = visualStudioProjects;
			this.project = project;
			this.visualStudioPropertyAdapter = new VisualStudioIdePropertyAdapter(project);
		}

		@Inject
		protected abstract TaskContainer getTasks();

		@Inject
		protected abstract ObjectFactory getObjects();

		@Override
		public String getDescription() {
			return "Visual Studio IDE bridge tasks begin with _visualStudio. Do not call these directly.";
		}

		@Override
		public void apply(String taskName) {
			if (taskName.startsWith("_visualStudio")) {
				VisualStudioIdeRequest request = VisualStudioIdeRequest.of(taskName);
				String action = request.getAction();
				if (action.equals("clean")) {
					Task bridgeTask = getTasks().create(taskName);
					bridgeTask.dependsOn("clean");
				} else if ("".equals(action) || "build".equals(action)) {
					final DefaultVisualStudioIdeTarget target = findVisualStudioTarget(request);
					Copy bridgeTask = getTasks().create(taskName, Copy.class); // TODO: We should sync but only the files we know about or redirect logs to another folder so we can sync
					bridgeProductBuild(bridgeTask, target, request);
				} else {
					throw new GradleException("Unrecognized bridge action from Xcode '" + action + "'");
				}
			}
		}

		private DefaultVisualStudioIdeTarget findVisualStudioTarget(VisualStudioIdeRequest request) {
			String projectName = request.getProjectName();
			DefaultVisualStudioIdeProject project = (DefaultVisualStudioIdeProject) visualStudioProjects.findByName(projectName);
			if (project == null) {
				throw new GradleException(String.format("Unknown Xcode IDE project '%s', try re-generating the Xcode IDE configuration using '%s:xcode' task.", projectName, getPrefixableProjectPath(this.project)));
			}

			String targetName = request.getTargetName();
			val projectConfiguration = VisualStudioIdeProjectConfiguration.of(VisualStudioIdeConfiguration.of(request.getConfiguration()), VisualStudioIdePlatform.of(request.getPlatformName()));
			DefaultVisualStudioIdeTarget target = project.getTargets().stream().filter(it -> it.getProjectConfiguration().equals(projectConfiguration)).findFirst().orElse(null);
			if (target == null) {
				throw new GradleException(String.format("Unknown Xcode IDE target '%s', try re-generating the Xcode IDE configuration using '%s:xcode' task.", targetName, getPrefixableProjectPath(this.project)));
			}
			return target;
		}

		private void bridgeProductBuild(Copy bridgeTask, DefaultVisualStudioIdeTarget target, VisualStudioIdeRequest request) {
			// Library or executable
//			final String configurationName = request.getConfiguration();
//			XcodeIdeBuildConfiguration configuration = target.getBuildConfigurations().findByName(configurationName);
//			if (configuration == null) {
//				throw new GradleException(String.format("Unknown Xcode IDE configuration '%s', try re-generating the Xcode IDE configuration using '%s:xcode' task.", configurationName, getPrefixableProjectPath(this.project)));
//			}

			// For XCTest bundle, we have to copy the binary to the BUILT_PRODUCTS_DIR, otherwise Xcode doesn't find the tests.
			// For legacy target, we can work around this by setting the CONFIGURATION_BUILD_DIR to the parent of where Gradle put the built binaries.
			// However, XCTest target are a bit more troublesome and it doesn't seem to play by the same rules as the legacy target.
			// To simplify the configuration, let's always copy the product where Xcode expect it to be.
			// It remove complexity and fragility on the Gradle side.
			bridgeTask.from(target.getProductLocation());
			bridgeTask.setDestinationDir(new File(visualStudioPropertyAdapter.getIntermediateDirectory()));
		}
	}

	// TODO: Converge XcodeIdeRequest, XcodeIdeBridge and XcodeIdePropertyAdapter. All three have overlapping responsibilities.
	//  Specifically for XcodeIdeBridge, we may want to attach the product sync task directly to the XcodeIde* model an convert the lifecycle task type to Task.
	//  It would make the bridge task more dummy and open for further customization of the Xcode delegation by allowing configuring the bridge task.
	// TODO: XcodeIdeRequest should convert the action string/null to an XcodeIdeAction enum
	@Value
	public static class VisualStudioIdeRequest {
		private static final Pattern LIFECYCLE_TASK_PATTERN = Pattern.compile("_visualStudio__(?<action>build|clean)_(?<project>[a-zA-Z\\-_]+)_(?<configuration>[a-zA-Z\\-_]+)_(?<platform>[a-zA-Z0-9]+)");
		String action;
		String projectName;
		String configuration;
		String platformName;

		public String getTargetName() {
			return configuration + "_" + platformName;
		}

		public static VisualStudioIdeRequest of(String taskName) {
			Matcher m = LIFECYCLE_TASK_PATTERN.matcher(taskName);
			if (m.matches()) {
				return new VisualStudioIdeRequest(m.group("action"), m.group("project"), m.group("configuration"), m.group("platform"));
			}
			throw new GradleException(String.format("Unable to match the lifecycle task name '%s', it is most likely a bug. Please report it at https://github.com/nokeedev/gradle-native/issues.", taskName));
		}
	}
}
