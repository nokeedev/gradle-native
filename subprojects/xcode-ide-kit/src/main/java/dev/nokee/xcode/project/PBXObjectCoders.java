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
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.files.PBXReference;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import dev.nokee.xcode.objects.files.PBXVariantGroup;
import dev.nokee.xcode.objects.files.XCVersionGroup;
import dev.nokee.xcode.objects.targets.PBXAggregateTarget;
import dev.nokee.xcode.objects.targets.PBXLegacyTarget;
import dev.nokee.xcode.objects.targets.PBXNativeTarget;
import dev.nokee.xcode.objects.targets.PBXReferenceProxy;
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.objects.targets.PBXTargetDependency;
import dev.nokee.xcode.objects.targets.ProductType;
import dev.nokee.xcode.objects.targets.ProductTypes;
import lombok.val;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static dev.nokee.xcode.objects.configuration.XCConfigurationList.DefaultConfigurationVisibility.HIDDEN;
import static dev.nokee.xcode.objects.configuration.XCConfigurationList.DefaultConfigurationVisibility.VISIBLE;
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
		new ProjectReferenceCoder()
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
			decoder.decodeIfPresent("mainGroup", builder::mainGroup);
			decoder.decodeIfPresent("targets", builder::targets);
			decoder.decodeIfPresent("buildConfigurationList", XCConfigurationList.class, builder::buildConfigurations);
			decoder.decodeIfPresent("projectReferences", new TypeToken<Iterable<PBXProject.ProjectReference>>() {}.getType(), builder::projectReferences);
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

	private static final class PBXFileReferenceCoder implements PBXObjectCoder<PBXFileReference> {
		@Override
		public Class<PBXFileReference> getType() {
			return PBXFileReference.class;
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

	private static final class PBXGroupCoder implements PBXObjectCoder<PBXGroup> {
		@Override
		public Class<PBXGroup> getType() {
			return PBXGroup.class;
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

			List<PBXReference> children = new ArrayList<>(value.getChildren());
			if (value.getSortPolicy() == PBXGroup.SortPolicy.BY_NAME) {
				children.sort(comparing(o -> o.getName().orElse(null), naturalOrder()));
			}

			encoder.encode("children", children);
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

			List<PBXReference> children = new ArrayList<>(value.getChildren());
			if (value.getSortPolicy() == PBXVariantGroup.SortPolicy.BY_NAME) {
				children.sort(comparing(o -> o.getName().orElse(null), naturalOrder()));
			}

			encoder.encode("children", children);
		}
	}

	private static final class XCVersionGroupCoder implements PBXObjectCoder<XCVersionGroup> {
		@Override
		public Class<XCVersionGroup> getType() {
			return XCVersionGroup.class;
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

			List<PBXReference> children = new ArrayList<>(value.getChildren());
			if (value.getSortPolicy() == PBXVariantGroup.SortPolicy.BY_NAME) {
				children.sort(comparing(o -> o.getName().orElse(null), naturalOrder()));
			}

			encoder.encode("children", children);
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

	private static final class PBXLegacyTargetCoder implements PBXObjectCoder<PBXLegacyTarget> {
		@Override
		public Class<PBXLegacyTarget> getType() {
			return PBXLegacyTarget.class;
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

	private static final class PBXNativeTargetCoder implements PBXObjectCoder<PBXNativeTarget> {
		@Override
		public Class<PBXNativeTarget> getType() {
			return PBXNativeTarget.class;
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

	private static final class XCBuildConfigurationCoder implements PBXObjectCoder<XCBuildConfiguration> {
		@Override
		public Class<XCBuildConfiguration> getType() {
			return XCBuildConfiguration.class;
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
			decoder.decodeIfPresent("inputPaths", new TypeToken<Iterable<String>>() {}.getType(), builder::inputPaths);

			// TODO: Write test to show this mistake, it should be builder::outputPaths
			decoder.decodeIfPresent("outputPaths", new TypeToken<Iterable<String>>() {}.getType(), builder::inputPaths);
			decoder.decodeIfPresent("shellPath", String.class, builder::shellPath);
			decoder.decodeIfPresent("shellScript", String.class, builder::shellScript);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXShellScriptBuildPhase value) {
			encoder.encode("files", value.getFiles());

			encoder.encode("inputPaths", value.getInputPaths());
			encoder.encode("outputPaths", value.getOutputPaths());

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

	private static final class PBXBuildFileCoder implements PBXObjectCoder<PBXBuildFile> {
		@Override
		public Class<PBXBuildFile> getType() {
			return PBXBuildFile.class;
		}

		@Override
		public PBXBuildFile read(Decoder decoder) {
			val builder = PBXBuildFile.builder();
			decoder.decodeIfPresent("fileRef", builder::fileRef);
			return builder.build();
		}

		@Override
		public void write(Encoder encoder, PBXBuildFile value) {
			encoder.encode("fileRef", value.getFileRef());
			if (!value.getSettings().isEmpty()) {
				encoder.encode("settings", value.getSettings());
			}
		}
	}

	private static final class PBXContainerItemProxyCoder implements PBXObjectCoder<PBXContainerItemProxy> {
		@Override
		public Class<PBXContainerItemProxy> getType() {
			return PBXContainerItemProxy.class;
		}

		@Override
		public PBXContainerItemProxy read(Decoder decoder) {
			val builder = PBXContainerItemProxy.builder();
			builder.containerPortal(() -> decoder.decodeObject("containerPortal").orElseThrow(RuntimeException::new));
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

	private static final class PBXReferenceProxyCoder implements PBXObjectCoder<PBXReferenceProxy> {
		@Override
		public Class<PBXReferenceProxy> getType() {
			return PBXReferenceProxy.class;
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
}
