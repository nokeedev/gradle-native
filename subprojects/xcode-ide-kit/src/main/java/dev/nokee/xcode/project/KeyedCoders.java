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
import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.GroupChild;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.objects.targets.PBXTargetDependency;
import dev.nokee.xcode.objects.targets.ProductType;
import dev.nokee.xcode.project.coders.PBXBuildFileFactory;
import dev.nokee.xcode.project.coders.PBXContainerItemProxyFactory;
import dev.nokee.xcode.project.coders.PBXFileReferenceFactory;
import dev.nokee.xcode.project.coders.PBXGroupFactory;
import dev.nokee.xcode.project.coders.PBXTargetDependencyFactory;
import dev.nokee.xcode.project.coders.ProjectReferenceFactory;
import dev.nokee.xcode.project.coders.XCBuildConfigurationFactory;
import dev.nokee.xcode.project.coders.XCConfigurationListFactory;
import dev.nokee.xcode.project.coders.XCRemoteSwiftPackageReferenceFactory;
import dev.nokee.xcode.project.coders.XCSwiftPackageProductDependencyFactory;
import dev.nokee.xcode.project.coders.ZeroOneBooleanCoder;
import dev.nokee.xcode.project.coders.BuildPhaseFactory;
import dev.nokee.xcode.project.coders.BuildSettingsCoder;
import dev.nokee.xcode.project.coders.ContainerPortalFactory;
import dev.nokee.xcode.project.coders.DictionaryCoder;
import dev.nokee.xcode.project.coders.FieldCoder;
import dev.nokee.xcode.project.coders.FileReferenceFactory;
import dev.nokee.xcode.project.coders.GroupChildFactory;
import dev.nokee.xcode.project.coders.ListCoder;
import dev.nokee.xcode.project.coders.ObjectCoder;
import dev.nokee.xcode.project.coders.ObjectRefCoder;
import dev.nokee.xcode.project.coders.ProductTypeCoder;
import dev.nokee.xcode.project.coders.ProxyTypeCoder;
import dev.nokee.xcode.project.coders.SourceTreeCoder;
import dev.nokee.xcode.project.coders.StringCoder;
import dev.nokee.xcode.project.coders.SubFolderCoder;
import dev.nokee.xcode.project.coders.TargetFactory;
import dev.nokee.xcode.project.coders.VersionRequirementFactory;
import dev.nokee.xcode.project.coders.VersionRequirementKindCoder;

import java.util.List;
import java.util.Map;

import static dev.nokee.utils.Cast.uncheckedCast;

public final class KeyedCoders {
	private static final ValueCoder<PBXSourceTree> sourceTree = new SourceTreeCoder();
	private static final ValueCoder<ProductType> productType = new ProductTypeCoder();
	private static final ValueCoder<Kind> requirementKind = new VersionRequirementKindCoder();
	private static final ValueCoder<Map<String, ?>> dictionary = new DictionaryCoder();
	private static final ValueCoder<String> string = new StringCoder();
	private static final ValueCoder<List<String>> listOfString = listOf(string);
	private static final ValueCoder<PBXBuildPhase> objectRefOfBuildPhase = uncheckedCast("only interested in main type", objectRef(new BuildPhaseFactory<>()));
	private static final ValueCoder<PBXTargetDependency> objectRefOfTargetDependency = uncheckedCast("only interested in main type", objectRef(new PBXTargetDependencyFactory<>()));
	private static final ValueCoder<PBXFileReference> objectRefOfFileReference = uncheckedCast("only interested in main type", objectRef(new PBXFileReferenceFactory<>()));
	private static final ValueCoder<List<PBXBuildFile>> listOfBuildFiles = uncheckedCast("only interested in main type", listOf(objectRef(new PBXBuildFileFactory<>())));
	private static final ValueCoder<List<GroupChild>> listOfGroupChildren = uncheckedCast("only interested in main type", listOf(objectRef(new GroupChildFactory<>())));
	private static final ValueCoder<XCConfigurationList> objectRefOfConfigurationList = uncheckedCast("only interested in main type", objectRef(new XCConfigurationListFactory<>()));
	private static final ValueCoder<PBXTarget> objectRefOfTarget = uncheckedCast("only interested in main type", objectRef(new TargetFactory<>()));

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
		.put(CodeablePBXBuildFile.CodingKeys.fileRef, forKey("fileRef", objectRef(new FileReferenceFactory<>())))
		.put(CodeablePBXBuildFile.CodingKeys.settings, forKey("settings", dictionary))
		.put(CodeablePBXBuildFile.CodingKeys.productRef, forKey("productRef", objectRef(new XCSwiftPackageProductDependencyFactory<>())))

		// PBXContainerItemProxy
		.put(CodeablePBXContainerItemProxy.CodingKeys.containerPortal, forKey("containerPortal", objectRef(new ContainerPortalFactory<>())))
		.put(CodeablePBXContainerItemProxy.CodingKeys.remoteGlobalIDString, forKey("remoteGlobalIDString"))
		.put(CodeablePBXContainerItemProxy.CodingKeys.remoteInfo, forKey("remoteInfo"))
		.put(CodeablePBXContainerItemProxy.CodingKeys.proxyType, forKey("proxyType", new ProxyTypeCoder()))

		// PBXCopyFilesBuildPhase
		.put(CodeablePBXCopyFilesBuildPhase.CodingKeys.files, forKey("files", listOfBuildFiles))
		.put(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstPath, forKey("dstPath"))
		.put(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstSubfolderSpec, forKey("dstSubfolderSpec", new SubFolderCoder()))

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
		.put(CodeablePBXLegacyTarget.CodingKeys.passBuildSettingsInEnvironment, forKey("passBuildSettingsInEnvironment", new ZeroOneBooleanCoder()))

		// PBXNativeTarget
		.put(CodeablePBXNativeTarget.CodingKeys.name, forKey("name"))
		.put(CodeablePBXNativeTarget.CodingKeys.productName, forKey("productName"))
		.put(CodeablePBXNativeTarget.CodingKeys.productType, forKey("productType", productType))
		.put(CodeablePBXNativeTarget.CodingKeys.productReference, forKey("productReference", objectRefOfFileReference))
		.put(CodeablePBXNativeTarget.CodingKeys.dependencies, forKey("dependencies", listOf(objectRefOfTargetDependency)))
		.put(CodeablePBXNativeTarget.CodingKeys.buildConfigurationList, forKey("buildConfigurationList", objectRefOfConfigurationList))
		.put(CodeablePBXNativeTarget.CodingKeys.buildPhases, forKey("buildPhases", listOf(objectRefOfBuildPhase)))
		.put(CodeablePBXNativeTarget.CodingKeys.packageProductDependencies, forKey("packageProductDependencies", listOf(objectRef(new XCSwiftPackageProductDependencyFactory<>()))))

		// PBXProject
		.put(CodeablePBXProject.CodingKeys.mainGroup, forKey("mainGroup", objectRef(new PBXGroupFactory<>())))
		.put(CodeablePBXProject.CodingKeys.targets, forKey("targets", listOf(objectRefOfTarget)))
		.put(CodeablePBXProject.CodingKeys.buildConfigurationList, forKey("buildConfigurationList", objectRefOfConfigurationList))
		.put(CodeablePBXProject.CodingKeys.compatibilityVersion, forKey("compatibilityVersion"))
		.put(CodeablePBXProject.CodingKeys.projectReferences, forKey("projectReferences", listOf(objectOf(new ProjectReferenceFactory<>()))))
		.put(CodeablePBXProject.CodingKeys.packageReferences, forKey("packageReferences", listOf(objectRef(new XCRemoteSwiftPackageReferenceFactory<>()))))

		// PBXReferenceProxy
		.put(CodeablePBXReferenceProxy.CodingKeys.name, forKey("name"))
		.put(CodeablePBXReferenceProxy.CodingKeys.path, forKey("path"))
		.put(CodeablePBXReferenceProxy.CodingKeys.sourceTree, forKey("sourceTree", sourceTree))
		.put(CodeablePBXReferenceProxy.CodingKeys.fileType, forKey("fileType"))
		.put(CodeablePBXReferenceProxy.CodingKeys.remoteRef, forKey("remoteRef", objectRef(new PBXContainerItemProxyFactory<>())))

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
		.put(CodeablePBXTargetDependency.CodingKeys.targetProxy, forKey("targetProxy", objectRef(new PBXContainerItemProxyFactory<>())))

		// PBXVariantGroup
		.put(CodeablePBXVariantGroup.CodingKeys.name, forKey("name"))
		.put(CodeablePBXVariantGroup.CodingKeys.path, forKey("path"))
		.put(CodeablePBXVariantGroup.CodingKeys.sourceTree, forKey("sourceTree", sourceTree))
		.put(CodeablePBXVariantGroup.CodingKeys.children, forKey("children", listOfGroupChildren))

		// ProjectReference
		.put(CodeableProjectReference.CodingKeys.ProjectRef, forKey("ProjectRef", objectRefOfFileReference))
		.put(CodeableProjectReference.CodingKeys.ProductGroup, forKey("ProductGroup", objectRef(new PBXGroupFactory<>())))

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
		.put(CodeableXCBuildConfiguration.CodingKeys.buildSettings, forKey("buildSettings", new BuildSettingsCoder()))
		.put(CodeableXCBuildConfiguration.CodingKeys.baseConfigurationReference, forKey("baseConfigurationReference", objectRefOfFileReference))

		// XCConfigurationList
		.put(CodeableXCConfigurationList.CodingKeys.defaultConfigurationIsVisible, forKey("defaultConfigurationIsVisible", new ZeroOneBooleanCoder()))
		.put(CodeableXCConfigurationList.CodingKeys.buildConfigurations, forKey("buildConfigurations", listOf(objectRef(new XCBuildConfigurationFactory<>()))))
		.put(CodeableXCConfigurationList.CodingKeys.defaultConfigurationName, forKey("defaultConfigurationName"))

		// XCRemoteSwiftPackageReference
		.put(CodeableXCRemoteSwiftPackageReference.CodingKeys.repositoryUrl, forKey("repositoryURL"))
		.put(CodeableXCRemoteSwiftPackageReference.CodingKeys.requirement, forKey("requirement", objectOf(new VersionRequirementFactory<>())))

		// XCSwiftPackageProductDependency
		.put(CodeableXCSwiftPackageProductDependency.CodingKeys.productName, forKey("productName"))
		.put(CodeableXCSwiftPackageProductDependency.CodingKeys.packageReference, forKey("package", objectRef(new XCRemoteSwiftPackageReferenceFactory<>())))

		// XCVersionGroup
		.put(CodeableXCVersionGroup.CodingKeys.name, forKey("name"))
		.put(CodeableXCVersionGroup.CodingKeys.path, forKey("path"))
		.put(CodeableXCVersionGroup.CodingKeys.sourceTree, forKey("sourceTree", sourceTree))
		.put(CodeableXCVersionGroup.CodingKeys.children, forKey("children", listOfGroupChildren))
		.put(CodeableXCVersionGroup.CodingKeys.currentVersion, forKey("currentVersion", objectRefOfFileReference))
		.put(CodeableXCVersionGroup.CodingKeys.versionGroupType, forKey("versionGroupType"))

		.build();

	private static <T> ValueCoder<List<T>> listOf(ValueCoder<T> elementCoder) {
		return new ListCoder<>(elementCoder);
	}

	private static FieldCoder<String> forKey(String key) {
		return new FieldCoder<>(key, string);
	}

	private static <T> FieldCoder<T> forKey(String key, ValueCoder<T> valueCoder) {
		return new FieldCoder<>(key, valueCoder);
	}

	private static <T extends Codeable> ValueCoder<T> objectRef(CodeableObjectFactory<T> factory) {
		return new ObjectRefCoder<>(factory);
	}

	private static <T extends Codeable> ValueCoder<T> objectOf(CodeableObjectFactory<T> factory) {
		return new ObjectCoder<>(factory);
	}
}
