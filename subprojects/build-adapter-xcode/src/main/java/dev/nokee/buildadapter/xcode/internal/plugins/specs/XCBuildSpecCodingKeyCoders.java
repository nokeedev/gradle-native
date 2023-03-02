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
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
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
import dev.nokee.xcode.project.Encodeable;
import dev.nokee.xcode.project.KeyedCoder;
import dev.nokee.xcode.project.ValueCoder;
import dev.nokee.xcode.project.ValueDecoder;
import dev.nokee.xcode.project.ValueEncoder;
import dev.nokee.xcode.project.coders.FalseTrueBooleanEncoder;
import dev.nokee.xcode.project.coders.FieldCoder;
import dev.nokee.xcode.project.coders.ListEncoder;
import dev.nokee.xcode.project.coders.NoOpEncoder;
import dev.nokee.xcode.project.coders.ProductTypeEncoder;
import dev.nokee.xcode.project.coders.VersionRequirementKindEncoder;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dev.nokee.xcode.project.KeyedCoders.ISA;
import static dev.nokee.xcode.project.coders.DictionaryEncoder.newDictionaryEncoder;
import static dev.nokee.xcode.project.coders.StringEncoder.newStringEncoder;

public final class XCBuildSpecCodingKeyCoders implements CodingKeyCoders {
	public static final CodingKey DESTINATION = new CodingKey() {
		@Override
		public String getName() {
			return "destination";
		}

		@Override
		public String toString() {
			return getName();
		}
	};

	private static final Map<CodingKey, KeyedCoder<?>> coders = Collections.unmodifiableMap(new HashMap<CodingKey, KeyedCoder<?>>() {{
		put(ISA, forKey("isa", input(string())));

		// PBXAggregateTarget
		put(CodeablePBXAggregateTarget.CodingKeys.name, forKey("name", input(string())));
		put(CodeablePBXAggregateTarget.CodingKeys.productName, forKey("productName", input(string())));
		put(CodeablePBXAggregateTarget.CodingKeys.productType, forKey("productType", input(productType())));
		put(CodeablePBXAggregateTarget.CodingKeys.productReference, forKey("productReference", outputLocation()));
		put(CodeablePBXAggregateTarget.CodingKeys.buildPhases, forKey("buildPhases", listOf(buildPhaseObject())));
		put(CodeablePBXAggregateTarget.CodingKeys.buildConfigurationList, forKey("buildConfigurationList", object(XCConfigurationList.class)));
		put(CodeablePBXAggregateTarget.CodingKeys.dependencies, null);

		// PBXBuildFile
		put(CodeablePBXBuildFile.CodingKeys.fileRef, forKey("fileRef", inputLocation()));
		put(CodeablePBXBuildFile.CodingKeys.settings, forKey("settings", input(dictionary())));
		put(CodeablePBXBuildFile.CodingKeys.productRef, forKey("productRef", object(XCSwiftPackageProductDependency.class)));

		// PBXContainerItemProxy
		put(CodeablePBXContainerItemProxy.CodingKeys.containerPortal, null);
		put(CodeablePBXContainerItemProxy.CodingKeys.remoteGlobalIDString, null);
		put(CodeablePBXContainerItemProxy.CodingKeys.remoteInfo, null);
		put(CodeablePBXContainerItemProxy.CodingKeys.proxyType, null);

		// PBXCopyFilesBuildPhase
		put(CodeablePBXCopyFilesBuildPhase.CodingKeys.files, forKey("files", listOf(object(PBXBuildFile.class))));
		put(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstPath, null);
		put(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstSubfolderSpec, null);
		put(DESTINATION, forKey("destination", listOf(outputLocation())));

		// PBXFrameworksBuildPhase
		put(CodeablePBXFrameworksBuildPhase.CodingKeys.files, forKey("files", listOf(object(PBXBuildFile.class))));

		// PBXGroup
		put(CodeablePBXGroup.CodingKeys.name, null);
		put(CodeablePBXGroup.CodingKeys.path, null);
		put(CodeablePBXGroup.CodingKeys.sourceTree, null);
		put(CodeablePBXGroup.CodingKeys.children, null);

		// PBXHeadersBuildPhase
		put(CodeablePBXHeadersBuildPhase.CodingKeys.files, forKey("files", listOf(object(PBXBuildFile.class))));

		// PBXLegacyTarget
		put(CodeablePBXLegacyTarget.CodingKeys.name, forKey("name", input(string())));
		put(CodeablePBXLegacyTarget.CodingKeys.productName, forKey("productName", input(string())));
		put(CodeablePBXLegacyTarget.CodingKeys.productType, forKey("productType", input(productType())));
		put(CodeablePBXLegacyTarget.CodingKeys.productReference, forKey("productReference", outputLocation()));
		put(CodeablePBXLegacyTarget.CodingKeys.dependencies, null);
		put(CodeablePBXLegacyTarget.CodingKeys.buildConfigurationList, forKey("buildConfigurationList", object(XCConfigurationList.class)));
		put(CodeablePBXLegacyTarget.CodingKeys.buildPhases, forKey("buildPhases", listOf(buildPhaseObject())));
		put(CodeablePBXLegacyTarget.CodingKeys.buildArgumentsString, forKey("buildArgumentsString", input(string())));
		put(CodeablePBXLegacyTarget.CodingKeys.buildToolPath, forKey("buildToolPath", input(string())));
		put(CodeablePBXLegacyTarget.CodingKeys.buildWorkingDirectory, forKey("buildWorkingDirectory", input(string()))); // TODO: capture resolved working directory PATH only
		put(CodeablePBXLegacyTarget.CodingKeys.passBuildSettingsInEnvironment, forKey("passBuildSettingsInEnvironment", input(trueFalseBoolean())));

		// PBXNativeTarget
		put(CodeablePBXNativeTarget.CodingKeys.name, forKey("name", input(string())));
		put(CodeablePBXNativeTarget.CodingKeys.productName, forKey("productName", input(string())));
		put(CodeablePBXNativeTarget.CodingKeys.productType, forKey("productType", input(productType())));
		put(CodeablePBXNativeTarget.CodingKeys.productReference, forKey("productReference", outputLocation()));
		put(CodeablePBXNativeTarget.CodingKeys.dependencies, null);
		put(CodeablePBXNativeTarget.CodingKeys.buildConfigurationList, forKey("buildConfigurationList", object(XCConfigurationList.class)));
		put(CodeablePBXNativeTarget.CodingKeys.buildPhases, forKey("buildPhases", listOf(buildPhaseObject())));
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
		put(CodeablePBXResourcesBuildPhase.CodingKeys.files, forKey("files", listOf(object(PBXBuildFile.class))));

		// PBXShellScriptBuildPhase
		put(CodeablePBXShellScriptBuildPhase.CodingKeys.name, forKey("name", input(string())));
		put(CodeablePBXShellScriptBuildPhase.CodingKeys.files, forKey("files", listOf(object(PBXBuildFile.class))));
		put(CodeablePBXShellScriptBuildPhase.CodingKeys.shellPath, forKey("shellPath", input(string())));
		put(CodeablePBXShellScriptBuildPhase.CodingKeys.shellScript, forKey("shellScript", input(string())));
		put(CodeablePBXShellScriptBuildPhase.CodingKeys.inputPaths, forKey("inputPaths", listOf(inputLocation())));
		put(CodeablePBXShellScriptBuildPhase.CodingKeys.inputFileListPaths, null);
		put(CodeablePBXShellScriptBuildPhase.CodingKeys.outputPaths, forKey("outputPaths", listOf(outputLocation())));
		put(CodeablePBXShellScriptBuildPhase.CodingKeys.outputFileListPaths, null);

		// PBXSourcesBuildPhase
		put(CodeablePBXSourcesBuildPhase.CodingKeys.files, forKey("files", listOf(object(PBXBuildFile.class))));

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
		put(CodeableVersionRequirementBranch.CodingKeys.kind, forKey("kind", input(kind())));
		put(CodeableVersionRequirementBranch.CodingKeys.branch, forKey("branch", input(string())));

		// VersionRequirement.Exact
		put(CodeableVersionRequirementExact.CodingKeys.kind, forKey("kind", input(kind())));
		put(CodeableVersionRequirementExact.CodingKeys.version, forKey("version", input(string())));

		// VersionRequirement.Range
		put(CodeableVersionRequirementRange.CodingKeys.kind, forKey("kind", input(kind())));
		put(CodeableVersionRequirementRange.CodingKeys.minimumVersion, forKey("minimumVersion", input(string())));
		put(CodeableVersionRequirementRange.CodingKeys.maximumVersion, forKey("maximumVersion", input(string())));

		// VersionRequirement.Revision
		put(CodeableVersionRequirementRevision.CodingKeys.kind, forKey("kind", input(kind())));
		put(CodeableVersionRequirementRevision.CodingKeys.revision, forKey("revision", input(string())));

		// VersionRequirement.UpToNextMajorVersion
		put(CodeableVersionRequirementUpToNextMajorVersion.CodingKeys.kind, forKey("kind", input(kind())));
		put(CodeableVersionRequirementUpToNextMajorVersion.CodingKeys.minimumVersion, forKey("minimumVersion", input(string())));

		// VersionRequirement.UpToNextMinorVersion
		put(CodeableVersionRequirementUpToNextMinorVersion.CodingKeys.kind, forKey("kind", input(kind())));
		put(CodeableVersionRequirementUpToNextMinorVersion.CodingKeys.minimumVersion, forKey("minimumVersion", input(string())));

		// XCBuildConfiguration
		put(CodeableXCBuildConfiguration.CodingKeys.name, forKey("name", input(string())));
		put(CodeableXCBuildConfiguration.CodingKeys.buildSettings, null);
		put(CodeableXCBuildConfiguration.CodingKeys.baseConfigurationReference, null);

		// XCConfigurationList
		put(CodeableXCConfigurationList.CodingKeys.defaultConfigurationIsVisible, null);
//		put(CodeableXCConfigurationList.CodingKeys.buildConfigurations, forKey("buildConfigurations", listOf(object(XCBuildConfiguration.class))));
		put(CodeableXCConfigurationList.CodingKeys.buildConfigurations, null); // don't really care about the available configurations
		put(CodeableXCConfigurationList.CodingKeys.defaultConfigurationName, null);

		// XCRemoteSwiftPackageReference
		put(CodeableXCRemoteSwiftPackageReference.CodingKeys.repositoryUrl, forKey("repositoryURL", input(string())));
		put(CodeableXCRemoteSwiftPackageReference.CodingKeys.requirement, forKey("requirement", object(XCRemoteSwiftPackageReference.VersionRequirement.class)));

		// XCSwiftPackageProductDependency
		put(CodeableXCSwiftPackageProductDependency.CodingKeys.productName, forKey("productName", input(string())));
		put(CodeableXCSwiftPackageProductDependency.CodingKeys.packageReference, forKey("package", object(XCRemoteSwiftPackageReference.class)));

		// XCVersionGroup
		put(CodeableXCVersionGroup.CodingKeys.name, null);
		put(CodeableXCVersionGroup.CodingKeys.path, null);
		put(CodeableXCVersionGroup.CodingKeys.sourceTree, null);
		put(CodeableXCVersionGroup.CodingKeys.children, null);
		put(CodeableXCVersionGroup.CodingKeys.currentVersion, null);
		put(CodeableXCVersionGroup.CodingKeys.versionGroupType, null);
	}});

	private static <IN extends PBXBuildPhase & Encodeable> ValueEncoder<XCBuildSpec, IN> buildPhaseObject() {
		return new NestedObjectSpecEncoder<>(new FixPBXCopyFileBuildPhaseEncoder<>());
	}

	@Override
	public Optional<KeyedCoder<?>> get(CodingKey key) {
		return Optional.ofNullable(coders.get(key));
	}

	private static <T> FieldCoder<T> forKey(String key, ValueEncoder<?, T> valueCoder) {
		return new FieldCoder<>(key, new EncoderOnlyCoder<>(valueCoder));
	}

	private static <T extends Codeable, U /*extends PBXObject*/> ValueEncoder<XCBuildSpec, T> object(Class<U> type) {
		return new NestedObjectSpecEncoder<>(new NoOpEncoder<>());
	}

	private static ValueEncoder<Map<String, ?>, Map<String, ?>> dictionary() {
		return newDictionaryEncoder();
	}

	private static ValueEncoder<Boolean, Boolean> trueFalseBoolean() {
		return new FalseTrueBooleanEncoder();
	}

	private static <IN> ValueEncoder<XCBuildSpec, IN> input(ValueEncoder<?, IN> encoder) {
		return new InputSpecEncoder<>(encoder);
	}

	private static ValueEncoder<String, ProductType> productType() {
		return new ProductTypeEncoder();
	}

	private static ValueEncoder<String, XCRemoteSwiftPackageReference.VersionRequirement.Kind> kind() {
		return new VersionRequirementKindEncoder();
	}

	private static ValueEncoder<String, String> string() {
		return newStringEncoder();
	}

	private static ValueEncoder<XCBuildSpec, Object> inputLocation() {
		return new InputLocationSpecEncoder<>(new NormalizeStringAsPBXReferenceEncoder(new NormalizePBXBuildFileFileReferenceAsPBXReferenceEncoder()));
	}

	private static ValueEncoder<XCBuildSpec, Object> outputLocation() {
		return new OutputLocationSpecEncoder<>(new NormalizeStringAsPBXReferenceEncoder(new NormalizePBXBuildFileFileReferenceAsPBXReferenceEncoder()));
	}

	private static <T> ValueEncoder<XCBuildSpec, List<T>> listOf(ValueEncoder<XCBuildSpec, T> elementEncoder) {
		return new CompositeSpecEncoder<>(new ListEncoder<>(elementEncoder));
	}

	private static final class EncoderOnlyCoder<T> implements ValueCoder<T> {
		private final ValueEncoder<?, T> encoder;

		private EncoderOnlyCoder(ValueEncoder<?, T> encoder) {
			this.encoder = encoder;
		}

		@Override
		public T decode(Object object, ValueDecoder.Context context) {
			throw new UnsupportedOperationException();
		}

		@Override
		public Object encode(T value, ValueEncoder.Context context) {
			return encoder.encode(value, context);
		}
	}
}
