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
package dev.nokee.ide.xcode.internal.rules;

import com.google.common.collect.ImmutableList;
import dev.nokee.core.exec.CommandLine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import dev.nokee.ide.xcode.XcodeIdeBuildConfiguration;
import dev.nokee.ide.xcode.XcodeIdeGroup;
import dev.nokee.ide.xcode.XcodeIdeProductType;
import dev.nokee.ide.xcode.XcodeIdeProductTypes;
import dev.nokee.ide.xcode.XcodeIdeProject;
import dev.nokee.ide.xcode.XcodeIdeProjectExtension;
import dev.nokee.ide.xcode.XcodeIdeTarget;
import dev.nokee.ide.xcode.internal.DefaultXcodeIdeBuildConfiguration;
import dev.nokee.ide.xcode.internal.DefaultXcodeIdeGroup;
import dev.nokee.ide.xcode.internal.DefaultXcodeIdeTarget;
import dev.nokee.language.base.HasSource;
import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.nativebase.HasHeaders;
import dev.nokee.language.swift.internal.SupportSwiftSourceSetTag;
import dev.nokee.model.internal.ModelElement;
import dev.nokee.model.internal.ModelMapAdapters;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.HasBaseName;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.VariantInternal;
import dev.nokee.platform.ios.IosResourceSet;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.ios.internal.DefaultIosApplicationVariant;
import dev.nokee.platform.ios.internal.IosApplicationBundleInternal;
import dev.nokee.platform.ios.internal.SignedIosApplicationBundle;
import dev.nokee.platform.ios.internal.SignedIosApplicationBundleInternal;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.BaseNativeBinary;
import dev.nokee.platform.nativebase.internal.HasOutputFile;
import dev.nokee.platform.nativebase.internal.OperatingSystemOperations;
import dev.nokee.runtime.nativebase.BinaryLinkage;
import dev.nokee.runtime.nativebase.BuildType;
import dev.nokee.testing.xctest.internal.BaseXCTestTestSuiteComponent;
import dev.nokee.testing.xctest.internal.DefaultUiTestXCTestTestSuiteComponent;
import dev.nokee.testing.xctest.internal.DefaultUnitTestXCTestTestSuiteComponent;
import dev.nokee.testing.xctest.internal.IosXCTestBundle;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectContainer;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Transformer;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.file.FileVisitDetails;
import org.gradle.api.file.FileVisitor;
import org.gradle.api.file.ProjectLayout;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.reflect.TypeOf;
import org.gradle.api.tasks.TaskContainer;
import org.gradle.language.objectivec.tasks.ObjectiveCCompile;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import static dev.nokee.language.base.internal.SourceAwareComponentUtils.sourceViewOf;
import static dev.nokee.language.nativebase.internal.SupportLanguageSourceSet.hasLanguageSupport;
import static dev.nokee.runtime.nativebase.BuildType.BUILD_TYPE_COORDINATE_AXIS;
import static dev.nokee.runtime.nativebase.OperatingSystemFamily.OPERATING_SYSTEM_COORDINATE_AXIS;
import static dev.nokee.utils.TransformerUtils.to;

public final class CreateNativeComponentXcodeIdeProject implements Action<ModelMapAdapters.ModelElementIdentity> {
	private final XcodeIdeProjectExtension extension;
	private final ProviderFactory providerFactory;
	private final ObjectFactory objectFactory;
	private final ProjectLayout projectLayout;
	private final TaskContainer taskContainer;
	private final ProjectIdentifier projectIdentifier;

	public CreateNativeComponentXcodeIdeProject(XcodeIdeProjectExtension extension, ProviderFactory providerFactory, ObjectFactory objectFactory, ProjectLayout projectLayout, TaskContainer taskContainer, ProjectIdentifier projectIdentifier) {
		this.extension = extension;
		this.providerFactory = providerFactory;
		this.objectFactory = objectFactory;
		this.projectLayout = projectLayout;
		this.taskContainer = taskContainer;
		this.projectIdentifier = projectIdentifier;
	}

	@Override
	public void execute(ModelMapAdapters.ModelElementIdentity knownComponent) {
		registerXcodeIdeProjectIfAbsent(extension.getProjects(), projectIdentifier.getName().toString()).configure(configureXcodeIdeProject(knownComponent));
	}

	private static NamedDomainObjectProvider<XcodeIdeProject> registerXcodeIdeProjectIfAbsent(NamedDomainObjectContainer<XcodeIdeProject> container, String name) {
		if (isXcodeIdeProjectAbsent(container, name)) {
			return container.register(name);
		}
		return container.named(name);
	}

	private static boolean isXcodeIdeProjectAbsent(NamedDomainObjectContainer<XcodeIdeProject> container, String name) {
		return !StreamSupport.stream(container.getCollectionSchema().getElements().spliterator(), false).anyMatch(it -> it.getName().equals(name));
	}

	private Action<XcodeIdeProject> configureXcodeIdeProject(ModelMapAdapters.ModelElementIdentity knownComponent) {
		return new Action<XcodeIdeProject>() {
			@Override
			public void execute(XcodeIdeProject xcodeProject) {
				xcodeProject.getTargets().addAllLater(asCollectionProvider(knownComponent.asProvider().map(to(new org.gradle.api.reflect.TypeOf<VariantAwareComponent<?>>() {})).flatMap(CreateNativeComponentXcodeIdeProject.this.toXcodeIdeTargets())));
				xcodeProject.getGroups().addLater(knownComponent.asProvider().map(to(new TypeOf<VariantAwareComponent<?>>() {})).flatMap(CreateNativeComponentXcodeIdeProject.this::toXcodeIdeGroup));
			}
		};
	}

	@SuppressWarnings("unchecked")
	private <T> Provider<? extends Iterable<T>> asCollectionProvider(Provider<? extends Iterable<? extends T>> collectionProvider) {
		// TODO: Only on early Gradle
		return (Provider<? extends Iterable<T>>) objectFactory.listProperty(Object.class).value(collectionProvider);
	}

	private Provider<XcodeIdeGroup> toXcodeIdeGroup(VariantAwareComponent<?> component) {
		return providerFactory.provider(new Callable<XcodeIdeGroup>() {
			@Override
			public XcodeIdeGroup call() throws Exception {
				val result = new DefaultXcodeIdeGroup(((HasBaseName) component).getBaseName().get(), objectFactory);
				result.getSources().from(sourceViewOf(component).flatMap(CreateNativeComponentXcodeIdeProject.this::toSource));
				return result;
			}
		});
	}

	private FileCollection toSource(LanguageSourceSet sourceSet) {
		if (sourceSet instanceof IosResourceSet) {
			return objectFactory.fileCollection()
				.from(((IosResourceSet) sourceSet).getSources().getAsFileTree().matching(it -> it.include("*.lproj/*.storyboard")))
				.from(((IosResourceSet) sourceSet).getSources().getAsFileTree().matching(it -> it.exclude("*.lproj", "*.xcassets/**")))
				.from((Callable<List<File>>)() -> {
					List<File> result = new ArrayList<>();
					((IosResourceSet) sourceSet).getSources().getAsFileTree().visit(new FileVisitor() {
						@Override
						public void visitDir(FileVisitDetails details) {
							if (details.getName().endsWith(".xcassets")) {
								result.add(details.getFile());
							}
						}

						@Override
						public void visitFile(FileVisitDetails details) {
							// ignores
						}
					});
					return result;
				});
		} else if (sourceSet instanceof HasHeaders) {
			return ((HasSource) sourceSet).getSource().getAsFileTree().plus(((HasHeaders) sourceSet).getHeaders().getAsFileTree());
		}

		// TODO: We should just ignore the source set.
		throw new UnsupportedOperationException();
	}

	private Transformer<Provider<List<XcodeIdeTarget>>, VariantAwareComponent<?>> toXcodeIdeTargets() {
		return new Transformer<Provider<List<XcodeIdeTarget>>, VariantAwareComponent<?>>() {
			@Override
			public Provider<List<XcodeIdeTarget>> transform(VariantAwareComponent<?> component) {
				return component.getVariants().getElements().flatMap(new ToXcodeIdeTargets(component));
			}
		};
	}

	private class ToXcodeIdeTargets implements Transformer<Provider<List<XcodeIdeTarget>>, Set<? extends Variant>> {
		private final boolean hasMultipleLinkages;
		private final VariantAwareComponent<?> component;

		ToXcodeIdeTargets(VariantAwareComponent<?> component) {
			this.component = component;
			this.hasMultipleLinkages = component.getBuildVariants().get().stream().map(buildVariant -> ((BuildVariantInternal) buildVariant).getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS)).distinct().count() > 1;
		}

		private OperatingSystemOperations operatingSystemOperations(BuildVariantInternal buildVariant) {
			return OperatingSystemOperations.of(buildVariant.getAxisValue(OPERATING_SYSTEM_COORDINATE_AXIS));
		}

		@Override
		public Provider<List<XcodeIdeTarget>> transform(Set<? extends Variant> variants) {
			val xcodeIdeTargets = new HashMap<String, XcodeIdeTarget>();
			variants.stream().map(VariantInternal.class::cast).forEach(variantInternal -> {
				val linkage = variantInternal.getBuildVariant().getAxisValue(BinaryLinkage.BINARY_LINKAGE_COORDINATE_AXIS);
				val osOperations = operatingSystemOperations(variantInternal.getBuildVariant());
				val target = xcodeIdeTargets.computeIfAbsent(targetName(component, linkage), createXcodeIdeTarget(osOperations, linkage));

				target.getBuildConfigurations().addLater(createXcodeIdeBuildConfiguration(variantInternal));
				target.getSources().from(sourceViewOf(component).flatMap(CreateNativeComponentXcodeIdeProject.this::toSource));
			});

			return providerFactory.provider(() -> ImmutableList.copyOf(xcodeIdeTargets.values()));
		}

		private Provider<? extends XcodeIdeBuildConfiguration> createXcodeIdeBuildConfiguration(VariantInternal variantInternal) {
			return providerFactory.provider(new Callable<DefaultXcodeIdeBuildConfiguration>() {
				@Override
				public DefaultXcodeIdeBuildConfiguration call() throws Exception {
					val buildType = buildType(variantInternal.getBuildVariant());
					val xcodeConfiguration = new DefaultXcodeIdeBuildConfiguration(buildType.getName(), objectFactory);

					val binaries = variantInternal.getBinaries().withType(BaseNativeBinary.class);
					xcodeConfiguration.getProductLocation().set(variantInternal.getDevelopmentBinary().flatMap(toProductLocation()));

					if (component instanceof DefaultUnitTestXCTestTestSuiteComponent) {
						xcodeConfiguration.getBuildSettings()
							.put("BUNDLE_LOADER", "$(TEST_HOST)")
							// FIXME: The TEST_HOST should be set in theory but doesn't seems to work as expected in practice.
//							.put("TEST_HOST", "$(BUILT_PRODUCTS_DIR)/ObjectiveCIosApplicationUnitTest.app/ObjectiveCIosApplicationUnitTest")
							// Codesign
							.put("INFOPLIST_FILE", "src/unitTest/resources/Info.plist") // TODO: Add RegularFileProperty to hold this location
							.put("IPHONEOS_DEPLOYMENT_TARGET", 13.2)
							.put("PRODUCT_BUNDLE_IDENTIFIER", ((BaseXCTestTestSuiteComponent) component).getProductBundleIdentifier())
							.put("PRODUCT_NAME", "$(TARGET_NAME)")
							.put("TARGETED_DEVICE_FAMILY", "1,2")
							.put("SDKROOT", "iphoneos")
							.put("USER_HEADER_SEARCH_PATHS", getXCTestHeaderSearchPaths())
							.put("FRAMEWORK_SEARCH_PATHS", getXCTestFrameworkPaths())
							.put("COMPILER_INDEX_STORE_ENABLE", "YES")
							.put("USE_HEADERMAP", "NO");
					} else if (component instanceof DefaultUiTestXCTestTestSuiteComponent) {
						xcodeConfiguration.getBuildSettings()
							.put("BUNDLE_LOADER", "$(TEST_HOST)")
							// Codesign
							.put("INFOPLIST_FILE", "src/uiTest/resources/Info.plist") // TODO: Add RegularFileProperty to hold this file location
							.put("IPHONEOS_DEPLOYMENT_TARGET", 13.2)
							.put("PRODUCT_BUNDLE_IDENTIFIER", ((BaseXCTestTestSuiteComponent) component).getProductBundleIdentifier())
							.put("PRODUCT_NAME", "$(TARGET_NAME)")
							.put("TARGETED_DEVICE_FAMILY", "1,2")
							.put("SDKROOT", "iphoneos")
							.put("USER_HEADER_SEARCH_PATHS", getXCTestHeaderSearchPaths())
							.put("FRAMEWORK_SEARCH_PATHS", getXCTestFrameworkPaths())
							.put("COMPILER_INDEX_STORE_ENABLE", "YES")
							.put("USE_HEADERMAP", "NO")
							.put("TEST_TARGET_NAME", ((DefaultUiTestXCTestTestSuiteComponent) component).getTestedComponent().flatMap(it -> ((DefaultIosApplicationComponent)it).getModuleName()));
					}
					xcodeConfiguration.getBuildSettings()
						.put("PRODUCT_NAME", "$(TARGET_NAME)")
						.put("HEADER_SEARCH_PATHS", binaries.getElements().flatMap(toHeaderSearchPaths()))
						.put("FRAMEWORK_SEARCH_PATHS", binaries.getElements().flatMap(toFrameworkSearchPaths()))
						.put("COMPILER_INDEX_STORE_ENABLE", "YES")
						.put("USE_HEADERMAP", "NO")
						// TODO: We shouldn't need toString?
						.put("GRADLE_IDE_PROJECT_NAME", ((ModelElement) component).getIdentifier().getName().toString());

					if (variantInternal instanceof DefaultIosApplicationVariant) {
						xcodeConfiguration.getBuildSettings()
							.put("BUNDLE_LOADER", "$(TEST_HOST)")
							// Codesign
							.put("INFOPLIST_FILE", "src/main/resources/Info.plist")
							.put("IPHONEOS_DEPLOYMENT_TARGET", 13.2)
							.put("PRODUCT_BUNDLE_IDENTIFIER", ((DefaultIosApplicationVariant) variantInternal).getProductBundleIdentifier())
							.put("PRODUCT_NAME", "$(TARGET_NAME)")
							.put("TARGETED_DEVICE_FAMILY", "1,2")
							.put("SDKROOT", "iphoneos")

							// Oh boy, talk about hacking Xcode! Let's capture some important information here.
							// The indexing is taken care by SourceKit, started in background by Xcode.
							// It uses the build settings to create the compiler args which are then used on each compilation unit.
							// This means the C headers are ignored from this process.
							// Through this process SourceKit gets access to the symbols within the compilation unit.
							// At this stage, the indexing looks complete, but in fact, we only have access to the symbols.
							// We have access to their location inside the source files.
							// However, the process seems to be completely blind with regards to #import/#include inside C header files.
							// For example, you can _jump to definition_ from a source file to a C header file through its #import/#include.
							// But we can't _jump to definition_ from a C header file to the same C header file through its #import/#include.
							// We can however _jump to definition_ on any symbols in both C header files and compilation unit.
							// My guess is the process used by SourceKit parsed a preprocessed result of the compilation unit.
							// The #import/#include macros are already resolved.
							//
							// As an experiment, you can trick Xcode to treat the C header files are a compilation unit.
							// To do so, change the known type of the C header files to something that can be compiled, i.e. Objective-C sources.
							// Looking at the SourceKit logs, we can see the file being picked up by the normal process.
							// The side effect of this approach is the C header files will be shown as Objective-C sources.
							// That is pretty bad when we want to achieve a vanilla experience with Xcode.
							//
							// Comparing the SourceKit log of a normal Xcode target with our indexer target, we see that SourceKit uses `-index-store-path`.
							// We can use the OTHER_CFLAGS to pass an array like: ["-index-store-path", "$(BUILD_ROOT)/../../Index/DataStore"].
							// The basic idea is to point `-index-store-path` to the folders `Index/DataStore` under the project-specific derived data.
							// Since there is no build settings pointing to the project-specific derived data, we need to pick another close enough variable.
							// BUILD_ROOT point to only 2 directory deep inside the derived data directory.
							// There are other build settings we can use but that one is good enough.
							// This solution is clean, but not clean enough.
							//
							// It happens the `-index-store-path` is managed by the COMPILER_INDEX_STORE_ENABLE build setting.
							// On a vanilla Xcode project the default value seems to be _YES_.
							// However, on our indexer target, the default value seems to be _NO_.
							// If we explicitly set the build setting to _YES_, there is no need to use OTHER_CFLAGS.
							//
							// IMPORTANT: The Index/DataStore seems to get out of sync when doing a lot of trial and error with our indexer target.
							// It's best to nuke the project-specific derived data directory before each attempt.
							// It's also important to quit Xcode completely between attempt to avoid any cached states.
							//
							// NOTE: It is possible to compute the index store ahead of time.
							// If the index store is coming from a different machine, there may be a need to relocate the data.
							// For relocating, we can use https://github.com/lyft/index-import
							// At the moment, it's best to let Xcode compute the index store as it's the most transparent and provide a more vanilla experience.
							// Computing the index store outside of Xcode is a performance optimization and should be dealt with when it becomes a bottleneck.
							.put("COMPILER_INDEX_STORE_ENABLE", "YES")

							// The headermap is another feature that seems to hinder indexing.
							// The observation shows the behavior is a bit arbitrary on which files are not indexed properly.
							.put("USE_HEADERMAP", "NO");
					}

					if (hasSwiftCapability()) {
						xcodeConfiguration.getBuildSettings()
							.put("SWIFT_VERSION", "5.2")
							.put("SWIFT_INCLUDE_PATHS", binaries.getElements().flatMap(toImportSearchPaths()));
					}

					return xcodeConfiguration;
				}

				private boolean hasSwiftCapability() {
					return ((ModelElement) variantInternal).getParents().anyMatch(hasLanguageSupport(SupportSwiftSourceSetTag.class));
				}

				public Transformer<Provider<? extends FileSystemLocation>, Binary> toProductLocation() {
					return new Transformer<Provider<? extends FileSystemLocation>, Binary>() {
						@Override
						public Provider<? extends FileSystemLocation> transform(Binary binary) {
							if (binary instanceof ExecutableBinary) {
								return ((ExecutableBinary) binary).getLinkTask().get().getLinkedFile();
							} else if (binary instanceof SharedLibraryBinary) {
								return ((SharedLibraryBinary) binary).getLinkTask().get().getLinkedFile();
							} else if (binary instanceof StaticLibraryBinary) {
								return ((StaticLibraryBinary) binary).getCreateTask().get().getOutputFile();
							} else if (binary instanceof SignedIosApplicationBundle) {
								return ((SignedIosApplicationBundleInternal) binary).getApplicationBundleLocation();
							} else if (binary instanceof IosApplicationBundleInternal) {
								return ((IosApplicationBundleInternal) binary).getApplicationBundleLocation();
							} else if (binary instanceof IosXCTestBundle) {
								return ((IosXCTestBundle) binary).getXCTestBundleLocation();
							} else if (binary instanceof HasOutputFile) {
								return ((HasOutputFile) binary).getOutputFile();
							}
							throw unsupportedBinaryType(binary);
						}

						private IllegalArgumentException unsupportedBinaryType(Binary binary) {
							return new IllegalArgumentException(String.format("Unsupported binary '%s'.", binary.getClass().getSimpleName()));
						}
					};
				}

				private Transformer<Provider<String>, Set<? extends BaseNativeBinary>> toImportSearchPaths() {
					return new Transformer<Provider<String>, Set<? extends BaseNativeBinary>>() {
						@Override
						public Provider<String> transform(Set<? extends BaseNativeBinary> binaries) {
							val result = objectFactory.setProperty(FileSystemLocation.class);
							binaries.stream().map(BaseNativeBinary::getImportSearchPaths).forEach(result::addAll);
							return result.map(this::toSpaceSeparatedList);
						}

						private String toSpaceSeparatedList(Set<FileSystemLocation> paths) {
							return paths.stream().map(location -> location.getAsFile().getAbsolutePath()).collect(Collectors.joining(" "));
						}
					};
				}

				private Transformer<Provider<String>, Set<? extends BaseNativeBinary>> toHeaderSearchPaths() {
					return new Transformer<Provider<String>, Set<? extends BaseNativeBinary>>() {
						@Override
						public Provider<String> transform(Set<? extends BaseNativeBinary> binaries) {
							val result = objectFactory.setProperty(FileSystemLocation.class);
							binaries.stream().map(BaseNativeBinary::getHeaderSearchPaths).forEach(result::addAll);
							return result.map(this::toSpaceSeparatedList);
						}

						private String toSpaceSeparatedList(Set<FileSystemLocation> paths) {
							return paths.stream().map(location -> location.getAsFile().getAbsolutePath()).collect(Collectors.joining(" "));
						}
					};
				}

				private String getXCTestHeaderSearchPaths() {
					return taskContainer.withType(ObjectiveCCompile.class).stream()
						// TODO: We should reprobe the compiler with proper sysroot to avoid filtering the System includes here.
						.flatMap(it -> Stream.concat(it.getIncludes().getFiles().stream(), it.getSystemIncludes().getFiles().stream().filter(f -> !f.getAbsolutePath().contains("/MacOSX.platform/"))))
						.map(File::getAbsolutePath)
						.collect(Collectors.joining(" "));
				}

				private Transformer<Provider<String>, Set<? extends BaseNativeBinary>> toFrameworkSearchPaths() {
					return new Transformer<Provider<String>, Set<? extends BaseNativeBinary>>() {
						@Override
						public Provider<String> transform(Set<? extends BaseNativeBinary> binaries) {
							val result = objectFactory.setProperty(FileSystemLocation.class);
							binaries.stream().map(BaseNativeBinary::getFrameworkSearchPaths).forEach(result::addAll);
							return result.map(this::toSpaceSeparatedList);
						}

						private String toSpaceSeparatedList(Set<FileSystemLocation> paths) {
							return paths.stream().map(location -> location.getAsFile().getAbsolutePath()).collect(Collectors.joining(" "));
						}
					};
				}

				private String getXCTestFrameworkPaths() {
					return getSdkPath() + " " + getSdkPlatformPath();
				}

				private String getSdkPath() {
					val sdkPath = CommandLine.of("xcrun", "--sdk", "iphonesimulator", "--show-sdk-path").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().getAsString().trim();
					return sdkPath + "/System/Library/Frameworks";
				}

				private String getSdkPlatformPath() {
					val sdkPlatformPath = CommandLine.of("xcrun", "--sdk", "iphonesimulator", "--show-sdk-platform-path").execute(new ProcessBuilderEngine()).waitFor().assertNormalExitValue().getStandardOutput().getAsString().trim();
					return sdkPlatformPath + "/Developer/Library/Frameworks";
				}

				private BuildType buildType(BuildVariantInternal buildVariant) {
					if (buildVariant.hasAxisValue(BUILD_TYPE_COORDINATE_AXIS)) {
						return buildVariant.getAxisValue(BUILD_TYPE_COORDINATE_AXIS);
					}
					return BuildType.named("Default");
				}
			});
		}

		private Function<String, XcodeIdeTarget> createXcodeIdeTarget(OperatingSystemOperations osOperations, BinaryLinkage linkage) {
			return name -> {
				val target = new DefaultXcodeIdeTarget(name, objectFactory);

				if (component instanceof DefaultIosApplicationComponent) {
					target.getProductName().set(((DefaultIosApplicationComponent) component).getModuleName());
					target.getProductReference().set(((DefaultIosApplicationComponent) component).getModuleName().map(it -> it + ".app"));
					target.getProductType().set(XcodeIdeProductTypes.APPLICATION);
				} else if (component instanceof DefaultUnitTestXCTestTestSuiteComponent) {
					target.getProductName().set(((DefaultUnitTestXCTestTestSuiteComponent) component).getModuleName());
					target.getProductReference().set(((DefaultUnitTestXCTestTestSuiteComponent) component).getModuleName().map(it -> it + ".xctest"));
					target.getProductType().set(XcodeIdeProductTypes.UNIT_TEST);
				} else if (component instanceof DefaultUiTestXCTestTestSuiteComponent) {
					target.getProductName().set(((DefaultUiTestXCTestTestSuiteComponent)component).getModuleName());
					target.getProductReference().set(((DefaultUiTestXCTestTestSuiteComponent)component).getModuleName().map(it -> it + ".xctest"));
					target.getProductType().set(XcodeIdeProductTypes.UI_TEST);
				} else {
					target.getProductName().set(((HasBaseName) component).getBaseName());
					target.getProductReference().set(((HasBaseName) component).getBaseName().map(toProductReference(osOperations, linkage)));
					target.getProductType().set(toProductType(linkage));
				}

				return target;
			};
		}

		private Transformer<String, String> toProductReference(OperatingSystemOperations osOperations, BinaryLinkage linkage) {
			return baseName -> {
				if (linkage.isShared()) {
					return osOperations.getSharedLibraryName(baseName);
				} else if (linkage.isStatic()) {
					return osOperations.getStaticLibraryName(baseName);
				} else if (linkage.isExecutable()) {
					return osOperations.getExecutableName(baseName);
				}
				throw unsupportedLinkage(linkage);
			};
		}

		private XcodeIdeProductType toProductType(BinaryLinkage linkage) {
			if (linkage.isShared()) {
				return XcodeIdeProductTypes.DYNAMIC_LIBRARY;
			} else if (linkage.isStatic()) {
				return XcodeIdeProductTypes.STATIC_LIBRARY;
			} else if (linkage.isExecutable()) {
				return XcodeIdeProductTypes.TOOL;
			}
			throw unsupportedLinkage(linkage);
		}

		private IllegalArgumentException unsupportedLinkage(BinaryLinkage linkage) {
			return new IllegalArgumentException(String.format("Unsupported linkage '%s'.", linkage));
		}

		private String targetName(VariantAwareComponent<?> component, BinaryLinkage linkage) {
			if (hasMultipleLinkages) {
				return ((HasBaseName) component).getBaseName().get() + StringUtils.capitalize(linkage.getName());
			}
			return ((HasBaseName) component).getBaseName().get();
		}
	}
}
