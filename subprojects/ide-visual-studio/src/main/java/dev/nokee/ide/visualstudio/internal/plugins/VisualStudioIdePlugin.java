package dev.nokee.ide.visualstudio.internal.plugins;

import dev.nokee.ide.base.internal.IdeProjectExtension;
import dev.nokee.ide.base.internal.IdeProjectInternal;
import dev.nokee.ide.base.internal.IdeWorkspaceExtension;
import dev.nokee.ide.base.internal.plugins.AbstractIdePlugin;
import dev.nokee.ide.visualstudio.*;
import dev.nokee.ide.visualstudio.internal.*;
import dev.nokee.internal.Cast;
import dev.nokee.language.cpp.tasks.CppCompile;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.Component;
import dev.nokee.platform.base.internal.ComponentCollection;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.BaseNativeBinary;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;
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
				task.getGradleCommand().set(toGradleCommand(getProject().getGradle()));
				task.getBridgeTaskPath().set(getBridgeTaskPath());
				task.getAdditionalGradleArguments().set(getAdditionalBuildArguments());
			});
		});

		getProject().getTasks().addRule(getObjects().newInstance(VisualStudioIdeBridge.class, this, extension.getProjects(), getProject()));

		getProject().getPluginManager().withPlugin("dev.nokee.c-application", this::registerNativeComponentProjects);
		getProject().getPluginManager().withPlugin("dev.nokee.cpp-application", this::registerNativeComponentProjects);
		getProject().getPluginManager().withPlugin("dev.nokee.c-library", this::registerNativeComponentProjects);
		getProject().getPluginManager().withPlugin("dev.nokee.cpp-library", this::registerNativeComponentProjects);
	}

	private void registerNativeComponentProjects(AppliedPlugin appliedPlugin) {
		ComponentCollection<Component> components = Cast.uncheckedCast("of type erasure", getProject().getExtensions().getByType(ComponentCollection.class));
		val extension = getProject().getExtensions().getByType(VisualStudioIdeProjectExtension.class);

		components.configureEach(Component.class, component -> {
			if (component instanceof BaseComponent) {
				BaseComponent<?> componentInternal = (BaseComponent<?>) component;
				// TODO: baseName could change between now and when it's finalized...
				extension.getProjects().register(componentInternal.getBaseName().get(), visualStudioProject -> {
					val visualStudioProjectInternal = (DefaultVisualStudioIdeProject) visualStudioProject;
					componentInternal.getSourceCollection().forEach(sourceSet -> {
						visualStudioProjectInternal.getSourceFiles().from(sourceSet.getAsFileTree());
					});
					visualStudioProjectInternal.getHeaderFiles().from(getProject().fileTree("src/main/headers", it -> it.include("*")));
					if (component instanceof DefaultNativeLibraryComponent) {
						visualStudioProjectInternal.getHeaderFiles().from(getProject().fileTree("src/main/public", it -> it.include("*")));
					}
					visualStudioProjectInternal.getBuildFiles().from(getBuildFiles());

					visualStudioProject.target(VisualStudioIdeProjectConfiguration.of(VisualStudioIdeConfiguration.of("Default"), VisualStudioIdePlatforms.X64), target -> {
						Provider<Binary> binary = componentInternal.getDevelopmentVariant().flatMap(Variant::getDevelopmentBinary);

						target.getProductLocation().set(binary.flatMap(it -> {
							if (it instanceof ExecutableBinary) {
								return ((ExecutableBinary) it).getLinkTask().get().getLinkedFile();
							} else if (it instanceof SharedLibraryBinary) {
								return ((SharedLibraryBinary) it).getLinkTask().get().getLinkedFile();
							} else if (it instanceof StaticLibraryBinary) {
								return ((StaticLibraryBinary) it).getCreateTask().get().getOutputFile();
							}
							throw unsupportedBinaryType(it);
						}));
						target.getProperties().put("ConfigurationType", binary.map(this::toConfigurationType));
						target.getProperties().put("UseDebugLibraries", true);
						target.getProperties().put("PlatformToolset", "v142");
						target.getProperties().put("CharacterSet", "Unicode");
						target.getProperties().put("LinkIncremental", true);
						target.getItemProperties().maybeCreate("ClCompile")
							.put("AdditionalIncludeDirectories", binary.flatMap(it -> {
								if (it instanceof BaseNativeBinary) {
									return ((BaseNativeBinary) it).getHeaderSearchPaths().map(this::toSemiColonSeperatedPaths);
								}
								throw unsupportedBinaryType(it);
							}))
							.put("LanguageStandard", getProviders().provider(() -> {
								if (binary.get() instanceof BaseNativeBinary) {
									val it = ((BaseNativeBinary) binary.get()).getCompileTasks().withType(CppCompile.class).getElements().get().iterator();
									if (it.hasNext()) {
										val compileTask = it.next();
										return compileTask.getCompilerArgs().get().stream().filter(arg -> arg.matches("^[-/]std:c++.+")).findFirst().map(a -> {
											if (a.endsWith("c++14")) {
												return "stdcpp14";
											} else if (a.endsWith("c++17")) {
												return "stdcpp17";
											} else if (a.endsWith("c++latest")) {
												return "stdcpplatest";
											}
											return "Default";
										}).orElse("Default");
									}
									return null;
								}
								throw unsupportedBinaryType(binary.get());
							}));
					});
				});
			}
		});
	}

	private static IllegalArgumentException unsupportedBinaryType(Binary binary) {
		return new IllegalArgumentException(String.format("Unsupported binary '%s'.", binary.getClass().getSimpleName()));
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
