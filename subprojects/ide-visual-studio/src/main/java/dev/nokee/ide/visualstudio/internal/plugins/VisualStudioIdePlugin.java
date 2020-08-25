package dev.nokee.ide.visualstudio.internal.plugins;

import com.google.common.collect.ImmutableList;
import dev.nokee.ide.base.internal.*;
import dev.nokee.ide.base.internal.plugins.AbstractIdePlugin;
import dev.nokee.ide.visualstudio.*;
import dev.nokee.ide.visualstudio.internal.*;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.c.CHeaderSet;
import dev.nokee.language.cpp.CppHeaderSet;
import dev.nokee.language.cpp.tasks.CppCompile;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.DomainObjectStore;
import dev.nokee.platform.base.internal.NamedDomainObjectIdentity;
import dev.nokee.platform.base.internal.plugins.ProjectStorePlugin;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.BaseNativeBinary;
import dev.nokee.platform.nativebase.internal.BaseTargetBuildType;
import dev.nokee.platform.nativebase.internal.NamedTargetBuildType;
import dev.nokee.runtime.nativebase.TargetBuildType;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Rule;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.plugins.ide.internal.IdeProjectMetadata;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dev.nokee.utils.ProjectUtils.getPrefixableProjectPath;
import static java.util.Collections.emptyList;

public abstract class VisualStudioIdePlugin extends AbstractIdePlugin<VisualStudioIdeProject> {
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
		registerNativeComponentProjects();
	}

	private void registerNativeComponentProjects() {
		getProject().getPluginManager().apply(ProjectStorePlugin.class);
		val store = getProject().getExtensions().getByType(DomainObjectStore.class);
		val extension = getProject().getExtensions().getByType(VisualStudioIdeProjectExtension.class);

		val v = getObjects().listProperty(VisualStudioIdeProject.class);
		v.value(store.flatMap(new Transformer<Iterable<? extends VisualStudioIdeProject>, Object>() {
			@Override
			public Iterable<? extends VisualStudioIdeProject> transform(Object it) {
				if (BaseComponent.class.isAssignableFrom(it.getClass())) {
					return ImmutableList.of(createVisualStudioIdeProject((BaseComponent<?>) it));
				}
				return emptyList();
			}
		}));
		v.disallowChanges();
		v.finalizeValueOnRead();
		extension.getProjects().addAllLater(v);

		store.whenElementKnown(BaseComponent.class, it -> {
			registerIdeProject(((NamedDomainObjectIdentity)it.getIdentity()).getName());
		});
	}

	private VisualStudioIdeProject createVisualStudioIdeProject(BaseComponent<?> component) {
		val visualStudioProject = getObjects().newInstance(DefaultVisualStudioIdeProject.class, component.getName());

		visualStudioProject.getGeneratorTask().configure(task -> {
			Provider<RegularFile> projectLocation = getLayout().getProjectDirectory().file(component.getBaseName().map(it -> it + ".vcxproj"));
			task.getProjectLocation().set(projectLocation);
		});

		visualStudioProject.getSourceFiles().from(getProviders().provider(() -> component.getSourceCollection().stream().filter(it -> !(it instanceof CHeaderSet || it instanceof CppHeaderSet)).map(LanguageSourceSet::getAsFileTree).collect(Collectors.toList())));

		visualStudioProject.getHeaderFiles().from(getProviders().provider(() -> component.getSourceCollection().stream().filter(it -> it instanceof CHeaderSet || it instanceof CppHeaderSet).map(LanguageSourceSet::getAsFileTree).collect(Collectors.toList())));

		val buildTypes = component.getBuildVariants().get().stream().map(b -> b.getAxisValue(BaseTargetBuildType.DIMENSION_TYPE)).collect(Collectors.toSet()); // TODO Maybe use linkedhashset to keep the ordering
		for (TargetBuildType buildType : buildTypes) {
			visualStudioProject.target(VisualStudioIdeProjectConfiguration.of(VisualStudioIdeConfiguration.of(((NamedTargetBuildType)buildType).getName()), VisualStudioIdePlatforms.X64), target -> {
				Provider<Binary> binary = component.getDevelopmentVariant().flatMap(Variant::getDevelopmentBinary);

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
				target.getItemProperties().maybeCreate("Link").put("SubSystem", getProviders().provider(() -> {
					if (binary.get() instanceof ExecutableBinary) {
						return ((ExecutableBinary) binary.get()).getLinkTask().get().getLinkerArgs().get().stream().filter(arg -> arg.matches("^[-/]SUBSYSTEM:.+")).findFirst().map(a -> {
							return StringUtils.capitalize(a.substring(11).toLowerCase());
						}).orElse(null);
					}
					return null;
				}));
			});
		}

		return visualStudioProject;
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
