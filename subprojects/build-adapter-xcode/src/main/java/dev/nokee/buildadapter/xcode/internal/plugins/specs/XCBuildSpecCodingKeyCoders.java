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
package dev.nokee.buildadapter.xcode.internal.plugins.specs;

import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.targets.ProductType;
import dev.nokee.xcode.project.Codeable;
import dev.nokee.xcode.project.CodeablePBXAggregateTarget;
import dev.nokee.xcode.project.CodeablePBXBuildFile;
import dev.nokee.xcode.project.CodeablePBXContainerItemProxy;
import dev.nokee.xcode.project.CodeablePBXCopyFilesBuildPhase;
import dev.nokee.xcode.project.CodeablePBXFrameworksBuildPhase;
import dev.nokee.xcode.project.CodeablePBXGroup;
import dev.nokee.xcode.project.CodeablePBXHeadersBuildPhase;
import dev.nokee.xcode.project.CodeablePBXLegacyTarget;
import dev.nokee.xcode.project.CodeablePBXNativeTarget;
import dev.nokee.xcode.project.CodeablePBXProject;
import dev.nokee.xcode.project.CodeablePBXReferenceProxy;
import dev.nokee.xcode.project.CodeablePBXResourcesBuildPhase;
import dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase;
import dev.nokee.xcode.project.CodeablePBXSourcesBuildPhase;
import dev.nokee.xcode.project.CodeablePBXTargetDependency;
import dev.nokee.xcode.project.CodeablePBXVariantGroup;
import dev.nokee.xcode.project.CodeableProjectReference;
import dev.nokee.xcode.project.CodeableVersionRequirementBranch;
import dev.nokee.xcode.project.CodeableVersionRequirementExact;
import dev.nokee.xcode.project.CodeableVersionRequirementRange;
import dev.nokee.xcode.project.CodeableVersionRequirementRevision;
import dev.nokee.xcode.project.CodeableVersionRequirementUpToNextMajorVersion;
import dev.nokee.xcode.project.CodeableVersionRequirementUpToNextMinorVersion;
import dev.nokee.xcode.project.CodeableXCBuildConfiguration;
import dev.nokee.xcode.project.CodeableXCConfigurationList;
import dev.nokee.xcode.project.CodeableXCRemoteSwiftPackageReference;
import dev.nokee.xcode.project.CodeableXCSwiftPackageProductDependency;
import dev.nokee.xcode.project.CodeableXCVersionGroup;
import dev.nokee.xcode.project.CodingKey;
import dev.nokee.xcode.project.CodingKeyCoders;
import dev.nokee.xcode.project.KeyedCoder;
import dev.nokee.xcode.project.ValueEncoder;
import dev.nokee.xcode.project.coders.FieldCoder;
import dev.nokee.xcode.project.coders.ListEncoder;
import dev.nokee.xcode.project.coders.NoOpEncoder;
import dev.nokee.xcode.project.coders.ProductTypeEncoder;
import dev.nokee.xcode.project.coders.StringEncoder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dev.nokee.xcode.project.KeyedCoders.ISA;

public final class XCBuildSpecCodingKeyCoders implements CodingKeyCoders {
	private static final Map<CodingKey, KeyedCoder<?>> coders = Collections.unmodifiableMap(new HashMap<CodingKey, KeyedCoder<?>>() {{
		put(ISA, forKey("isa", atInput(ofString())));

		// PBXAggregateTarget
		put(CodeablePBXAggregateTarget.CodingKeys.name, null);
		put(CodeablePBXAggregateTarget.CodingKeys.productName, forKey("productName", atInput(ofString())));
		put(CodeablePBXAggregateTarget.CodingKeys.productType, null); // aggregate target are not allowed to have product type
		put(CodeablePBXAggregateTarget.CodingKeys.productReference, null);
		put(CodeablePBXAggregateTarget.CodingKeys.buildPhases, forKey("buildPhases", list(of(PBXBuildPhase.class))));
		put(CodeablePBXAggregateTarget.CodingKeys.buildConfigurationList, null);
		put(CodeablePBXAggregateTarget.CodingKeys.dependencies, null);

		// PBXBuildFile
		put(CodeablePBXBuildFile.CodingKeys.fileRef, forKey("fileRef", atInputFiles(ofLocation())));
		put(CodeablePBXBuildFile.CodingKeys.settings, null);
		put(CodeablePBXBuildFile.CodingKeys.productRef, null);

		// PBXContainerItemProxy
		put(CodeablePBXContainerItemProxy.CodingKeys.containerPortal, null);
		put(CodeablePBXContainerItemProxy.CodingKeys.remoteGlobalIDString, null);
		put(CodeablePBXContainerItemProxy.CodingKeys.remoteInfo, null);
		put(CodeablePBXContainerItemProxy.CodingKeys.proxyType, null);

		// PBXCopyFilesBuildPhase
		put(CodeablePBXCopyFilesBuildPhase.CodingKeys.files, forKey("files", list(of(PBXBuildFile.class))));
		put(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstPath, null);
		put(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstSubfolderSpec, null);

		// PBXFrameworksBuildPhase
		put(CodeablePBXFrameworksBuildPhase.CodingKeys.files, forKey("files", list(of(PBXBuildFile.class))));

		// PBXGroup
		put(CodeablePBXGroup.CodingKeys.name, null);
		put(CodeablePBXGroup.CodingKeys.path, null);
		put(CodeablePBXGroup.CodingKeys.sourceTree, null);
		put(CodeablePBXGroup.CodingKeys.children, null);

		// PBXHeadersBuildPhase
		put(CodeablePBXHeadersBuildPhase.CodingKeys.files, forKey("files", list(of(PBXBuildFile.class))));

		// PBXLegacyTarget
		put(CodeablePBXLegacyTarget.CodingKeys.name, null);
		put(CodeablePBXLegacyTarget.CodingKeys.productName, forKey("productName", atInput(ofString())));
		put(CodeablePBXLegacyTarget.CodingKeys.productType, null); // legacy target are not allowed to have product type
		put(CodeablePBXLegacyTarget.CodingKeys.productReference, null);
		put(CodeablePBXLegacyTarget.CodingKeys.dependencies, null);
		put(CodeablePBXLegacyTarget.CodingKeys.buildConfigurationList, null);
		put(CodeablePBXLegacyTarget.CodingKeys.buildPhases, null); // legacy target are not allowed to have build phases
		put(CodeablePBXLegacyTarget.CodingKeys.buildArgumentsString, forKey("buildArgumentsString", atInput(ofString())));
		put(CodeablePBXLegacyTarget.CodingKeys.buildToolPath, forKey("buildToolPath", atInput(ofString())));
		put(CodeablePBXLegacyTarget.CodingKeys.buildWorkingDirectory, null);
		put(CodeablePBXLegacyTarget.CodingKeys.passBuildSettingsInEnvironment, null);

		// PBXNativeTarget
		put(CodeablePBXNativeTarget.CodingKeys.name, null);
		put(CodeablePBXNativeTarget.CodingKeys.productName, forKey("productName", atInput(ofString())));
		put(CodeablePBXNativeTarget.CodingKeys.productType, forKey("productType", atInput(ofProductType())));
		put(CodeablePBXNativeTarget.CodingKeys.productReference, null);
		put(CodeablePBXNativeTarget.CodingKeys.dependencies, null);
		put(CodeablePBXNativeTarget.CodingKeys.buildConfigurationList, null);
		put(CodeablePBXNativeTarget.CodingKeys.buildPhases, forKey("buildPhases", list(of(PBXBuildPhase.class))));
		put(CodeablePBXNativeTarget.CodingKeys.packageProductDependencies, null);

		// PBXProject
		put(CodeablePBXProject.CodingKeys.mainGroup, null);
		put(CodeablePBXProject.CodingKeys.targets, null);
		put(CodeablePBXProject.CodingKeys.buildConfigurationList, null);
		put(CodeablePBXProject.CodingKeys.compatibilityVersion, null);
		put(CodeablePBXProject.CodingKeys.projectReferences, null);
		put(CodeablePBXProject.CodingKeys.packageReferences, null);

		// PBXReferenceProxy
		put(CodeablePBXReferenceProxy.CodingKeys.name, null);
		put(CodeablePBXReferenceProxy.CodingKeys.path, null);
		put(CodeablePBXReferenceProxy.CodingKeys.sourceTree, null);
		put(CodeablePBXReferenceProxy.CodingKeys.fileType, null);
		put(CodeablePBXReferenceProxy.CodingKeys.remoteRef, null);

		// PBXResourcesBuildPhase
		put(CodeablePBXResourcesBuildPhase.CodingKeys.files, forKey("files", list(of(PBXBuildFile.class))));

		// PBXShellScriptBuildPhase
		put(CodeablePBXShellScriptBuildPhase.CodingKeys.name, null);
		put(CodeablePBXShellScriptBuildPhase.CodingKeys.files, null); // shell script uses input*/output* to check up-to-date
		put(CodeablePBXShellScriptBuildPhase.CodingKeys.shellPath, forKey("shellPath", atInput(ofString())));
		put(CodeablePBXShellScriptBuildPhase.CodingKeys.shellScript, forKey("shellScript", atInput(ofString())));
		put(CodeablePBXShellScriptBuildPhase.CodingKeys.inputPaths, forKey("inputPaths", atInputFiles(set(ofLocation()))));
		put(CodeablePBXShellScriptBuildPhase.CodingKeys.inputFileListPaths, null);
		put(CodeablePBXShellScriptBuildPhase.CodingKeys.outputPaths, null);
		put(CodeablePBXShellScriptBuildPhase.CodingKeys.outputFileListPaths, null);

		// PBXSourcesBuildPhase
		put(CodeablePBXSourcesBuildPhase.CodingKeys.files, forKey("files", list(of(PBXBuildFile.class))));

		// PBXTargetDependency
		put(CodeablePBXTargetDependency.CodingKeys.name, null);
		put(CodeablePBXTargetDependency.CodingKeys.target, null);
		put(CodeablePBXTargetDependency.CodingKeys.targetProxy, null);

		// PBXVariantGroup
		put(CodeablePBXVariantGroup.CodingKeys.name, null);
		put(CodeablePBXVariantGroup.CodingKeys.path, null);
		put(CodeablePBXVariantGroup.CodingKeys.sourceTree, null);
		put(CodeablePBXVariantGroup.CodingKeys.children, null);

		// ProjectReference
		put(CodeableProjectReference.CodingKeys.ProjectRef, null);
		put(CodeableProjectReference.CodingKeys.ProductGroup, null);

		// VersionRequirement.Branch
		put(CodeableVersionRequirementBranch.CodingKeys.kind, null);
		put(CodeableVersionRequirementBranch.CodingKeys.branch, null);

		// VersionRequirement.Exact
		put(CodeableVersionRequirementExact.CodingKeys.kind, null);
		put(CodeableVersionRequirementExact.CodingKeys.version, null);

		// VersionRequirement.Range
		put(CodeableVersionRequirementRange.CodingKeys.kind, null);
		put(CodeableVersionRequirementRange.CodingKeys.minimumVersion, null);
		put(CodeableVersionRequirementRange.CodingKeys.maximumVersion, null);

		// VersionRequirement.Revision
		put(CodeableVersionRequirementRevision.CodingKeys.kind, null);
		put(CodeableVersionRequirementRevision.CodingKeys.revision, null);

		// VersionRequirement.UpToNextMajorVersion
		put(CodeableVersionRequirementUpToNextMajorVersion.CodingKeys.kind, null);
		put(CodeableVersionRequirementUpToNextMajorVersion.CodingKeys.minimumVersion, null);

		// VersionRequirement.UpToNextMinorVersion
		put(CodeableVersionRequirementUpToNextMinorVersion.CodingKeys.kind, null);
		put(CodeableVersionRequirementUpToNextMinorVersion.CodingKeys.minimumVersion, null);

		// XCBuildConfiguration
		put(CodeableXCBuildConfiguration.CodingKeys.name, null);
		put(CodeableXCBuildConfiguration.CodingKeys.buildSettings, null);
		put(CodeableXCBuildConfiguration.CodingKeys.baseConfigurationReference, null);

		// XCConfigurationList
		put(CodeableXCConfigurationList.CodingKeys.defaultConfigurationIsVisible, null);
		put(CodeableXCConfigurationList.CodingKeys.buildConfigurations, null); // don't really care about the available configurations
		put(CodeableXCConfigurationList.CodingKeys.defaultConfigurationName, null);

		// XCRemoteSwiftPackageReference
		put(CodeableXCRemoteSwiftPackageReference.CodingKeys.repositoryUrl, null);
		put(CodeableXCRemoteSwiftPackageReference.CodingKeys.requirement, null);

		// XCSwiftPackageProductDependency
		put(CodeableXCSwiftPackageProductDependency.CodingKeys.productName, null);
		put(CodeableXCSwiftPackageProductDependency.CodingKeys.packageReference, null);

		// XCVersionGroup
		put(CodeableXCVersionGroup.CodingKeys.name, null);
		put(CodeableXCVersionGroup.CodingKeys.path, null);
		put(CodeableXCVersionGroup.CodingKeys.sourceTree, null);
		put(CodeableXCVersionGroup.CodingKeys.children, null);
		put(CodeableXCVersionGroup.CodingKeys.currentVersion, null);
		put(CodeableXCVersionGroup.CodingKeys.versionGroupType, null);
	}});

	@Override
	public Optional<KeyedCoder<?>> get(CodingKey key) {
		return Optional.ofNullable(coders.get(key));
	}

	private static <T> FieldCoder<T> forKey(String key, ValueEncoder<?, T> valueCoder) {
		return new FieldCoder<>(key, new EncoderOnlyCoder<>(valueCoder));
	}

	private static <T extends Codeable, U> ValueEncoder<XCBuildSpec, T> of(Class<U> type) {
		return new AtNestedMapEncoder<>(new NoOpEncoder<>());
	}

	private static ValueEncoder<XCBuildSpec, Object> ofLocation() {
		return new FileSystemLocationEncoder<>(new NormalizeStringAsPBXReferenceEncoder(new NormalizePBXBuildFileFileReferenceAsPBXReferenceEncoder(new ThrowingValueEncoder<>())));
	}

	private static <T> ValueEncoder<XCBuildSpec, List<T>> list(ValueEncoder<XCBuildSpec, T> elementEncoder) {
		return AtNestedCollectionEncoder.atNestedList(new ListEncoder<>(elementEncoder));
	}

	private static <T> ValueEncoder<XCBuildSpec, T> atInputFiles(ValueEncoder<XCBuildSpec, T> encoder) {
		return new AtInputFilesEncoder<>(encoder);
	}

	private static <T> ValueEncoder<XCBuildSpec, T> atInput(ValueEncoder<?, T> encoder) {
		return new AtInputEncoder<>(encoder);
	}

	private static ValueEncoder<String, String> ofString() {
		return new StringEncoder<>(new NoOpEncoder<>());
	}

	private static ValueEncoder<String, ProductType> ofProductType() {
		return new ProductTypeEncoder();
	}

	private static <T> ValueEncoder<XCBuildSpec, List<T>> set(ValueEncoder<XCBuildSpec, T> elementEncoder) {
		return AtNestedCollectionEncoder.atNestedSet(new ListEncoder<>(elementEncoder));
	}
}
