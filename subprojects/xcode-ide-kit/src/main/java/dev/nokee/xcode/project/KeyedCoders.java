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

import com.google.common.collect.ImmutableMap;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.GroupChild;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind;
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.objects.targets.PBXTargetDependency;
import dev.nokee.xcode.objects.targets.ProductType;
import dev.nokee.xcode.project.coders.BuildPhaseDecoder;
import dev.nokee.xcode.project.coders.BuildSettingsDecoder;
import dev.nokee.xcode.project.coders.BuildSettingsEncoder;
import dev.nokee.xcode.project.coders.ContainerPortalDecoder;
import dev.nokee.xcode.project.coders.DefaultCoder;
import dev.nokee.xcode.project.coders.DictionaryDecoder;
import dev.nokee.xcode.project.coders.DictionaryEncoder;
import dev.nokee.xcode.project.coders.FieldCoder;
import dev.nokee.xcode.project.coders.FileReferenceDecoder;
import dev.nokee.xcode.project.coders.GroupChildDecoder;
import dev.nokee.xcode.project.coders.IntegerDecoder;
import dev.nokee.xcode.project.coders.IntegerEncoder;
import dev.nokee.xcode.project.coders.ListDecoder;
import dev.nokee.xcode.project.coders.ListEncoder;
import dev.nokee.xcode.project.coders.NoOpEncoder;
import dev.nokee.xcode.project.coders.ObjectDecoder;
import dev.nokee.xcode.project.coders.ObjectEncoder;
import dev.nokee.xcode.project.coders.ObjectRefDecoder;
import dev.nokee.xcode.project.coders.ObjectRefEncoder;
import dev.nokee.xcode.project.coders.PBXBuildFileDecoder;
import dev.nokee.xcode.project.coders.PBXContainerItemProxyDecoder;
import dev.nokee.xcode.project.coders.PBXFileReferenceDecoder;
import dev.nokee.xcode.project.coders.PBXGroupDecoder;
import dev.nokee.xcode.project.coders.PBXTargetDependencyDecoder;
import dev.nokee.xcode.project.coders.ProductTypeDecoder;
import dev.nokee.xcode.project.coders.ProductTypeEncoder;
import dev.nokee.xcode.project.coders.ProjectReferenceDecoder;
import dev.nokee.xcode.project.coders.ProxyTypeDecoder;
import dev.nokee.xcode.project.coders.ProxyTypeEncoder;
import dev.nokee.xcode.project.coders.SourceTreeDecoder;
import dev.nokee.xcode.project.coders.SourceTreeEncoder;
import dev.nokee.xcode.project.coders.StringDecoder;
import dev.nokee.xcode.project.coders.StringEncoder;
import dev.nokee.xcode.project.coders.SubFolderDecoder;
import dev.nokee.xcode.project.coders.SubFolderEncoder;
import dev.nokee.xcode.project.coders.TargetDecoder;
import dev.nokee.xcode.project.coders.VersionRequirementDecoder;
import dev.nokee.xcode.project.coders.VersionRequirementKindDecoder;
import dev.nokee.xcode.project.coders.VersionRequirementKindEncoder;
import dev.nokee.xcode.project.coders.XCBuildConfigurationDecoder;
import dev.nokee.xcode.project.coders.XCConfigurationListDecoder;
import dev.nokee.xcode.project.coders.XCRemoteSwiftPackageReferenceDecoder;
import dev.nokee.xcode.project.coders.XCSwiftPackageProductDependencyDecoder;
import dev.nokee.xcode.project.coders.ZeroOneBooleanDecoder;
import dev.nokee.xcode.project.coders.ZeroOneBooleanEncoder;

import java.util.List;
import java.util.Map;

import static dev.nokee.utils.Cast.uncheckedCast;

public final class KeyedCoders {
	private static final ValueCoder<PBXSourceTree> sourceTree = new DefaultCoder<>(new StringDecoder<>(new SourceTreeDecoder()), new StringEncoder<>(new SourceTreeEncoder()));
	private static final ValueCoder<ProductType> productType = new DefaultCoder<>(new StringDecoder<>(new ProductTypeDecoder()), new StringEncoder<>(new ProductTypeEncoder()));
	private static final ValueCoder<Kind> requirementKind = new DefaultCoder<>(new StringDecoder<>(new VersionRequirementKindDecoder()), new StringEncoder<>(new VersionRequirementKindEncoder()));
	private static final ValueCoder<Map<String, ?>> dictionary = new DefaultCoder<>(DictionaryDecoder.newDictionaryDecoder(), DictionaryEncoder.newDictionaryEncoder());
	private static final ValueCoder<String> string = new DefaultCoder<>(StringDecoder.newStringDecoder(), StringEncoder.newStringEncoder());
	private static final ValueCoder<List<String>> listOfString = listOf(string);
	private static final ValueCoder<PBXBuildPhase> objectRefOfBuildPhase = uncheckedCast("only interested in main type", objectRef(new BuildPhaseDecoder<>()));
	private static final ValueCoder<PBXTargetDependency> objectRefOfTargetDependency = uncheckedCast("only interested in main type", objectRef(new PBXTargetDependencyDecoder<>()));
	private static final ValueCoder<PBXFileReference> objectRefOfFileReference = uncheckedCast("only interested in main type", objectRef(new PBXFileReferenceDecoder<>()));
	private static final ValueCoder<List<PBXBuildFile>> listOfBuildFiles = uncheckedCast("only interested in main type", listOf(objectRef(new PBXBuildFileDecoder<>())));
	private static final ValueCoder<List<GroupChild>> listOfGroupChildren = uncheckedCast("only interested in main type", listOf(objectRef(new GroupChildDecoder<>())));
	private static final ValueCoder<XCConfigurationList> objectRefOfConfigurationList = uncheckedCast("only interested in main type", objectRef(new XCConfigurationListDecoder<>()));
	private static final ValueCoder<PBXTarget> objectRefOfTarget = uncheckedCast("only interested in main type", objectRef(new TargetDecoder<>()));

	public static final CodingKey ISA = new CodingKey() {
		@Override
		public String getName() {
			return "isa";
		}

		@Override
		public String toString() {
			return getName();
		}
	};
	public static final CodingKey VERSION_REQUIREMENT_KIND = new CodingKey() {
		@Override
		public String getName() {
			return "kind";
		}

		@Override
		public String toString() {
			return getName();
		}
	};

	public static final Map<CodingKey, KeyedCoder<?>> DECODERS = ImmutableMap.<CodingKey, KeyedCoder<?>>builder()
		.put(ISA, forKey("isa"))
		.put(VERSION_REQUIREMENT_KIND, forKey("kind", requirementKind))

		// PBXAggregateTarget
		.put(CodeablePBXAggregateTarget.CodingKeys.name, forKey("name"))
		.put(CodeablePBXAggregateTarget.CodingKeys.productType, forKey("productType", productType))
		.put(CodeablePBXAggregateTarget.CodingKeys.buildPhases, forKey("buildPhases", listOf(objectRefOfBuildPhase)))
		.put(CodeablePBXAggregateTarget.CodingKeys.buildConfigurationList, forKey("buildConfigurationList", objectRefOfConfigurationList))
		.put(CodeablePBXAggregateTarget.CodingKeys.dependencies, forKey("dependencies", listOf(objectRefOfTargetDependency)))
		.put(CodeablePBXAggregateTarget.CodingKeys.productName, forKey("productName"))
		.put(CodeablePBXAggregateTarget.CodingKeys.productReference, forKey("productReference", objectRefOfFileReference))

		// PBXBuildFile
		.put(CodeablePBXBuildFile.CodingKeys.fileRef, forKey("fileRef", objectRef(new FileReferenceDecoder<>())))
		.put(CodeablePBXBuildFile.CodingKeys.settings, forKey("settings", dictionary))
		.put(CodeablePBXBuildFile.CodingKeys.productRef, forKey("productRef", objectRef(new XCSwiftPackageProductDependencyDecoder<>())))

		// PBXContainerItemProxy
		.put(CodeablePBXContainerItemProxy.CodingKeys.containerPortal, forKey("containerPortal", objectRef(new ContainerPortalDecoder<>())))
		.put(CodeablePBXContainerItemProxy.CodingKeys.remoteGlobalIDString, forKey("remoteGlobalIDString"))
		.put(CodeablePBXContainerItemProxy.CodingKeys.remoteInfo, forKey("remoteInfo"))
		.put(CodeablePBXContainerItemProxy.CodingKeys.proxyType, forKey("proxyType", new DefaultCoder<>(new IntegerDecoder<>(new ProxyTypeDecoder()), new IntegerEncoder<>(new ProxyTypeEncoder()))))

		// PBXCopyFilesBuildPhase
		.put(CodeablePBXCopyFilesBuildPhase.CodingKeys.files, forKey("files", listOfBuildFiles))
		.put(CodeablePBXCopyFilesBuildPhase.CodingKeys.name, forKey("name"))
		.put(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstPath, forKey("dstPath"))
		.put(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstSubfolderSpec, forKey("dstSubfolderSpec", new DefaultCoder<>(new IntegerDecoder<>(new SubFolderDecoder()), new IntegerEncoder<>(new SubFolderEncoder()))))

		// PBXFileReference
		.put(CodeablePBXFileReference.CodingKeys.name, forKey("name"))
		.put(CodeablePBXFileReference.CodingKeys.path, forKey("path"))
		.put(CodeablePBXFileReference.CodingKeys.sourceTree, forKey("sourceTree", sourceTree))
		.put(CodeablePBXFileReference.CodingKeys.lastKnownFileType, forKey("lastKnownFileType"))
		.put(CodeablePBXFileReference.CodingKeys.explicitFileType, forKey("explicitFileType"))

		// PBXFrameworksBuildPhase
		.put(CodeablePBXFrameworksBuildPhase.CodingKeys.files, forKey("files", listOfBuildFiles))

		// PBXGroup
		.put(CodeablePBXGroup.CodingKeys.name, forKey("name"))
		.put(CodeablePBXGroup.CodingKeys.path, forKey("path"))
		.put(CodeablePBXGroup.CodingKeys.sourceTree, forKey("sourceTree", sourceTree))
		.put(CodeablePBXGroup.CodingKeys.children, forKey("children", listOfGroupChildren))

		// PBXHeadersBuildPhase
		.put(CodeablePBXHeadersBuildPhase.CodingKeys.files, forKey("files", listOfBuildFiles))

		// PBXLegacyTarget
		.put(CodeablePBXLegacyTarget.CodingKeys.name, forKey("name"))
		.put(CodeablePBXLegacyTarget.CodingKeys.productName, forKey("productName"))
		.put(CodeablePBXLegacyTarget.CodingKeys.productType, forKey("productType", productType))
		.put(CodeablePBXLegacyTarget.CodingKeys.productReference, forKey("productReference", objectRefOfFileReference))
		.put(CodeablePBXLegacyTarget.CodingKeys.dependencies, forKey("dependencies", listOf(objectRefOfTargetDependency)))
		.put(CodeablePBXLegacyTarget.CodingKeys.buildConfigurationList, forKey("buildConfigurationList", objectRefOfConfigurationList))
		.put(CodeablePBXLegacyTarget.CodingKeys.buildPhases, forKey("buildPhases", listOf(objectRefOfBuildPhase)))
		.put(CodeablePBXLegacyTarget.CodingKeys.buildArgumentsString, forKey("buildArgumentsString"))
		.put(CodeablePBXLegacyTarget.CodingKeys.buildToolPath, forKey("buildToolPath"))
		.put(CodeablePBXLegacyTarget.CodingKeys.buildWorkingDirectory, forKey("buildWorkingDirectory"))
		.put(CodeablePBXLegacyTarget.CodingKeys.passBuildSettingsInEnvironment, forKey("passBuildSettingsInEnvironment", new DefaultCoder<>(new ZeroOneBooleanDecoder(), new ZeroOneBooleanEncoder())))

		// PBXNativeTarget
		.put(CodeablePBXNativeTarget.CodingKeys.name, forKey("name"))
		.put(CodeablePBXNativeTarget.CodingKeys.productName, forKey("productName"))
		.put(CodeablePBXNativeTarget.CodingKeys.productType, forKey("productType", productType))
		.put(CodeablePBXNativeTarget.CodingKeys.productReference, forKey("productReference", objectRefOfFileReference))
		.put(CodeablePBXNativeTarget.CodingKeys.dependencies, forKey("dependencies", listOf(objectRefOfTargetDependency)))
		.put(CodeablePBXNativeTarget.CodingKeys.buildConfigurationList, forKey("buildConfigurationList", objectRefOfConfigurationList))
		.put(CodeablePBXNativeTarget.CodingKeys.buildPhases, forKey("buildPhases", listOf(objectRefOfBuildPhase)))
		.put(CodeablePBXNativeTarget.CodingKeys.packageProductDependencies, forKey("packageProductDependencies", listOf(objectRef(new XCSwiftPackageProductDependencyDecoder<>()))))

		// PBXProject
		.put(CodeablePBXProject.CodingKeys.mainGroup, forKey("mainGroup", objectRef(new PBXGroupDecoder<>())))
		.put(CodeablePBXProject.CodingKeys.targets, forKey("targets", listOf(objectRefOfTarget)))
		.put(CodeablePBXProject.CodingKeys.buildConfigurationList, forKey("buildConfigurationList", objectRefOfConfigurationList))
		.put(CodeablePBXProject.CodingKeys.compatibilityVersion, forKey("compatibilityVersion"))
		.put(CodeablePBXProject.CodingKeys.projectReferences, forKey("projectReferences", listOf(objectOf(new ProjectReferenceDecoder<>()))))
		.put(CodeablePBXProject.CodingKeys.packageReferences, forKey("packageReferences", listOf(objectRef(new XCRemoteSwiftPackageReferenceDecoder<>()))))
		.put(CodeablePBXProject.CodingKeys.projectDirPath, forKey("projectDirPath", string))

		// PBXReferenceProxy
		.put(CodeablePBXReferenceProxy.CodingKeys.name, forKey("name"))
		.put(CodeablePBXReferenceProxy.CodingKeys.path, forKey("path"))
		.put(CodeablePBXReferenceProxy.CodingKeys.sourceTree, forKey("sourceTree", sourceTree))
		.put(CodeablePBXReferenceProxy.CodingKeys.fileType, forKey("fileType"))
		.put(CodeablePBXReferenceProxy.CodingKeys.remoteRef, forKey("remoteRef", objectRef(new PBXContainerItemProxyDecoder<>())))

		// PBXResourcesBuildPhase
		.put(CodeablePBXResourcesBuildPhase.CodingKeys.files, forKey("files", listOfBuildFiles))

		// PBXShellScriptBuildPhase
		.put(CodeablePBXShellScriptBuildPhase.CodingKeys.name, forKey("name"))
		.put(CodeablePBXShellScriptBuildPhase.CodingKeys.files, forKey("files", listOfBuildFiles))
		.put(CodeablePBXShellScriptBuildPhase.CodingKeys.shellPath, forKey("shellPath"))
		.put(CodeablePBXShellScriptBuildPhase.CodingKeys.shellScript, forKey("shellScript"))
		.put(CodeablePBXShellScriptBuildPhase.CodingKeys.inputPaths, forKey("inputPaths", listOfString))
		.put(CodeablePBXShellScriptBuildPhase.CodingKeys.inputFileListPaths, forKey("inputFileListPaths", listOfString))
		.put(CodeablePBXShellScriptBuildPhase.CodingKeys.outputPaths, forKey("outputPaths", listOfString))
		.put(CodeablePBXShellScriptBuildPhase.CodingKeys.outputFileListPaths, forKey("outputFileListPaths", listOfString))

		// PBXSourcesBuildPhase
		.put(CodeablePBXSourcesBuildPhase.CodingKeys.files, forKey("files", listOfBuildFiles))

		// PBXTargetDependency
		.put(CodeablePBXTargetDependency.CodingKeys.name, forKey("name"))
		.put(CodeablePBXTargetDependency.CodingKeys.target, forKey("target", objectRefOfTarget))
		.put(CodeablePBXTargetDependency.CodingKeys.targetProxy, forKey("targetProxy", objectRef(new PBXContainerItemProxyDecoder<>())))

		// PBXVariantGroup
		.put(CodeablePBXVariantGroup.CodingKeys.name, forKey("name"))
		.put(CodeablePBXVariantGroup.CodingKeys.path, forKey("path"))
		.put(CodeablePBXVariantGroup.CodingKeys.sourceTree, forKey("sourceTree", sourceTree))
		.put(CodeablePBXVariantGroup.CodingKeys.children, forKey("children", listOfGroupChildren))

		// ProjectReference
		.put(CodeableProjectReference.CodingKeys.ProjectRef, forKey("ProjectRef", objectRefOfFileReference))
		.put(CodeableProjectReference.CodingKeys.ProductGroup, forKey("ProductGroup", objectRef(new PBXGroupDecoder<>())))

		// VersionRequirement.Branch
		.put(CodeableVersionRequirementBranch.CodingKeys.kind, forKey("kind", requirementKind))
		.put(CodeableVersionRequirementBranch.CodingKeys.branch, forKey("branch"))

		// VersionRequirement.Exact
		.put(CodeableVersionRequirementExact.CodingKeys.kind, forKey("kind", requirementKind))
		.put(CodeableVersionRequirementExact.CodingKeys.version, forKey("version"))

		// VersionRequirement.Range
		.put(CodeableVersionRequirementRange.CodingKeys.kind, forKey("kind", requirementKind))
		.put(CodeableVersionRequirementRange.CodingKeys.minimumVersion, forKey("minimumVersion"))
		.put(CodeableVersionRequirementRange.CodingKeys.maximumVersion, forKey("maximumVersion"))

		// VersionRequirement.Revision
		.put(CodeableVersionRequirementRevision.CodingKeys.kind, forKey("kind", requirementKind))
		.put(CodeableVersionRequirementRevision.CodingKeys.revision, forKey("revision"))

		// VersionRequirement.UpToNextMajorVersion
		.put(CodeableVersionRequirementUpToNextMajorVersion.CodingKeys.kind, forKey("kind", requirementKind))
		.put(CodeableVersionRequirementUpToNextMajorVersion.CodingKeys.minimumVersion, forKey("minimumVersion"))

		// VersionRequirement.UpToNextMinorVersion
		.put(CodeableVersionRequirementUpToNextMinorVersion.CodingKeys.kind, forKey("kind", requirementKind))
		.put(CodeableVersionRequirementUpToNextMinorVersion.CodingKeys.minimumVersion, forKey("minimumVersion"))

		// XCBuildConfiguration
		.put(CodeableXCBuildConfiguration.CodingKeys.name, forKey("name"))
		.put(CodeableXCBuildConfiguration.CodingKeys.buildSettings, forKey("buildSettings", new DefaultCoder<>(new DictionaryDecoder<>(new BuildSettingsDecoder()), new DictionaryEncoder<>(new BuildSettingsEncoder()))))
		.put(CodeableXCBuildConfiguration.CodingKeys.baseConfigurationReference, forKey("baseConfigurationReference", objectRefOfFileReference))

		// XCConfigurationList
		.put(CodeableXCConfigurationList.CodingKeys.defaultConfigurationIsVisible, forKey("defaultConfigurationIsVisible", new DefaultCoder<>(new ZeroOneBooleanDecoder(), new ZeroOneBooleanEncoder())))
		.put(CodeableXCConfigurationList.CodingKeys.buildConfigurations, forKey("buildConfigurations", listOf(objectRef(new XCBuildConfigurationDecoder<>()))))
		.put(CodeableXCConfigurationList.CodingKeys.defaultConfigurationName, forKey("defaultConfigurationName"))

		// XCRemoteSwiftPackageReference
		.put(CodeableXCRemoteSwiftPackageReference.CodingKeys.repositoryUrl, forKey("repositoryURL"))
		.put(CodeableXCRemoteSwiftPackageReference.CodingKeys.requirement, forKey("requirement", objectOf(new VersionRequirementDecoder<>())))

		// XCSwiftPackageProductDependency
		.put(CodeableXCSwiftPackageProductDependency.CodingKeys.productName, forKey("productName"))
		.put(CodeableXCSwiftPackageProductDependency.CodingKeys.packageReference, forKey("package", objectRef(new XCRemoteSwiftPackageReferenceDecoder<>())))

		// XCVersionGroup
		.put(CodeableXCVersionGroup.CodingKeys.name, forKey("name"))
		.put(CodeableXCVersionGroup.CodingKeys.path, forKey("path"))
		.put(CodeableXCVersionGroup.CodingKeys.sourceTree, forKey("sourceTree", sourceTree))
		.put(CodeableXCVersionGroup.CodingKeys.children, forKey("children", listOfGroupChildren))
		.put(CodeableXCVersionGroup.CodingKeys.currentVersion, forKey("currentVersion", objectRefOfFileReference))
		.put(CodeableXCVersionGroup.CodingKeys.versionGroupType, forKey("versionGroupType"))

		.build();

	private static <T> ValueCoder<List<T>> listOf(ValueCoder<T> elementCoder) {
		return new DefaultCoder<>(new ListDecoder<>(elementCoder), new ListEncoder<>(elementCoder));
	}

	public static FieldCoder<String> forKey(String key) {
		return new FieldCoder<>(key, string);
	}

	private static <T> FieldCoder<T> forKey(String key, ValueCoder<T> valueCoder) {
		return new FieldCoder<>(key, valueCoder);
	}

	private static <T extends Codeable> ValueCoder<T> objectRef(ValueDecoder<T, KeyedObject> decoder) {
		return new DefaultCoder<>(new ObjectRefDecoder<>(decoder), new ObjectRefEncoder<>(new NoOpEncoder<>()));
	}

	private static <T extends Codeable> ValueCoder<T> objectOf(ValueDecoder<T, KeyedObject> decoder) {
		return new DefaultCoder<>(new ObjectDecoder<>(decoder), new ObjectEncoder<>(new NoOpEncoder<>()));
	}
}
