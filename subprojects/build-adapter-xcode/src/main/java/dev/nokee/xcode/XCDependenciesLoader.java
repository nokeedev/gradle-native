/*
 * Copyright 2023 the original author or authors.
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
package dev.nokee.xcode;

import com.google.common.collect.ImmutableSet;
import dev.nokee.buildadapter.xcode.internal.plugins.XcodeDependenciesService;
import dev.nokee.utils.Optionals;
import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXFrameworksBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXHeadersBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXResourcesBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXShellScriptBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXSourcesBuildPhase;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;
import dev.nokee.xcode.objects.targets.PBXTarget;

import javax.annotation.Nullable;
import java.nio.file.Path;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static com.google.common.base.Preconditions.checkArgument;
import static dev.nokee.xcode.XCDependenciesLoader.CoordinateDependency.Type.explicit;
import static dev.nokee.xcode.XCDependenciesLoader.CoordinateDependency.Type.implicit;

public final class XCDependenciesLoader implements XCLoader<Set<XCDependency>, XCTargetReference> {
	private final XCLoader<PBXTarget, XCTargetReference> targetLoader;
	private final XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> fileReferencesLoader;
	private final XCDependencyCoordinateLookup dependencyFactory;

	public XCDependenciesLoader(XCLoader<PBXTarget, XCTargetReference> targetLoader, XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> fileReferencesLoader, XCDependencyCoordinateLookup dependencyFactory) {
		this.targetLoader = targetLoader;
		this.fileReferencesLoader = fileReferencesLoader;
		this.dependencyFactory = dependencyFactory;
	}

	public interface XCDependencyCoordinateLookup {
		@Nullable
		XcodeDependenciesService.Coordinate forFile(XCFileReference reference);

		@Nullable
		XcodeDependenciesService.Coordinate forTarget(XCTargetReference reference);
	}

	@Override
	public Set<XCDependency> load(XCTargetReference reference) {
		PBXTarget target = targetLoader.load(reference);

		return Stream.concat( //
			target.getBuildPhases().stream() //
				.flatMap(buildPhase -> buildPhase.getFiles().stream() //
						.flatMap(buildFile -> {
							return Stream.concat(Optionals.stream(buildFile.getFileRef())
								.map(it -> fileReferencesLoader.load(reference.getProject()).get((PBXReference) it)) //
								.flatMap(file -> Stream.of(file).map(dependencyFactory::forFile).filter(Objects::nonNull)
									.map(it -> new CoordinateDependency(it, implicit().via(file).inBuildPhase(buildPhase)))),
								Optionals.stream(buildFile.getProductRef()).map(it -> new SwiftPMDependency(it.getProductName(), implicit().via(it.getPackageReference()).inBuildPhase(buildPhase)))); //
						})
				), //
			target.getDependencies().stream() //
				.map(it -> it.getTarget().map(t -> toTargetReference(reference.getProject(), t)).orElseGet(() -> toTargetReference(reference.getProject(), it.getTargetProxy()))) //
				.map(dependencyFactory::forTarget) //
				.filter(Objects::nonNull) //
				.map(it -> new CoordinateDependency(it, explicit())) //
			).collect(ImmutableSet.toImmutableSet());
	}

	private XCTargetReference toTargetReference(XCProjectReference project, PBXTarget target) {
		return new DefaultXCTargetReference(project, target.getName());
	}

	private XCTargetReference toTargetReference(XCProjectReference project, PBXContainerItemProxy targetProxy) {
		checkArgument(PBXContainerItemProxy.ProxyType.TARGET_REFERENCE.equals(targetProxy.getProxyType()), "'targetProxy' is expected to be a target reference");

		return targetProxy.getContainerPortal().accept(new PBXContainerItemProxy.ContainerPortal.Visitor<XCTargetReference>() {
			@Override
			public XCTargetReference visit(PBXProject container) {
				return new DefaultXCTargetReference(project, targetProxy.getRemoteInfo()
					.orElseThrow(XCDependenciesLoader::missingRemoteInfoException));
			}

			@Override
			public XCTargetReference visit(PBXFileReference container) {
				return new DefaultXCTargetReference(new DefaultXCProjectReference(project.load(fileReferencesLoader).get((PBXFileReference) targetProxy.getContainerPortal()).resolve(new XCFileReference.ResolveContext() {
					@Override
					public Path getBuiltProductsDirectory() {
						throw new UnsupportedOperationException("Should not call");
					}

					@Override
					public Path get(String name) {
						if ("SOURCE_ROOT".equals(name)) {
							return project.getLocation().getParent();
						}
						throw new UnsupportedOperationException(String.format("Could not resolve '%s' build setting.", name));
					}
				})), targetProxy.getRemoteInfo().orElseThrow(XCDependenciesLoader::missingRemoteInfoException));
			}
		});
	}

	private static RuntimeException missingRemoteInfoException() {
		return new RuntimeException("Missing 'remoteInfo' on 'targetProxy'.");
	}

	public static final class CoordinateDependency implements XCDependency {
		private final XcodeDependenciesService.Coordinate coordinate;
		private final Type type;

		public CoordinateDependency(XcodeDependenciesService.Coordinate coordinate, Type type) {
			this.coordinate = coordinate;
			this.type = type;
		}

		public interface Type {
			// TODO: implicit dependency via options '-framework CocoaLumberjack' in build settings 'OTHER_LDFLAGS'
			static ImplicitTypeBuilder implicit() {
				return new DefaultImplicitTypeBuilder();
			}

			static Type explicit() {
				return new Type() {
					@Override
					public String toString() {
						return "explicit";
					}
				};
			}
		}

		public XcodeDependenciesService.Coordinate getCoordinate() {
			return coordinate;
		}

		@Override
		public String toString() {
			return coordinate + " (" + type + ")";
		}
	}

	public static final class SwiftPMDependency implements XCDependency {
		private final String productName;
		private final CoordinateDependency.Type type;

		public SwiftPMDependency(String productName, CoordinateDependency.Type type) {
			this.productName = productName;
			this.type = type;
		}

		@Override
		public String toString() {
			return "product '" + productName + "' (" + type + ")";
		}
	}

	public interface ImplicitTypeBuilder {
		ImplicitViaFileTypeBuilder via(XCFileReference file);
		ImplicitViaFileTypeBuilder via(XCRemoteSwiftPackageReference reference);
//		ImplicitViaOptionTypeBuilder via(XCBuildOption option);
	}

	public interface ImplicitViaFileTypeBuilder {
		CoordinateDependency.Type inBuildPhase(PBXBuildPhase buildPhase);
	}

	public interface ImplicitViaOptionTypeBuilder {
		CoordinateDependency.Type inBuildSetting(String buildSetting);
	}

	// Examples:
	//   implicit dependency via options '-framework SQLCipher' in build setting 'OTHER_LDFLAGS'
	//   implicit dependency via file 'Pods_SignalNSE.framework' in build phase 'Link Binary'
	public static final class DefaultImplicitTypeBuilder implements ImplicitTypeBuilder, ImplicitViaFileTypeBuilder, CoordinateDependency.Type {
		private StringBuilder description = new StringBuilder("implicit dependency");

		public ImplicitViaFileTypeBuilder via(XCFileReference file) {
			description.append(" via ").append(toString(file));
			return this;
		}

		@Override
		public ImplicitViaFileTypeBuilder via(XCRemoteSwiftPackageReference reference) {
			description.append(" via ").append(toString(reference));
			return this;
		}

		@Override
		public CoordinateDependency.Type inBuildPhase(PBXBuildPhase buildPhase) {
			description.append(" in ").append(toString(buildPhase));
			return this;
		}

		private static String toString(PBXBuildPhase buildPhase) {
			return buildPhase.accept(new PBXBuildPhase.Visitor<String>() {
				@Override
				public String visit(PBXCopyFilesBuildPhase buildPhase) {
					return "build phase '" + buildPhase.getName().orElse("copy files") + "'";
				}

				@Override
				public String visit(PBXFrameworksBuildPhase buildPhase) {
					return "build phase 'Link Binary'";
				}

				@Override
				public String visit(PBXHeadersBuildPhase buildPhase) {
					return "build phase 'Headers'";
				}

				@Override
				public String visit(PBXResourcesBuildPhase buildPhase) {
					return "build phase 'Copy Resources'";
				}

				@Override
				public String visit(PBXShellScriptBuildPhase buildPhase) {
					return "a build phase";
				}

				@Override
				public String visit(PBXSourcesBuildPhase buildPhase) {
					return "build phase 'Compile Sources'";
				}
			});
		}

		private static String toString(XCFileReference file) {
			return "file '" + file + "'";
		}

		private static String toString(XCRemoteSwiftPackageReference reference) {
			return "package at '" + reference.getRepositoryUrl() + "' which " + reference.getRequirement();
		}

		@Override
		public String toString() {
			return description.toString();
		}
	}
}
