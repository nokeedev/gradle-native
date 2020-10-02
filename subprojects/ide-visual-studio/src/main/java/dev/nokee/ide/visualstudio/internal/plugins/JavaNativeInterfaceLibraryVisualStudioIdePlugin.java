package dev.nokee.ide.visualstudio.internal.plugins;

import dev.nokee.ide.visualstudio.*;
import dev.nokee.ide.visualstudio.internal.DefaultVisualStudioIdeProject;
import dev.nokee.language.base.internal.SourceSet;
import dev.nokee.language.c.internal.CHeaderSet;
import dev.nokee.language.cpp.internal.CppHeaderSet;
import dev.nokee.language.cpp.tasks.CppCompile;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.jni.JniLibraryExtension;
import dev.nokee.platform.jni.internal.JniLibraryExtensionInternal;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.BaseNativeBinary;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public class JavaNativeInterfaceLibraryVisualStudioIdePlugin implements Plugin<Project> {
	private static final VisualStudioIdeConfiguration DEFAULT_CONFIGURATION = VisualStudioIdeConfiguration.of("default");
	private final ProviderFactory providerFactory;
	private final ProjectLayout projectLayout;

	@Inject
	public JavaNativeInterfaceLibraryVisualStudioIdePlugin(ProviderFactory providerFactory, ProjectLayout projectLayout) {
		this.providerFactory = providerFactory;
		this.projectLayout = projectLayout;
	}

	@Override
	public void apply(Project project) {
		val extension = project.getExtensions().getByType(VisualStudioIdeProjectExtension.class);
		val library = (JniLibraryExtensionInternal) project.getExtensions().getByType(JniLibraryExtension.class);

		extension.getProjects().register(project.getName(), vsProject -> {
			val visualStudioProject = (DefaultVisualStudioIdeProject) vsProject;

			visualStudioProject.getGeneratorTask().configure(task -> {
				Provider<RegularFile> projectLocation = projectLayout.getProjectDirectory().file(library.getComponent().getBaseName().map(it -> it + ".vcxproj"));
				task.getProjectLocation().set(projectLocation);
			});

			visualStudioProject.getSourceFiles().from(providerFactory.provider(() -> library.getComponent().getSourceCollection().stream().filter(it -> !(it instanceof CHeaderSet || it instanceof CppHeaderSet)).map(SourceSet::getAsFileTree).collect(Collectors.toList())));

			visualStudioProject.getHeaderFiles().from(providerFactory.provider(() -> library.getComponent().getSourceCollection().stream().filter(it -> (it instanceof CHeaderSet || it instanceof CppHeaderSet) && !it.getName().equals("jvm") && !it.getName().equals("jni")).map(SourceSet::getAsFileTree).collect(Collectors.toList())));
			library.getBuildVariants().get().forEach(buildVariant -> {
				visualStudioProject.target(asConfiguration(buildVariant), target -> {
					Provider<Binary> binary = library.getComponent().getDevelopmentVariant().flatMap(Variant::getDevelopmentBinary);

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
					target.getProperties().put("ConfigurationType", binary.map(JavaNativeInterfaceLibraryVisualStudioIdePlugin::toConfigurationType));
					target.getProperties().put("UseDebugLibraries", true);
					target.getProperties().put("PlatformToolset", "v142");
					target.getProperties().put("CharacterSet", "Unicode");
					target.getProperties().put("LinkIncremental", true);
					target.getItemProperties().maybeCreate("ClCompile")
						.put("AdditionalIncludeDirectories", binary.flatMap(it -> {
							if (it instanceof BaseNativeBinary) {
								return ((BaseNativeBinary) it).getHeaderSearchPaths().map(JavaNativeInterfaceLibraryVisualStudioIdePlugin::toSemiColonSeparatedPaths);
							}
							throw unsupportedBinaryType(it);
						}))
						.put("LanguageStandard", providerFactory.provider(() -> {
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
					target.getItemProperties().maybeCreate("Link").put("SubSystem", providerFactory.provider(() -> {
						if (binary.get() instanceof ExecutableBinary) {
							return ((ExecutableBinary) binary.get()).getLinkTask().get().getLinkerArgs().get().stream().filter(arg -> arg.matches("^[-/]SUBSYSTEM:.+")).findFirst().map(a -> {
								return StringUtils.capitalize(a.substring(11).toLowerCase());
							}).orElse(null);
						}
						return null;
					}));
				});
			});
		});
	}

	private static String toSemiColonSeparatedPaths(Iterable<? extends FileSystemLocation> it) {
		return StreamSupport.stream(it.spliterator(), false).map(a -> "\"" + a.getAsFile().getAbsolutePath() + "\"").collect(Collectors.joining(";"));
	}

	private static IllegalArgumentException unsupportedBinaryType(Binary binary) {
		return new IllegalArgumentException(String.format("Unsupported binary '%s'.", binary.getClass().getSimpleName()));
	}

	private static String toConfigurationType(Binary binary) {
		if (binary instanceof SharedLibraryBinary) {
			return "DynamicLibrary";
		} else if (binary instanceof StaticLibraryBinary) {
			return "StaticLibrary";
		} else if (binary instanceof ExecutableBinary) {
			return "Application";
		}
		throw new IllegalArgumentException(String.format("Unknown binary type '%s'.", binary.getClass().getSimpleName()));
	}

	private static VisualStudioIdeProjectConfiguration asConfiguration(BuildVariantInternal buildVariant) {
		return VisualStudioIdeProjectConfiguration.of(DEFAULT_CONFIGURATION, toArchitecture(buildVariant.getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE)));
	}

	private static VisualStudioIdePlatform toArchitecture(DefaultMachineArchitecture architecture) {
		if (architecture.is32Bit()) {
			return VisualStudioIdePlatforms.WIN32;
		} else if (architecture.is64Bit()) {
			return VisualStudioIdePlatforms.X64;
		}
		throw new IllegalArgumentException();
	}
}
