package dev.nokee.ide.visualstudio.internal.plugins;

import dev.nokee.ide.base.internal.IdeProjectExtension;
import dev.nokee.ide.base.internal.IdeProjectInternal;
import dev.nokee.ide.base.internal.IdeWorkspaceExtension;
import dev.nokee.ide.base.internal.plugins.AbstractIdePlugin;
import dev.nokee.ide.visualstudio.*;
import dev.nokee.ide.visualstudio.internal.*;
import dev.nokee.internal.Cast;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.internal.Component;
import dev.nokee.platform.base.internal.ComponentCollection;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
import dev.nokee.platform.nativebase.internal.ExecutableBinaryInternal;
import dev.nokee.platform.nativebase.internal.SharedLibraryBinaryInternal;
import lombok.val;
import org.gradle.api.Rule;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFile;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.plugins.ide.internal.IdeProjectMetadata;

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
				task.getBridgeTaskPath().set(getBridgeTaskPath());
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
					target.getProperties().put("ConfigurationType", library.getDevelopmentVariant().flatMap(NativeLibrary::getDevelopmentBinary).map(this::toConfigurationType));
					target.getProperties().put("UseDebugLibraries", true);
					target.getProperties().put("PlatformToolset", "v142");
					target.getProperties().put("CharacterSet", "Unicode");
					target.getProperties().put("LinkIncremental", true);
					target.getItemProperties().maybeCreate("ClCompile").put("AdditionalIncludeDirectories", binary.flatMap(it -> ((SharedLibraryBinaryInternal) it).getHeaderSearchPaths().map(this::toSemiColonSeperatedPaths)));
				});
			});
		});
	}

	private String toConfigurationType(Binary binary) {
		if (binary instanceof SharedLibraryBinary) {
			return "DynamicLibrary";
		} else if (binary instanceof StaticLibraryBinary) {
			return "StaticLibrary";
		} else if (binary instanceof ExecutableBinary) {
			return "Application";
		}
		throw new IllegalArgumentException(String.format("Unknown binary type '%s'.", binary.getClass().getSimpleName()));
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
	 * @return a fully qualified task path format for the Gradle delegation to realize using the macros from within Visual Studio IDE.
	 */
	private String getBridgeTaskPath() {
		return getPrefixableProjectPath(getProject()) + ":" + VisualStudioIdeBridge.BRIDGE_TASK_NAME;
	}
}
