/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.xcode.project;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.reflect.TypeToken;
import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXFrameworksBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXHeadersBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXResourcesBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXShellScriptBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXSourcesBuildPhase;
import dev.nokee.xcode.objects.configuration.BuildSettings;
import dev.nokee.xcode.objects.configuration.XCBuildConfiguration;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.GroupChild;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.files.PBXReferenceProxy;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import dev.nokee.xcode.objects.files.PBXVariantGroup;
import dev.nokee.xcode.objects.files.XCVersionGroup;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import dev.nokee.xcode.objects.targets.PBXAggregateTarget;
import dev.nokee.xcode.objects.targets.PBXLegacyTarget;
import dev.nokee.xcode.objects.targets.PBXNativeTarget;
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.objects.targets.PBXTargetDependency;
import dev.nokee.xcode.objects.targets.ProductType;
import dev.nokee.xcode.objects.targets.ProductTypes;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Consumer;

import static dev.nokee.xcode.objects.configuration.XCConfigurationList.DefaultConfigurationVisibility.HIDDEN;
import static dev.nokee.xcode.objects.configuration.XCConfigurationList.DefaultConfigurationVisibility.VISIBLE;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.BRANCH;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.EXACT;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.RANGE;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.REVISION;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.UP_TO_NEXT_MAJOR_VERSION;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.UP_TO_NEXT_MINOR_VERSION;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.branch;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.exact;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.range;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.revision;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.upToNextMajorVersion;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.upToNextMinorVersion;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;

final class PBXObjectCoders {
	private static final ImmutableList<PBXObjectCoder<?>> ALL_CODERS = ImmutableList.of(
		new PBXProjectCoder(),
		new XCConfigurationListCoder(),
		new PBXFileReferenceCoder(),
		new PBXGroupCoder(),
		new PBXVariantGroupCoder(),
		new XCVersionGroupCoder(),
		new PBXLegacyTargetCoder(),
		new PBXNativeTargetCoder(),
		new PBXAggregateTargetCoder(),
		new XCBuildConfigurationCoder(),
		new PBXShellScriptBuildPhaseCoder(),
		new PBXSourcesBuildPhaseCoder(),
		new PBXHeadersBuildPhaseCoder(),
		new PBXFrameworksBuildPhaseCoder(),
		new PBXResourcesBuildPhaseCoder(),
		new PBXCopyFilesBuildPhaseCoder(),
		new PBXTargetDependencyCoder(),
		new PBXBuildFileCoder(),
		new PBXContainerItemProxyCoder(),
		new PBXReferenceProxyCoder(),
		new ProjectReferenceCoder(),
		new XCRemoteSwiftPackageReferenceCoder(),
		new VersionRequirementCoder(),
		new ExactVersionRequirementCoder(),
		new BranchVersionRequirementCoder(),
		new RevisionVersionRequirementCoder(),
		new RangeVersionRequirementCoder(),
		new UpToNextMinorVersionVersionRequirementCoder(),
		new UpToNextMajorVersionVersionRequirementCoder(),
		new XCSwiftPackageProductDependencyCoder()
	);

	public static PBXObjectCoder<?>[] values() {
		return ALL_CODERS.toArray(new PBXObjectCoder<?>[0]);
	}

	@SuppressWarnings("unchecked")
	static int stableHash(Object o) {
		return ALL_CODERS.stream().filter(it -> it.getType().equals(o.getClass()) && it instanceof HasStableHash).map(it -> (HasStableHash<Object>) it).findFirst().map(it -> it.stableHash(o)).orElse(0);
	}

	private interface HasStableHash<T> {
		int stableHash(T o);
	}

	private static final class PBXProjectCoder implements PBXObjectCoder<PBXProject>, HasStableHash<PBXProject> {
		@Override
		public Class<PBXProject> getType() {
			return PBXProject.class;
		}

		@Override
		public int stableHash(PBXProject value) {
			if (value.getName() != null) {
				return value.getName().hashCode();
			} else {
				return 0;
			}
		}

		@Override
		public PBXProject read(Decoder decoder) {
			val builder = PBXProject.builder();
			decoder.decodeIfPresent("mainGroup", builder::mainGroup);
			decoder.decodeIfPresent("targets", builder::targets);
			decoder.decodeIfPresent("buildConfigurationList", XCConfigurationList.class, builder::buildConfigurations);
			decoder.decodeIfPresent("projectReferences", new TypeToken<Iterable<PBXProject.ProjectReference>>() {}.getType(), builder::projectReferences);
			decoder.decodeIfPresent("packageReferences", new TypeToken<Iterable<XCRemoteSwiftPackageReference>>() {}.getType(), builder::packageReferences);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXProject value) {
			encoder.encode("mainGroup", value.getMainGroup());

			val targets = new ArrayList<>(value.getTargets());
			targets.sort(comparing(PBXTarget::getName, naturalOrder()));
			encoder.encode("targets", targets);
			encoder.encode("buildConfigurationList", value.getBuildConfigurationList());
			encoder.encode("compatibilityVersion", value.getCompatibilityVersion());
			encoder.encode("attributes", ImmutableMap.of("LastUpgradeCheck", "0610"));
			encoder.encode("projectReferences", value.getProjectReferences());
			encoder.encode("packageReferences", value.getPackageReferences());
		}
	}

	private static final class ProjectReferenceCoder implements PBXObjectCoder<PBXProject.ProjectReference> {
		@Override
		public Class<PBXProject.ProjectReference> getType() {
			return PBXProject.ProjectReference.class;
		}

		@Override
		public PBXProject.ProjectReference read(Decoder decoder) {
			val builder = PBXProject.ProjectReference.builder();
			decoder.decodeIfPresent("ProjectRef", builder::projectReference);
			decoder.decodeIfPresent("ProductGroup", builder::productGroup);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXProject.ProjectReference value) {
			encoder.encode("ProjectRef", value.getProjectReference());
			encoder.encode("ProductGroup", value.getProductGroup());
		}
	}

	private static final class XCConfigurationListCoder implements PBXObjectCoder<XCConfigurationList> {
		@Override
		public Class<XCConfigurationList> getType() {
			return XCConfigurationList.class;
		}

		@Override
		public XCConfigurationList read(Decoder decoder) {
			val builder = XCConfigurationList.builder();
			decoder.decodeIfPresent("buildConfigurations", new TypeToken<Iterable<XCBuildConfiguration>>() {}.getType(), builder::buildConfigurations);
			decoder.decodeIfPresent("defaultConfigurationName", String.class, builder::defaultConfigurationName);
			decoder.decodeIfPresent("defaultConfigurationIsVisible", String.class, toVisibility(builder::defaultConfigurationVisibility));
			return builder.build();
		}

		private static Consumer<String> toVisibility(Consumer<? super XCConfigurationList.DefaultConfigurationVisibility> consumer) {
			return value -> {
				if (value.equals("YES") || value.equals("1")) {
					consumer.accept(VISIBLE);
				} else if (value.equals("NO") || value.equals("0")) {
					consumer.accept(HIDDEN);
				} else {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public void write(Encoder encoder, XCConfigurationList value) {
			val buildConfigurations = new ArrayList<>(value.getBuildConfigurationsByName().values());
			buildConfigurations.sort(comparing(XCBuildConfiguration::getName, naturalOrder()));
			encoder.encode("buildConfigurations", buildConfigurations);
			value.getDefaultConfigurationName().ifPresent(it -> encoder.encode("defaultConfigurationName", it));
			encoder.encode("defaultConfigurationIsVisible", value.isDefaultConfigurationIsVisible() ? "YES" : "NO");
		}
	}

	private static final class PBXFileReferenceCoder implements PBXObjectCoder<PBXFileReference>, HasStableHash<PBXFileReference> {
		@Override
		public Class<PBXFileReference> getType() {
			return PBXFileReference.class;
		}

		@Override
		public int stableHash(PBXFileReference value) {
			return Objects.hash(value.getName().orElse(null));
		}

		@Override
		public PBXFileReference read(Decoder decoder) {
			val builder = PBXFileReference.builder();
			decoder.decodeIfPresent("name", String.class, builder::name);
			decoder.decodeIfPresent("path", String.class, builder::path);
			decoder.decodeIfPresent("sourceTree", String.class, it -> builder.sourceTree(PBXSourceTree.of(it)));
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXFileReference value) {
			value.getName().ifPresent(it -> encoder.encode("name", it));
			value.getPath().ifPresent(it -> encoder.encode("path", it));
			encoder.encode("sourceTree", value.getSourceTree().toString());

			value.getExplicitFileType().ifPresent(it -> encoder.encode("explicitFileType", it));
			value.getLastKnownFileType().ifPresent(it -> encoder.encode("lastKnownFileType", it));
		}
	}

	private static final class PBXGroupCoder implements PBXObjectCoder<PBXGroup>, HasStableHash<PBXGroup> {
		@Override
		public Class<PBXGroup> getType() {
			return PBXGroup.class;
		}

		@Override
		public int stableHash(PBXGroup value) {
			return Objects.hash(value.getName().orElse(null));
		}

		@Override
		public PBXGroup read(Decoder decoder) {
			val builder = PBXGroup.builder();
			decoder.decodeIfPresent("name", String.class, builder::name);
			decoder.decodeIfPresent("path", String.class, builder::path);
			decoder.decodeIfPresent("sourceTree", String.class, it -> builder.sourceTree(PBXSourceTree.of(it)));
			decoder.decodeIfPresent("children", builder::children);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXGroup value) {
			value.getName().ifPresent(it -> encoder.encode("name", it));
			value.getPath().ifPresent(it -> encoder.encode("path", it));
			encoder.encode("sourceTree", value.getSourceTree().toString());

			List<GroupChild> children = new ArrayList<>(value.getChildren());
			if (value.getSortPolicy() == PBXGroup.SortPolicy.BY_NAME) {
				children.sort(comparing(o -> o.getName().orElse(null), naturalOrder()));
			}

			encoder.encode("children", children);
		}
	}

	private static final class PBXVariantGroupCoder implements PBXObjectCoder<PBXVariantGroup>, HasStableHash<PBXVariantGroup> {
		@Override
		public Class<PBXVariantGroup> getType() {
			return PBXVariantGroup.class;
		}

		@Override
		public int stableHash(PBXVariantGroup value) {
			return Objects.hash(value.getName().orElse(null));
		}

		@Override
		public PBXVariantGroup read(Decoder decoder) {
			val builder = PBXVariantGroup.builder();
			decoder.decodeIfPresent("name", String.class, builder::name);
			decoder.decodeIfPresent("path", String.class, builder::path);
			decoder.decodeIfPresent("sourceTree", String.class, it -> builder.sourceTree(PBXSourceTree.of(it)));
			decoder.decodeIfPresent("children", builder::children);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXVariantGroup value) {
			value.getName().ifPresent(it -> encoder.encode("name", it));
			value.getPath().ifPresent(it -> encoder.encode("path", it));
			encoder.encode("sourceTree", value.getSourceTree().toString());

			List<GroupChild> children = new ArrayList<>(value.getChildren());
			if (value.getSortPolicy() == PBXVariantGroup.SortPolicy.BY_NAME) {
				children.sort(comparing(o -> o.getName().orElse(null), naturalOrder()));
			}

			encoder.encode("children", children);
		}
	}

	private static final class XCVersionGroupCoder implements PBXObjectCoder<XCVersionGroup>, HasStableHash<XCVersionGroup> {
		@Override
		public Class<XCVersionGroup> getType() {
			return XCVersionGroup.class;
		}

		@Override
		public int stableHash(XCVersionGroup value) {
			return Objects.hash(value.getName().orElse(null));
		}

		@Override
		public XCVersionGroup read(Decoder decoder) {
			val builder = XCVersionGroup.builder();
			decoder.decodeIfPresent("name", String.class, builder::name);
			decoder.decodeIfPresent("path", String.class, builder::path);
			decoder.decodeIfPresent("sourceTree", String.class, it -> builder.sourceTree(PBXSourceTree.of(it)));
			decoder.decodeIfPresent("children", builder::children);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, XCVersionGroup value) {
			value.getName().ifPresent(it -> encoder.encode("name", it));
			value.getPath().ifPresent(it -> encoder.encode("path", it));
			encoder.encode("sourceTree", value.getSourceTree().toString());

			List<GroupChild> children = new ArrayList<>(value.getChildren());
			if (value.getSortPolicy() == PBXVariantGroup.SortPolicy.BY_NAME) {
				children.sort(comparing(o -> o.getName().orElse(null), naturalOrder()));
			}

			encoder.encode("children", children);
		}
	}

	private static final class PBXTargetDependencyCoder implements PBXObjectCoder<PBXTargetDependency>, HasStableHash<PBXTargetDependency> {
		@Override
		public Class<PBXTargetDependency> getType() {
			return PBXTargetDependency.class;
		}

		@Override
		public int stableHash(PBXTargetDependency value) {
			return PBXObjectCoders.stableHash(value.getTargetProxy());
		}

		@Override
		public PBXTargetDependency read(Decoder decoder) {
			val builder = PBXTargetDependency.builder();
			decoder.decodeIfPresent("name", String.class, builder::name);
			decoder.decodeIfPresent("target", builder::target);
			decoder.decodeIfPresent("targetProxy", builder::targetProxy);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXTargetDependency value) {
			value.getName().ifPresent(it -> encoder.encode("name", it));
			value.getTarget().ifPresent(it -> encoder.encode("target", it));
			encoder.encode("targetProxy", value.getTargetProxy());
		}
	}

	private static final class PBXLegacyTargetCoder implements PBXObjectCoder<PBXLegacyTarget>, HasStableHash<PBXLegacyTarget> {
		@Override
		public Class<PBXLegacyTarget> getType() {
			return PBXLegacyTarget.class;
		}

		@Override
		public int stableHash(PBXLegacyTarget value) {
			return value.getName().hashCode();
		}

		@Override
		public PBXLegacyTarget read(Decoder decoder) {
			val builder = PBXLegacyTarget.builder();
			decoder.decodeIfPresent("name", String.class, builder::name);
			decoder.decodeIfPresent("productType", String.class, ProductTypes::valueOf);
			decoder.decodeIfPresent("productName", String.class, builder::productName);
			decoder.decodeIfPresent("productReference", builder::productReference);
			decoder.decodeIfPresent("buildConfigurationList", XCConfigurationList.class, builder::buildConfigurations);
			decoder.decodeIfPresent("dependencies", new TypeToken<Iterable<PBXTargetDependency>>() {}.getType(), builder::dependencies);
			decoder.decodeIfPresent("buildArgumentsString", String.class, builder::buildArguments);
			decoder.decodeIfPresent("buildToolPath", String.class, builder::buildToolPath);
			decoder.decodeIfPresent("buildWorkingDirectory", String.class, builder::buildWorkingDirectory);
			decoder.decodeIfPresent("passBuildSettingsInEnvironment", String.class, toBoolean(builder::passBuildSettingsInEnvironment));
			return builder.build();
		}

		private static Consumer<String> toBoolean(Consumer<? super Boolean> consumer) {
			return value -> {
				if (value.equals("1")) {
					consumer.accept(true);
				} else if (value.equals("0")) {
					consumer.accept(false);
				} else {
					throw new UnsupportedOperationException();
				}
			};
		}

		@Override
		public void write(Encoder encoder, PBXLegacyTarget value) {
			encoder.encode("name", value.getName());
			value.getProductType().ifPresent(it -> encoder.encode("productType", it.toString()));
			value.getProductName().ifPresent(it -> encoder.encode("productName", it));
			value.getProductReference().ifPresent(it -> encoder.encode("productReference", it));
			encoder.encode("buildPhases", value.getBuildPhases());
			encoder.encode("buildConfigurationList", value.getBuildConfigurationList());
			encoder.encode("dependencies", value.getDependencies());

			encoder.encode("buildArgumentsString", value.getBuildArgumentsString());
			encoder.encode("buildToolPath", value.getBuildToolPath());
			if (value.getBuildWorkingDirectory() != null) {
				encoder.encode("buildWorkingDirectory", value.getBuildWorkingDirectory());
			}
			encoder.encode("passBuildSettingsInEnvironment", value.isPassBuildSettingsInEnvironment() ? "1" : "0");
		}
	}

	private static final class PBXNativeTargetCoder implements PBXObjectCoder<PBXNativeTarget>, HasStableHash<PBXNativeTarget> {
		@Override
		public Class<PBXNativeTarget> getType() {
			return PBXNativeTarget.class;
		}

		@Override
		public int stableHash(PBXNativeTarget value) {
			return value.getName().hashCode();
		}

		@Override
		public PBXNativeTarget read(Decoder decoder) {
			val builder = PBXNativeTarget.builder();
			decoder.decodeIfPresent("name", String.class, builder::name);
			decoder.decodeIfPresent("productType", String.class, toProductType(builder::productType));
			decoder.decodeIfPresent("productName", String.class, builder::productName);
			decoder.decodeIfPresent("productReference", builder::productReference);
			decoder.decodeIfPresent("buildPhases", builder::buildPhases);
			decoder.decodeIfPresent("buildConfigurationList", XCConfigurationList.class, builder::buildConfigurations);
			decoder.decodeIfPresent("dependencies", new TypeToken<Iterable<PBXTargetDependency>>() {}.getType(), builder::dependencies);
			decoder.decodeIfPresent("packageProductDependencies", new TypeToken<Iterable<XCSwiftPackageProductDependency>>() {}.getType(), builder::packageProductDependencies);
			return builder.build();
		}

		private static Consumer<String> toProductType(Consumer<? super ProductType> consumer) {
			return it -> consumer.accept(ProductTypes.valueOf(it));
		}

		@Override
		public void write(Encoder encoder, PBXNativeTarget value) {
			encoder.encode("name", value.getName());
			value.getProductType().ifPresent(it -> encoder.encode("productType", it.toString()));
			value.getProductName().ifPresent(it -> encoder.encode("productName", it));
			value.getProductReference().ifPresent(it -> encoder.encode("productReference", it));
			encoder.encode("buildPhases", value.getBuildPhases());
			encoder.encode("buildConfigurationList", value.getBuildConfigurationList());
			encoder.encode("dependencies", value.getDependencies());
			encoder.encode("packageProductDependencies", value.getPackageProductDependencies());
		}
	}

	private static final class PBXAggregateTargetCoder implements PBXObjectCoder<PBXAggregateTarget>, HasStableHash<PBXAggregateTarget> {
		@Override
		public Class<PBXAggregateTarget> getType() {
			return PBXAggregateTarget.class;
		}

		@Override
		public int stableHash(PBXAggregateTarget value) {
			return value.getName().hashCode();
		}

		@Override
		public PBXAggregateTarget read(Decoder decoder) {
			val builder = PBXAggregateTarget.builder();
			decoder.decodeIfPresent("name", String.class, builder::name);
			decoder.decodeIfPresent("buildPhases", builder::buildPhases);
			decoder.decodeIfPresent("buildConfigurationList", XCConfigurationList.class, builder::buildConfigurations);
			decoder.decodeIfPresent("dependencies", new TypeToken<Iterable<PBXTargetDependency>>() {}.getType(), builder::dependencies);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXAggregateTarget value) {
			encoder.encode("name", value.getName());
			encoder.encode("buildPhases", value.getBuildPhases());
			encoder.encode("buildConfigurationList", value.getBuildConfigurationList());
			encoder.encode("dependencies", value.getDependencies());
		}
	}

	private static final class PBXCopyFilesBuildPhaseCoder implements PBXObjectCoder<PBXCopyFilesBuildPhase> {
		@Override
		public Class<PBXCopyFilesBuildPhase> getType() {
			return PBXCopyFilesBuildPhase.class;
		}

		@Override
		public PBXCopyFilesBuildPhase read(Decoder decoder) {
			val builder = PBXCopyFilesBuildPhase.builder();
			decoder.decodeIfPresent("dstPath", String.class, builder::dstPath);
			decoder.decodeIfPresent("dstSubfolderSpec", Integer.class, toSubFolderSpec(builder::dstSubfolderSpec));
			decoder.decodeIfPresent("files", new TypeToken<Iterable<PBXBuildFile>>() {}.getType(), builder::files);
			return builder.build();
		}

		private static Consumer<Integer> toSubFolderSpec(Consumer<? super PBXCopyFilesBuildPhase.SubFolder> consumer) {
			return value -> {
				consumer.accept(Arrays.stream(PBXCopyFilesBuildPhase.SubFolder.values()).filter(candidate -> candidate.getValue() == value).findFirst().orElseThrow(UnsupportedOperationException::new));
			};
		}

		@Override
		public void write(Encoder encoder, PBXCopyFilesBuildPhase value) {
			encoder.encode("dstPath", value.getDstPath());
			encoder.encode("dstSubfolderSpec", value.getDstSubfolderSpec().getValue());
			encoder.encode("files", value.getFiles());
		}
	}

	private static final class XCBuildConfigurationCoder implements PBXObjectCoder<XCBuildConfiguration>, HasStableHash<XCBuildConfiguration> {
		@Override
		public Class<XCBuildConfiguration> getType() {
			return XCBuildConfiguration.class;
		}

		@Override
		public int stableHash(XCBuildConfiguration value) {
			return value.getName().hashCode();
		}

		@Override
		public XCBuildConfiguration read(Decoder decoder) {
			val builder = XCBuildConfiguration.builder();
			decoder.decodeIfPresent("name", String.class, builder::name);
			decoder.decodeIfPresent("buildSettings", new TypeToken<Map<String, Object>>() {}.getType(), toBuildSettings(builder::buildSettings));
			decoder.decodeIfPresent("baseConfigurationReference", builder::baseConfigurationReference);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, XCBuildConfiguration value) {
			encoder.encode("name", value.getName());
			encoder.encode("buildSettings", value.getBuildSettings().asMap());
		}
	}

	private static Consumer<Map<String, Object>> toBuildSettings(Consumer<? super BuildSettings> action) {
		return it -> action.accept(BuildSettings.of(it));
	}

	private static final class PBXShellScriptBuildPhaseCoder implements PBXObjectCoder<PBXShellScriptBuildPhase> {
		@Override
		public Class<PBXShellScriptBuildPhase> getType() {
			return PBXShellScriptBuildPhase.class;
		}

		@Override
		public PBXShellScriptBuildPhase read(Decoder decoder) {
			val builder = PBXShellScriptBuildPhase.builder();
			// 'files' key seems to always be empty
			decoder.decodeIfPresent("name", String.class, builder::name);
			decoder.decodeIfPresent("shellPath", String.class, builder::shellPath);
			decoder.decodeIfPresent("shellScript", String.class, builder::shellScript);
			decoder.decodeIfPresent("inputPaths", new TypeToken<Iterable<String>>() {}.getType(), builder::inputPaths);
			decoder.decodeIfPresent("inputFileListPaths", new TypeToken<Iterable<String>>() {}.getType(), builder::inputFileListPaths);
			decoder.decodeIfPresent("outputPaths", new TypeToken<Iterable<String>>() {}.getType(), builder::outputPaths);
			decoder.decodeIfPresent("outputFileListPaths", new TypeToken<Iterable<String>>() {}.getType(), builder::outputFileListPaths);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXShellScriptBuildPhase value) {
			value.getName().ifPresent(it -> encoder.encode("name", it));
			encoder.encode("inputPaths", value.getInputPaths());
			encoder.encode("outputPaths", value.getOutputPaths());
			encoder.encode("inputFileListPaths", value.getInputFileListPaths());
			encoder.encode("outputFileListPaths", value.getOutputFileListPaths());

			if (value.getShellPath() == null) {
				encoder.encode("shellPath", "/bin/sh");
			} else {
				encoder.encode("shellPath", value.getShellPath());
			}

			if (value.getShellScript() == null) {
				encoder.encode("shellScript", "");
			} else {
				encoder.encode("shellScript", value.getShellScript());
			}
		}
	}

	private static final class PBXSourcesBuildPhaseCoder implements PBXObjectCoder<PBXSourcesBuildPhase> {
		@Override
		public Class<PBXSourcesBuildPhase> getType() {
			return PBXSourcesBuildPhase.class;
		}

		@Override
		public PBXSourcesBuildPhase read(Decoder decoder) {
			val builder = PBXSourcesBuildPhase.builder();
			decoder.decodeIfPresent("files", new TypeToken<Iterable<PBXBuildFile>>() {}.getType(), builder::files);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXSourcesBuildPhase value) {
			encoder.encode("files", value.getFiles());
		}
	}

	private static final class PBXHeadersBuildPhaseCoder implements PBXObjectCoder<PBXHeadersBuildPhase> {
		@Override
		public Class<PBXHeadersBuildPhase> getType() {
			return PBXHeadersBuildPhase.class;
		}

		@Override
		public PBXHeadersBuildPhase read(Decoder decoder) {
			val builder = PBXHeadersBuildPhase.builder();
			decoder.decodeIfPresent("files", new TypeToken<Iterable<PBXBuildFile>>() {}.getType(), builder::files);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXHeadersBuildPhase value) {
			encoder.encode("files", value.getFiles());
		}
	}

	private static final class PBXResourcesBuildPhaseCoder implements PBXObjectCoder<PBXResourcesBuildPhase> {
		@Override
		public Class<PBXResourcesBuildPhase> getType() {
			return PBXResourcesBuildPhase.class;
		}

		@Override
		public PBXResourcesBuildPhase read(Decoder decoder) {
			val builder = PBXResourcesBuildPhase.builder();
			decoder.decodeIfPresent("files", new TypeToken<Iterable<PBXBuildFile>>() {}.getType(), builder::files);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXResourcesBuildPhase value) {
			encoder.encode("files", value.getFiles());
		}
	}

	private static final class PBXFrameworksBuildPhaseCoder implements PBXObjectCoder<PBXFrameworksBuildPhase> {
		@Override
		public Class<PBXFrameworksBuildPhase> getType() {
			return PBXFrameworksBuildPhase.class;
		}

		@Override
		public PBXFrameworksBuildPhase read(Decoder decoder) {
			val builder = PBXFrameworksBuildPhase.builder();
			decoder.decodeIfPresent("files", new TypeToken<Iterable<PBXBuildFile>>() {}.getType(), builder::files);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXFrameworksBuildPhase value) {
			encoder.encode("files", value.getFiles());
		}
	}

	private static final class PBXBuildFileCoder implements PBXObjectCoder<PBXBuildFile>, HasStableHash<PBXBuildFile> {
		@Override
		public Class<PBXBuildFile> getType() {
			return PBXBuildFile.class;
		}

		@Override
		public int stableHash(PBXBuildFile value) {
			return PBXObjectCoders.stableHash(value.getFileRef().map(Object.class::cast)
				.orElseGet(() -> value.getProductRef().get()));
		}

		@Override
		public PBXBuildFile read(Decoder decoder) {
			val builder = PBXBuildFile.builder();
			decoder.decodeIfPresent("fileRef", builder::fileRef);
			decoder.decodeIfPresent("productRef", builder::productRef);
			decoder.decodeIfPresent("settings", new TypeToken<Map<String, Object>>() {}.getType(), builder::settings);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXBuildFile value) {
			value.getFileRef().ifPresent(it -> encoder.encode("fileRef", it));
			value.getProductRef().ifPresent(it -> encoder.encode("productRef", it));
			if (!value.getSettings().isEmpty()) {
				encoder.encode("settings", value.getSettings());
			}
		}
	}

	private static final class PBXContainerItemProxyCoder implements PBXObjectCoder<PBXContainerItemProxy>, HasStableHash<PBXContainerItemProxy> {
		@Override
		public Class<PBXContainerItemProxy> getType() {
			return PBXContainerItemProxy.class;
		}

		@Override
		public int stableHash(PBXContainerItemProxy value) {
			return value.getRemoteGlobalIDString().hashCode();
		}

		@Override
		public PBXContainerItemProxy read(Decoder decoder) {
			val builder = PBXContainerItemProxy.builder();
			builder.containerPortal(() -> decoder.<PBXContainerItemProxy.ContainerPortal>decodeObject("containerPortal")
				.orElseThrow(RuntimeException::new));
			decoder.decodeIfPresent("remoteGlobalIDString", String.class, builder::remoteGlobalId);
			decoder.decodeIfPresent("proxyType", Integer.class, toProxyType(builder::proxyType));
			decoder.decodeIfPresent("remoteInfo", String.class, builder::remoteInfo);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXContainerItemProxy value) {
			encoder.encode("containerPortal", value.getContainerPortal());
			encoder.encode("remoteGlobalIDString", value.getRemoteGlobalIDString());
			encoder.encode("proxyType", value.getProxyType().getIntValue());
			value.getRemoteInfo().ifPresent(it -> encoder.encode("remoteInfo", it));
		}

		private static Consumer<Integer> toProxyType(Consumer<? super PBXContainerItemProxy.ProxyType> consumer) {
			return value -> {
				consumer.accept(Arrays.stream(PBXContainerItemProxy.ProxyType.values()).filter(candidate -> candidate.getIntValue() == value).findFirst().orElseThrow(UnsupportedOperationException::new));
			};
		}
	}

	private static final class PBXReferenceProxyCoder implements PBXObjectCoder<PBXReferenceProxy>, HasStableHash<PBXReferenceProxy> {
		@Override
		public Class<PBXReferenceProxy> getType() {
			return PBXReferenceProxy.class;
		}

		@Override
		public int stableHash(PBXReferenceProxy value) {
			return Objects.hash(value.getName().orElse(null));
		}

		@Override
		public PBXReferenceProxy read(Decoder decoder) {
			val builder = PBXReferenceProxy.builder();
			decoder.decodeIfPresent("name", String.class, builder::name);
			decoder.decodeIfPresent("path", String.class, builder::path);
			decoder.decodeIfPresent("sourceTree", String.class, it -> builder.sourceTree(PBXSourceTree.of(it)));
			decoder.decodeIfPresent("remoteRef", PBXContainerItemProxy.class, builder::remoteReference);
			decoder.decodeIfPresent("fileType", String.class, builder::fileType);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXReferenceProxy value) {
			value.getName().ifPresent(it -> encoder.encode("name", it));
			value.getPath().ifPresent(it -> encoder.encode("path", it));
			encoder.encode("sourceTree", value.getSourceTree().toString());
			encoder.encode("remoteRef", value.getRemoteReference());
			encoder.encode("fileType", value.getFileType());
		}
	}

	private static final class XCRemoteSwiftPackageReferenceCoder implements PBXObjectCoder<XCRemoteSwiftPackageReference>, HasStableHash<XCRemoteSwiftPackageReference> {
		@Override
		public Class<XCRemoteSwiftPackageReference> getType() {
			return XCRemoteSwiftPackageReference.class;
		}

		@Override
		public int stableHash(XCRemoteSwiftPackageReference value) {
			return value.getRepositoryUrl().hashCode();
		}

		@Override
		public XCRemoteSwiftPackageReference read(Decoder decoder) {
			val builder = XCRemoteSwiftPackageReference.builder();
			decoder.decodeIfPresent("repositoryURL", String.class, builder::repositoryUrl);
			decoder.decodeIfPresent("requirement", XCRemoteSwiftPackageReference.VersionRequirement.class, builder::requirement);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, XCRemoteSwiftPackageReference value) {
			encoder.encode("repositoryURL", value.getRepositoryUrl());
			encoder.encode("requirement", value.getRequirement());
		}
	}

	private static final class XCSwiftPackageProductDependencyCoder implements PBXObjectCoder<XCSwiftPackageProductDependency>, HasStableHash<XCSwiftPackageProductDependency> {
		@Override
		public Class<XCSwiftPackageProductDependency> getType() {
			return XCSwiftPackageProductDependency.class;
		}

		@Override
		public int stableHash(XCSwiftPackageProductDependency value) {
			return Objects.hash(value.getProductName(), value.getPackageReference().getRepositoryUrl());
		}

		@Override
		public XCSwiftPackageProductDependency read(Decoder decoder) {
			val builder = XCSwiftPackageProductDependency.builder();
			decoder.decodeIfPresent("productName", String.class, builder::productName);
			decoder.decodeIfPresent("package", XCRemoteSwiftPackageReference.class, builder::packageReference);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, XCSwiftPackageProductDependency value) {
			encoder.encode("productName", value.getProductName());
			encoder.encode("package", value.getPackageReference());
		}
	}

	private static final class VersionRequirementCoder implements PBXObjectCoder<XCRemoteSwiftPackageReference.VersionRequirement> {
		private static final Map<XCRemoteSwiftPackageReference.VersionRequirement.Kind, PBXObjectCoder<? extends XCRemoteSwiftPackageReference.VersionRequirement>> VERSION_REQUIREMENT_CODERS = ImmutableMap.<XCRemoteSwiftPackageReference.VersionRequirement.Kind, PBXObjectCoder<? extends XCRemoteSwiftPackageReference.VersionRequirement>>builder()
			.put(REVISION, new RevisionVersionRequirementCoder())
			.put(BRANCH, new BranchVersionRequirementCoder())
			.put(EXACT, new ExactVersionRequirementCoder())
			.put(RANGE, new RangeVersionRequirementCoder())
			.put(UP_TO_NEXT_MINOR_VERSION, new UpToNextMinorVersionVersionRequirementCoder())
			.put(UP_TO_NEXT_MAJOR_VERSION, new UpToNextMajorVersionVersionRequirementCoder())
			.build();

		@Override
		public Class<XCRemoteSwiftPackageReference.VersionRequirement> getType() {
			return XCRemoteSwiftPackageReference.VersionRequirement.class;
		}

		@Override
		public XCRemoteSwiftPackageReference.VersionRequirement read(Decoder decoder) {
			return Objects.requireNonNull(VERSION_REQUIREMENT_CODERS.get(toKind(decoder.decode("kind", String.class).orElseThrow(RuntimeException::new)))).read(decoder);
		}

		@Override
		@SuppressWarnings("unchecked")
		public void write(Encoder encoder, XCRemoteSwiftPackageReference.VersionRequirement value) {
			((PBXObjectCoder<Object>) Objects.requireNonNull((PBXObjectCoder<? extends Object>) VERSION_REQUIREMENT_CODERS.get(value.getKind()))).write(encoder, value);
		}

		private static XCRemoteSwiftPackageReference.VersionRequirement.Kind toKind(String value) {
			switch (value) {
				case "revision": return REVISION;
				case "branch": return BRANCH;
				case "exactVersion": return EXACT;
				case "versionRange": return RANGE;
				case "upToNextMinorVersion": return UP_TO_NEXT_MINOR_VERSION;
				case "upToNextMajorVersion": return UP_TO_NEXT_MAJOR_VERSION;
				default: throw new UnsupportedOperationException(String.format("Unsupported XCRemoteSwiftPackageReference kind '%s'. Supported reference kinds are: %s", value, "revision, branch, exactVersion, versionRange, upToNextMinorVersion, upToNextMajorVersion"));
			}
		}
	}

	private static final class BranchVersionRequirementCoder implements PBXObjectCoder<XCRemoteSwiftPackageReference.VersionRequirement.Branch> {
		@Override
		public Class<XCRemoteSwiftPackageReference.VersionRequirement.Branch> getType() {
			return XCRemoteSwiftPackageReference.VersionRequirement.Branch.class;
		}

		@Override
		public XCRemoteSwiftPackageReference.VersionRequirement.Branch read(Decoder decoder) {
			assert "branch".equals(decoder.decode("kind", String.class).orElse(null));
			return branch(decoder.decode("branch", String.class).orElse(null));
		}

		@Override
		public void write(Encoder encoder, XCRemoteSwiftPackageReference.VersionRequirement.Branch value) {
			encoder.encode("kind", "branch");
			encoder.encode("branch", value.getBranch());
		}
	}

	private static final class RevisionVersionRequirementCoder implements PBXObjectCoder<XCRemoteSwiftPackageReference.VersionRequirement.Revision> {
		@Override
		public Class<XCRemoteSwiftPackageReference.VersionRequirement.Revision> getType() {
			return XCRemoteSwiftPackageReference.VersionRequirement.Revision.class;
		}

		@Override
		public XCRemoteSwiftPackageReference.VersionRequirement.Revision read(Decoder decoder) {
			assert "revision".equals(decoder.decode("kind", String.class).orElse(null));
			return revision(decoder.decode("revision", String.class).orElse(null));
		}

		@Override
		public void write(Encoder encoder, XCRemoteSwiftPackageReference.VersionRequirement.Revision value) {
			encoder.encode("kind", "revision");
			encoder.encode("revision", value.getRevision());
		}
	}

	private static final class ExactVersionRequirementCoder implements PBXObjectCoder<XCRemoteSwiftPackageReference.VersionRequirement.Exact> {
		@Override
		public Class<XCRemoteSwiftPackageReference.VersionRequirement.Exact> getType() {
			return XCRemoteSwiftPackageReference.VersionRequirement.Exact.class;
		}

		@Override
		public XCRemoteSwiftPackageReference.VersionRequirement.Exact read(Decoder decoder) {
			assert "exactVersion".equals(decoder.decode("kind", String.class).orElse(null));
			return exact(decoder.decode("version", String.class).orElse(null));
		}

		@Override
		public void write(Encoder encoder, XCRemoteSwiftPackageReference.VersionRequirement.Exact value) {
			encoder.encode("kind", "exactVersion");
			encoder.encode("version", value.getVersion());
		}
	}

	private static final class RangeVersionRequirementCoder implements PBXObjectCoder<XCRemoteSwiftPackageReference.VersionRequirement.Range> {
		@Override
		public Class<XCRemoteSwiftPackageReference.VersionRequirement.Range> getType() {
			return XCRemoteSwiftPackageReference.VersionRequirement.Range.class;
		}

		@Override
		public XCRemoteSwiftPackageReference.VersionRequirement.Range read(Decoder decoder) {
			assert "versionRange".equals(decoder.decode("kind", String.class).orElse(null));
			return range(decoder.decode("minimumVersion", String.class).orElse(null), decoder.decode("maximumVersion", String.class).orElse(null));
		}

		@Override
		public void write(Encoder encoder, XCRemoteSwiftPackageReference.VersionRequirement.Range value) {
			encoder.encode("kind", "versionRange");
			encoder.encode("minimumVersion", value.getMinimumVersion());
			encoder.encode("maximumVersion", value.getMaximumVersion());
		}
	}

	private static final class UpToNextMinorVersionVersionRequirementCoder implements PBXObjectCoder<XCRemoteSwiftPackageReference.VersionRequirement.UpToNextMinorVersion> {
		@Override
		public Class<XCRemoteSwiftPackageReference.VersionRequirement.UpToNextMinorVersion> getType() {
			return XCRemoteSwiftPackageReference.VersionRequirement.UpToNextMinorVersion.class;
		}

		@Override
		public XCRemoteSwiftPackageReference.VersionRequirement.UpToNextMinorVersion read(Decoder decoder) {
			assert "upToNextMinorVersion".equals(decoder.decode("kind", String.class).orElse(null));
			return upToNextMinorVersion(decoder.decode("minimumVersion", String.class).orElse(null));
		}

		@Override
		public void write(Encoder encoder, XCRemoteSwiftPackageReference.VersionRequirement.UpToNextMinorVersion value) {
			encoder.encode("kind", "upToNextMinorVersion");
			encoder.encode("minimumVersion", value.getMinimumVersion());
		}
	}

	private static final class UpToNextMajorVersionVersionRequirementCoder implements PBXObjectCoder<XCRemoteSwiftPackageReference.VersionRequirement.UpToNextMajorVersion> {
		@Override
		public Class<XCRemoteSwiftPackageReference.VersionRequirement.UpToNextMajorVersion> getType() {
			return XCRemoteSwiftPackageReference.VersionRequirement.UpToNextMajorVersion.class;
		}

		@Override
		public XCRemoteSwiftPackageReference.VersionRequirement.UpToNextMajorVersion read(Decoder decoder) {
			assert "upToNextMajorVersion".equals(decoder.decode("kind", String.class).orElse(null));
			return upToNextMajorVersion(decoder.decode("minimumVersion", String.class).orElse(null));
		}

		@Override
		public void write(Encoder encoder, XCRemoteSwiftPackageReference.VersionRequirement.UpToNextMajorVersion value) {
			encoder.encode("kind", "upToNextMajorVersion");
			encoder.encode("minimumVersion", value.getMinimumVersion());
		}
	}
}
