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
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import dev.nokee.xcode.objects.files.PBXVariantGroup;
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
import java.util.function.Consumer;

import static com.google.common.collect.Streams.stream;
import static dev.nokee.xcode.objects.configuration.XCConfigurationList.DefaultConfigurationVisibility.HIDDEN;
import static dev.nokee.xcode.objects.configuration.XCConfigurationList.DefaultConfigurationVisibility.VISIBLE;
import static java.util.Comparator.comparing;
import static java.util.Comparator.naturalOrder;
import static java.util.stream.Collectors.toList;

final class PBXObjectCoders {
	private static final ImmutableList<PBXObjectCoder<?>> ALL_CODERS = ImmutableList.of(
		new PBXProjectCoder(),
		new XCConfigurationListCoder(),
		new PBXFileReferenceCoder(),
		new PBXGroupCoder(),
		new PBXVariantGroupCoder(),
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
		new PBXBuildFileCoder()
	);

	public static PBXObjectCoder<?>[] values() {
		return ALL_CODERS.toArray(new PBXObjectCoder<?>[0]);
	}

	private static final class PBXProjectCoder implements PBXObjectCoder<PBXProject> {
		@Override
		public Class<PBXProject> getType() {
			return PBXProject.class;
		}

		@Override
		public PBXProject read(Decoder decoder) {
			val builder = PBXProject.builder();
			decoder.decodeObjectIfPresent("mainGroup", builder::mainGroup);
			decoder.decodeObjectsIfPresent("targets", builder::targets);
			decoder.<XCConfigurationList>decodeObjectIfPresent("buildConfigurationList", builder::buildConfigurations);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXProject value) {
			encoder.encodeObject("mainGroup", value.getMainGroup());

			val targets = new ArrayList<>(value.getTargets());
			targets.sort(comparing(PBXTarget::getName, naturalOrder()));
			encoder.encodeObjects("targets", targets);
			encoder.encodeObject("buildConfigurationList", value.getBuildConfigurationList());
			encoder.encodeString("compatibilityVersion", value.getCompatibilityVersion());
			encoder.encodeMap("attributes", ImmutableMap.of("LastUpgradeCheck", "0610"));
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
			decoder.decodeObjectsIfPresent("buildConfigurations", builder::buildConfigurations);
			decoder.decodeStringIfPresent("defaultConfigurationName", builder::defaultConfigurationName);
			decoder.decodeStringIfPresent("defaultConfigurationIsVisible", toVisibility(builder::defaultConfigurationVisibility));
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
			encoder.encodeObjects("buildConfigurations", buildConfigurations);
			value.getDefaultConfigurationName().ifPresent(it -> encoder.encodeString("defaultConfigurationName", it));
			encoder.encodeString("defaultConfigurationIsVisible", value.isDefaultConfigurationIsVisible() ? "YES" : "NO");
		}
	}

	private static final class PBXFileReferenceCoder implements PBXObjectCoder<PBXFileReference> {
		@Override
		public Class<PBXFileReference> getType() {
			return PBXFileReference.class;
		}

		@Override
		public PBXFileReference read(Decoder decoder) {
			val builder = PBXFileReference.builder();
			decoder.decodeStringIfPresent("name", builder::name);
			decoder.decodeStringIfPresent("path", builder::path);
			decoder.decodeStringIfPresent("sourceTree", it -> builder.sourceTree(PBXSourceTree.of(it).orElseThrow(RuntimeException::new)));
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXFileReference value) {
			value.getName().ifPresent(it -> encoder.encodeString("name", it));
			value.getPath().ifPresent(it -> encoder.encodeString("path", it));
			encoder.encodeString("sourceTree", value.getSourceTree().toString());

			value.getExplicitFileType().ifPresent(it -> encoder.encodeString("explicitFileType", it));
			value.getLastKnownFileType().ifPresent(it -> encoder.encodeString("lastKnownFileType", it));
		}
	}

	private static final class PBXGroupCoder implements PBXObjectCoder<PBXGroup> {
		@Override
		public Class<PBXGroup> getType() {
			return PBXGroup.class;
		}

		@Override
		public PBXGroup read(Decoder decoder) {
			val builder = PBXGroup.builder();
			decoder.decodeStringIfPresent("name", builder::name);
			decoder.decodeStringIfPresent("path", builder::path);
			decoder.decodeStringIfPresent("sourceTree", it -> builder.sourceTree(PBXSourceTree.of(it).orElseThrow(RuntimeException::new)));
			decoder.decodeObjectsIfPresent("children", builder::children);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXGroup value) {
			value.getName().ifPresent(it -> encoder.encodeString("name", it));
			value.getPath().ifPresent(it -> encoder.encodeString("path", it));
			encoder.encodeString("sourceTree", value.getSourceTree().toString());

			List<PBXReference> children = new ArrayList<>(value.getChildren());
			if (value.getSortPolicy() == PBXGroup.SortPolicy.BY_NAME) {
				children.sort(comparing(o -> o.getName().orElse(null), naturalOrder()));
			}

			encoder.encodeObjects("children", children);
		}
	}

	private static final class PBXVariantGroupCoder implements PBXObjectCoder<PBXVariantGroup> {
		@Override
		public Class<PBXVariantGroup> getType() {
			return PBXVariantGroup.class;
		}

		@Override
		public PBXVariantGroup read(Decoder decoder) {
			val builder = PBXVariantGroup.builder();
			decoder.decodeStringIfPresent("name", builder::name);
			decoder.decodeStringIfPresent("path", builder::path);
			decoder.decodeStringIfPresent("sourceTree", it -> builder.sourceTree(PBXSourceTree.of(it).orElseThrow(RuntimeException::new)));
			decoder.decodeObjectsIfPresent("children", builder::children);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXVariantGroup value) {
			value.getName().ifPresent(it -> encoder.encodeString("name", it));
			value.getPath().ifPresent(it -> encoder.encodeString("path", it));
			encoder.encodeString("sourceTree", value.getSourceTree().toString());

			List<PBXReference> children = new ArrayList<>(value.getChildren());
			if (value.getSortPolicy() == PBXVariantGroup.SortPolicy.BY_NAME) {
				children.sort(comparing(o -> o.getName().orElse(null), naturalOrder()));
			}

			encoder.encodeObjects("children", children);
		}
	}

	private static final class PBXTargetDependencyCoder implements PBXObjectCoder<PBXTargetDependency> {
		@Override
		public Class<PBXTargetDependency> getType() {
			return PBXTargetDependency.class;
		}

		@Override
		public PBXTargetDependency read(Decoder decoder) {
			val builder = PBXTargetDependency.builder();
			decoder.decodeStringIfPresent("name", builder::name);
			decoder.decodeObjectIfPresent("target", builder::target);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXTargetDependency value) {
			value.getName().ifPresent(it -> encoder.encodeString("name", it));
			encoder.encodeObject("target", value.getTarget());
		}
	}

	private static final class PBXLegacyTargetCoder implements PBXObjectCoder<PBXLegacyTarget> {
		@Override
		public Class<PBXLegacyTarget> getType() {
			return PBXLegacyTarget.class;
		}

		@Override
		public PBXLegacyTarget read(Decoder decoder) {
			val builder = PBXLegacyTarget.builder();
			decoder.decodeStringIfPresent("name", builder::name);
			decoder.decodeStringIfPresent("productType", ProductTypes::valueOf);
			decoder.decodeStringIfPresent("productName", builder::productName);
			decoder.decodeObjectIfPresent("productReference", builder::productReference);
			decoder.<XCConfigurationList>decodeObjectIfPresent("buildConfigurationList", builder::buildConfigurations);
			decoder.decodeObjectsIfPresent("dependencies", builder::dependencies);
			decoder.decodeStringIfPresent("buildArgumentsString", builder::buildArguments);
			decoder.decodeStringIfPresent("buildToolPath", builder::buildToolPath);
			decoder.decodeStringIfPresent("buildWorkingDirectory", builder::buildWorkingDirectory);
			decoder.decodeStringIfPresent("passBuildSettingsInEnvironment", toBoolean(builder::passBuildSettingsInEnvironment));
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
			encoder.encodeString("name", value.getName());
			value.getProductType().ifPresent(it -> encoder.encodeString("productType", it.toString()));
			value.getProductName().ifPresent(it -> encoder.encodeString("productName", it));
			value.getProductReference().ifPresent(it -> encoder.encodeObject("productReference", it));
			encoder.encodeObjects("buildPhases", value.getBuildPhases());
			encoder.encodeObject("buildConfigurationList", value.getBuildConfigurationList());
			encoder.encodeObjects("dependencies", value.getDependencies());

			encoder.encodeString("buildArgumentsString", value.getBuildArgumentsString());
			encoder.encodeString("buildToolPath", value.getBuildToolPath());
			if (value.getBuildWorkingDirectory() != null) {
				encoder.encodeString("buildWorkingDirectory", value.getBuildWorkingDirectory());
			}
			encoder.encodeString("passBuildSettingsInEnvironment", value.isPassBuildSettingsInEnvironment() ? "1" : "0");
		}
	}

	private static final class PBXNativeTargetCoder implements PBXObjectCoder<PBXNativeTarget> {
		@Override
		public Class<PBXNativeTarget> getType() {
			return PBXNativeTarget.class;
		}

		@Override
		public PBXNativeTarget read(Decoder decoder) {
			val builder = PBXNativeTarget.builder();
			decoder.decodeStringIfPresent("name", builder::name);
			decoder.decodeStringIfPresent("productType", toProductType(builder::productType));
			decoder.decodeStringIfPresent("productName", builder::productName);
			decoder.decodeObjectIfPresent("productReference", builder::productReference);
			decoder.decodeObjectsIfPresent("buildPhases", builder::buildPhases);
			decoder.<XCConfigurationList>decodeObjectIfPresent("buildConfigurationList", builder::buildConfigurations);
			decoder.decodeObjectsIfPresent("dependencies", builder::dependencies);
			return builder.build();
		}

		private static Consumer<String> toProductType(Consumer<? super ProductType> consumer) {
			return it -> consumer.accept(ProductTypes.valueOf(it));
		}

		@Override
		public void write(Encoder encoder, PBXNativeTarget value) {
			encoder.encodeString("name", value.getName());
			value.getProductType().ifPresent(it -> encoder.encodeString("productType", it.toString()));
			value.getProductName().ifPresent(it -> encoder.encodeString("productName", it));
			value.getProductReference().ifPresent(it -> encoder.encodeObject("productReference", it));
			encoder.encodeObjects("buildPhases", value.getBuildPhases());
			encoder.encodeObject("buildConfigurationList", value.getBuildConfigurationList());
			encoder.encodeObjects("dependencies", value.getDependencies());
		}
	}

	private static final class PBXAggregateTargetCoder implements PBXObjectCoder<PBXAggregateTarget> {
		@Override
		public Class<PBXAggregateTarget> getType() {
			return PBXAggregateTarget.class;
		}

		@Override
		public PBXAggregateTarget read(Decoder decoder) {
			val builder = PBXAggregateTarget.builder();
			decoder.decodeStringIfPresent("name", builder::name);
			decoder.decodeObjectsIfPresent("buildPhases", builder::buildPhases);
			decoder.<XCConfigurationList>decodeObjectIfPresent("buildConfigurationList", builder::buildConfigurations);
			decoder.decodeObjectsIfPresent("dependencies", builder::dependencies);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXAggregateTarget value) {
			encoder.encodeString("name", value.getName());
			encoder.encodeObjects("buildPhases", value.getBuildPhases());
			encoder.encodeObject("buildConfigurationList", value.getBuildConfigurationList());
			encoder.encodeObjects("dependencies", value.getDependencies());
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
			decoder.decodeStringIfPresent("dstPath", builder::dstPath);
			decoder.decodeIntegerIfPresent("dstSubfolderSpec", toSubFolderSpec(builder::dstSubfolderSpec));
			decoder.decodeObjectsIfPresent("files", builder::files);
			return builder.build();
		}

		private static Consumer<Integer> toSubFolderSpec(Consumer<? super PBXCopyFilesBuildPhase.SubFolder> consumer) {
			return value -> {
				consumer.accept(Arrays.stream(PBXCopyFilesBuildPhase.SubFolder.values()).filter(candidate -> candidate.getValue() == value).findFirst().orElseThrow(UnsupportedOperationException::new));
			};
		}

		@Override
		public void write(Encoder encoder, PBXCopyFilesBuildPhase value) {
			encoder.encodeString("dstPath", value.getDstPath());
			encoder.encodeInteger("dstSubfolderSpec", value.getDstSubfolderSpec().getValue());
			encoder.encodeObjects("files", value.getFiles());
		}
	}

	private static final class XCBuildConfigurationCoder implements PBXObjectCoder<XCBuildConfiguration> {
		@Override
		public Class<XCBuildConfiguration> getType() {
			return XCBuildConfiguration.class;
		}

		@Override
		public XCBuildConfiguration read(Decoder decoder) {
			val builder = XCBuildConfiguration.builder();
			decoder.decodeStringIfPresent("name", builder::name);
			decoder.decodeMapIfPresent("buildSettings", it -> builder.buildSettings(BuildSettings.of(it)));
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, XCBuildConfiguration value) {
			encoder.encodeString("name", value.getName());
			encoder.encodeMap("buildSettings", value.getBuildSettings().asMap());
		}
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
			decoder.decodeArrayIfPresent("inputPaths", it -> builder.inputPaths(stream(it).map(Object::toString).collect(toList())));
			decoder.decodeArrayIfPresent("outputPaths", it -> builder.inputPaths(stream(it).map(Object::toString).collect(toList())));
			decoder.decodeStringIfPresent("shellPath", builder::shellPath);
			decoder.decodeStringIfPresent("shellScript", builder::shellScript);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXShellScriptBuildPhase value) {
			encoder.encodeObjects("files", value.getFiles());

			encoder.encodeArray("inputPaths", value.getInputPaths());
			encoder.encodeArray("outputPaths", value.getOutputPaths());

			if (value.getShellPath() == null) {
				encoder.encodeString("shellPath", "/bin/sh");
			} else {
				encoder.encodeString("shellPath", value.getShellPath());
			}

			if (value.getShellScript() == null) {
				encoder.encodeString("shellScript", "");
			} else {
				encoder.encodeString("shellScript", value.getShellScript());
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
			decoder.decodeObjectsIfPresent("files", builder::files);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXSourcesBuildPhase value) {
			encoder.encodeObjects("files", value.getFiles());
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
			decoder.decodeObjectsIfPresent("files", builder::files);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXHeadersBuildPhase value) {
			encoder.encodeObjects("files", value.getFiles());
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
			decoder.decodeObjectsIfPresent("files", builder::files);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXResourcesBuildPhase value) {
			encoder.encodeObjects("files", value.getFiles());
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
			decoder.decodeObjectsIfPresent("files", builder::files);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXFrameworksBuildPhase value) {
			encoder.encodeObjects("files", value.getFiles());
		}
	}

	private static final class PBXBuildFileCoder implements PBXObjectCoder<PBXBuildFile> {
		@Override
		public Class<PBXBuildFile> getType() {
			return PBXBuildFile.class;
		}

		@Override
		public PBXBuildFile read(Decoder decoder) {
			val builder = PBXBuildFile.builder();
			decoder.decodeObjectIfPresent("fileRef", builder::fileRef);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXBuildFile value) {
			encoder.encodeObject("fileRef", value.getFileRef());
			if (!value.getSettings().isEmpty()) {
				encoder.encodeMap("settings", value.getSettings());
			}
		}
	}
}
