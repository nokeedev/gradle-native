package dev.nokee.ide.visualstudio.internal.rules;

import dev.nokee.ide.visualstudio.*;
import dev.nokee.ide.visualstudio.internal.DefaultVisualStudioIdeProject;
import dev.nokee.ide.visualstudio.internal.DefaultVisualStudioIdeTarget;
import dev.nokee.ide.visualstudio.internal.tasks.GenerateVisualStudioIdeProjectTask;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.cpp.tasks.CppCompile;
import dev.nokee.language.nativebase.NativeHeaderSet;
import dev.nokee.model.KnownDomainObject;
import dev.nokee.model.internal.core.ModelIdentifier;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.BaseComponent;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.jni.JniLibrary;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.BaseNativeBinary;
import dev.nokee.platform.nativebase.internal.BaseTargetBuildType;
import dev.nokee.platform.nativebase.internal.DefaultBinaryLinkage;
import dev.nokee.platform.nativebase.internal.NamedTargetBuildType;
import dev.nokee.runtime.nativebase.internal.DefaultMachineArchitecture;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileTree;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.file.RegularFile;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Specs;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static dev.nokee.platform.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static java.util.stream.Collectors.joining;

public final class CreateNativeComponentVisualStudioIdeProject implements Action<KnownDomainObject<BaseComponent<?>>> {
	private final VisualStudioIdeProjectExtension extension;
	private final ProjectLayout projectLayout;
	private final ObjectFactory objectFactory;
	private final ProviderFactory providerFactory;

	public CreateNativeComponentVisualStudioIdeProject(VisualStudioIdeProjectExtension extension, ProjectLayout projectLayout, ObjectFactory objectFactory, ProviderFactory providerFactory) {
		this.extension = extension;
		this.projectLayout = projectLayout;
		this.objectFactory = objectFactory;
		this.providerFactory = providerFactory;
	}

	@Override
	public void execute(KnownDomainObject<BaseComponent<?>> knownComponent) {
		// TODO: Do something about the casting of DomainObjectIdentifier
		extension.getProjects().register(((ModelIdentifier<?>)knownComponent.getIdentifier()).getPath().getName(), configureVisualStudioIdeProject(knownComponent));
	}

	private Action<VisualStudioIdeProject> configureVisualStudioIdeProject(KnownDomainObject<BaseComponent<?>> knownComponent) {
		return new Action<VisualStudioIdeProject>() {
			@Override
			public void execute(VisualStudioIdeProject visualStudioProject) {
				val visualStudioProjectInternal = (DefaultVisualStudioIdeProject) visualStudioProject;
				visualStudioProjectInternal.getGeneratorTask().configure(vcxprojFilenameToMatchComponentBaseName(knownComponent.flatMap(BaseComponent::getBaseName)));

				visualStudioProjectInternal.getSourceFiles().from(knownComponent.flatMap(this::componentSources));
				visualStudioProjectInternal.getHeaderFiles().from(knownComponent.flatMap(this::componentHeaders));
				visualStudioProjectInternal.getTargets().addAllLater(CreateNativeComponentVisualStudioIdeProject.this.asGradleListProvider(knownComponent.flatMap(CreateNativeComponentVisualStudioIdeProject.this::toVisualStudioIdeTargets)));
			}

			private Provider<List<? extends FileTree>> componentSources(BaseComponent<?> component) {
				return sourceViewOf(component).filter(Specs.negate(this::forHeaderSets)).map(ProviderUtils.map(LanguageSourceSet::getAsFileTree));
			}

			private Provider<List<? extends FileTree>> componentHeaders(BaseComponent<?> component) {
				return sourceViewOf(component).filter(this::forHeaderSets).map(ProviderUtils.map(LanguageSourceSet::getAsFileTree));
			}

			private boolean forHeaderSets(LanguageSourceSet sourceSet) {
				return sourceSet instanceof NativeHeaderSet;
			}

			private Action<GenerateVisualStudioIdeProjectTask> vcxprojFilenameToMatchComponentBaseName(Provider<String> baseName) {
				return task -> task.getProjectLocation().set(projectLayout.getProjectDirectory().file(baseName.map(it -> it + ".vcxproj")));
			}
		};
	}

	private Provider<List<VisualStudioIdeTarget>> asGradleListProvider(Provider<List<? extends VisualStudioIdeTarget>> visualStudioIdeTargets) {
		val result = objectFactory.listProperty(VisualStudioIdeTarget.class);
		result.set(visualStudioIdeTargets);
		return result;
	}

	private Provider<List<VisualStudioIdeTarget>> toVisualStudioIdeTargets(BaseComponent<?> component) {
		return component.getVariants().flatMap(new ToVisualStudioIdeTargets(component));
	}

	private class ToVisualStudioIdeTargets implements Transformer<Iterable<VisualStudioIdeTarget>, Variant> {
		private final BaseComponent<?> component;
		private final Set<DefaultBinaryLinkage> allLinkages;

		ToVisualStudioIdeTargets(BaseComponent<?> component) {
			this.component = component;
			this.allLinkages = component.getBuildVariants().get().stream().map(it -> it.getAxisValue(DefaultBinaryLinkage.DIMENSION_TYPE)).collect(Collectors.toSet());
		}

		@Override
		public Iterable<VisualStudioIdeTarget> transform(Variant variant) {
			// Ignore non-shared linkage variant when multiple linkage are available
			if (allLinkages.size() > 1 && !variant.getBuildVariant().hasAxisOf(DefaultBinaryLinkage.SHARED)) {
				return Collections.emptyList();
			}

			val variantInternal = (VariantInternal) variant;
			val buildType = buildType(variantInternal);
			val machineArchitecture = machineArchitecture(variantInternal);
			val projectConfiguration = projectConfiguration(buildType, machineArchitecture);
			val target = new DefaultVisualStudioIdeTarget(projectConfiguration, objectFactory);

			val binary = developmentBinary(variant);
			target.getProductLocation().set(binary.flatMap(toProductLocation()));
			target.getProperties().put("ConfigurationType", binary.flatMap(toConfigurationType()));
			target.getProperties().put("UseDebugLibraries", true);
			target.getProperties().put("PlatformToolset", "v142");
			target.getProperties().put("CharacterSet", "Unicode");
			target.getProperties().put("LinkIncremental", true);
			target.getItemProperties().maybeCreate("ClCompile")
				.put("AdditionalIncludeDirectories", binary.flatMap(toAdditionalIncludeDirectories()))
				.put("LanguageStandard", binary.flatMap(toLanguageStandard()));
			target.getItemProperties().maybeCreate("Link")
				.put("SubSystem", binary.flatMap(toSubSystem()));

			return Collections.singletonList(target);
		}

		private Provider<Binary> developmentBinary(Variant variant) {
			if (variant instanceof JniLibrary) {
				return ProviderUtils.fixed(((JniLibrary) variant).getSharedLibrary());
			}
			return variant.getDevelopmentBinary();
		}

		private NamedTargetBuildType buildType(VariantInternal variantInternal) {
			if (variantInternal.getBuildVariant().hasAxisValue(BaseTargetBuildType.DIMENSION_TYPE)) {
				return (NamedTargetBuildType) variantInternal.getBuildVariant().getAxisValue(BaseTargetBuildType.DIMENSION_TYPE);
			}
			return new NamedTargetBuildType("default");
		}

		private DefaultMachineArchitecture machineArchitecture(VariantInternal variantInternal) {
			return variantInternal.getBuildVariant().getAxisValue(DefaultMachineArchitecture.DIMENSION_TYPE);
		}

		private VisualStudioIdeProjectConfiguration projectConfiguration(NamedTargetBuildType buildType, DefaultMachineArchitecture machineArchitecture) {
			VisualStudioIdePlatform idePlatform = null;
			if (machineArchitecture.is64Bit()) {
				idePlatform = VisualStudioIdePlatforms.X64;
			} else if (machineArchitecture.is32Bit()) {
				idePlatform = VisualStudioIdePlatforms.WIN32;
			} else {
				throw new IllegalArgumentException("Unsupported architecture for Visual Studio IDE.");
			}

			return VisualStudioIdeProjectConfiguration.of(VisualStudioIdeConfiguration.of(buildType.getName()), idePlatform);
		}

		private Transformer<Provider<String>, Binary> toSubSystem() {
			return new Transformer<Provider<String>, Binary>() {
				@Override
				public Provider<String> transform(Binary binary) {
					if (binary instanceof ExecutableBinary) {
						return ((ExecutableBinary) binary).getLinkTask().get().getLinkerArgs().flatMap(this::asVisualStudioIdeSubSystemValue);
					}
					return null;
				}

				private Provider<String> asVisualStudioIdeSubSystemValue(List<String> args) {
					return providerFactory.provider(ofVisualStudioIdeSubSystemValue(args));
				}

				private Callable<String> ofVisualStudioIdeSubSystemValue(List<String> args) {
					return () -> args.stream()
						.filter(this::forSubSystemLinkerFlag)
						.findFirst() // TODO: We may want to use the last one, it depends how Visual Studio deal with flag duplicate
						.map(this::withoutLinkFlagPrefix)
						.orElse("Default");
				}

				private boolean forSubSystemLinkerFlag(String arg) {
					return arg.matches("^[-/]SUBSYSTEM:.+");
				}

				private String withoutLinkFlagPrefix(String subSystemLinkerFlag) {
					return StringUtils.capitalize(subSystemLinkerFlag.substring(11).toLowerCase());
				}
			};
		}

		private Transformer<Provider<String>, Binary> toLanguageStandard() {
			return new Transformer<Provider<String>, Binary>() {
				@Override
				public Provider<String> transform(Binary binary) {
					if (binary instanceof BaseNativeBinary) {
						return cppCompileTask((BaseNativeBinary) binary).map(this::compilerArgsToLanguageStandard).orElse(null);
					}
					throw unsupportedBinaryType(binary);
				}

				private Optional<CppCompile> cppCompileTask(BaseNativeBinary binary) {
					val iter = binary.getCompileTasks().withType(CppCompile.class).get().iterator();
					if (iter.hasNext()) {
						return Optional.of(iter.next());
					}
					return Optional.empty();
				}

				private Provider<String> compilerArgsToLanguageStandard(CppCompile task) {
					return task.getCompilerArgs().flatMap(this::asVisualStudioIdeLanguageStandardValue);
				}

				private Provider<String> asVisualStudioIdeLanguageStandardValue(List<String> args) {
					return providerFactory.provider(ofVisualStudioIdeLanguageStandardValue(args));
				}

				private Callable<String> ofVisualStudioIdeLanguageStandardValue(List<String> args) {
					return () -> args.stream()
						.filter(this::forStdCppFlag)
						.findFirst()
						.map(this::toIntellisenseLanguageStandardValue)
						.orElse("Default");
				}

				private boolean forStdCppFlag(String arg) {
					return arg.matches("^[-/]std:c++.+");
				}

				private String toIntellisenseLanguageStandardValue(String stdCppFlag) {
					if (stdCppFlag.endsWith("c++14")) {
						return "stdcpp14";
					} else if (stdCppFlag.endsWith("c++17")) {
						return "stdcpp17";
					} else if (stdCppFlag.endsWith("c++latest")) {
						return "stdcpplatest";
					}
					return "Default";
				}
			};
		}

		private Transformer<Provider<String>, Binary> toConfigurationType() {
			return binary -> {
				return providerFactory.provider(() -> {
					if (binary instanceof SharedLibraryBinary) {
						return "DynamicLibrary";
					} else if (binary instanceof StaticLibraryBinary) {
						return "StaticLibrary";
					} else if (binary instanceof ExecutableBinary) {
						return "Application";
					}
					throw unsupportedBinaryType(binary);
				});
			};
		}

		private Transformer<Provider<String>, Binary> toAdditionalIncludeDirectories() {
			return new Transformer<Provider<String>, Binary>() {
				@Override
				public Provider<String> transform(Binary binary) {
					if (binary instanceof BaseNativeBinary) {
						return ((BaseNativeBinary) binary).getHeaderSearchPaths().map(this::toSemiColonSeparatedPaths);
					}
					throw unsupportedBinaryType(binary);
				}

				private String toSemiColonSeparatedPaths(Iterable<? extends FileSystemLocation> it) {
					return StreamSupport.stream(it.spliterator(), false).map(this::quotedAbsolutePath).collect(joining(";"));
				}

				private String quotedAbsolutePath(FileSystemLocation location) {
					return "\"" + location.getAsFile().getAbsolutePath() + "\"";
				}
			};
		}

		private Transformer<Provider<RegularFile>, Binary> toProductLocation() {
			return binary -> {
				if (binary instanceof ExecutableBinary) {
					return ((ExecutableBinary) binary).getLinkTask().get().getLinkedFile();
				} else if (binary instanceof SharedLibraryBinary) {
					return ((SharedLibraryBinary) binary).getLinkTask().get().getLinkedFile();
				} else if (binary instanceof StaticLibraryBinary) {
					return ((StaticLibraryBinary) binary).getCreateTask().get().getOutputFile();
				}
				throw unsupportedBinaryType(binary);
			};
		}

		private IllegalArgumentException unsupportedBinaryType(Binary binary) {
			return new IllegalArgumentException(String.format("Unsupported binary '%s'.", binary.getClass().getSimpleName()));
		}
	}
}
