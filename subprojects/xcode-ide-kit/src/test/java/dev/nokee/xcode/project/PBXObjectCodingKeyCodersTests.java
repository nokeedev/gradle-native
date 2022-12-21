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

import dev.nokee.xcode.objects.PBXContainerItemProxy;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;
import dev.nokee.xcode.project.coders.BuildSettingsCoder;
import dev.nokee.xcode.project.coders.DictionaryCoder;
import dev.nokee.xcode.project.coders.FieldCoder;
import dev.nokee.xcode.project.coders.ListCoder;
import dev.nokee.xcode.project.coders.ObjectCoder;
import dev.nokee.xcode.project.coders.ObjectRefCoder;
import dev.nokee.xcode.project.coders.ProductTypeCoder;
import dev.nokee.xcode.project.coders.ProxyTypeCoder;
import dev.nokee.xcode.project.coders.SourceTreeCoder;
import dev.nokee.xcode.project.coders.StringCoder;
import dev.nokee.xcode.project.coders.SubFolderCoder;
import dev.nokee.xcode.project.coders.VersionRequirementKindCoder;
import dev.nokee.xcode.project.coders.ZeroOneBooleanCoder;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class PBXObjectCodingKeyCodersTests {
	PBXObjectCodingKeyCoders subject = new PBXObjectCodingKeyCoders();

	@ParameterizedTest
	@ArgumentsSource(CodingKeysProvider.class)
	void checkCoderForCodingKeys(CodingKey key, Matcher<Optional<KeyedCoder<?>>> expected) {
		assertThat(subject.get(key), expected);
	}

	private static final class CodingKeysProvider implements ArgumentsProvider {
		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return Stream.of(
				arguments(KeyedCoders.ISA, keyOf("isa", string())),

				arguments(CodeablePBXAggregateTarget.CodingKeys.name, keyOf("name", string())),
				arguments(CodeablePBXAggregateTarget.CodingKeys.productName, keyOf("productName", string())),
				arguments(CodeablePBXAggregateTarget.CodingKeys.productType, keyOf("productType", productType())),
				arguments(CodeablePBXAggregateTarget.CodingKeys.productReference, keyOf("productReference", objectRef())),
				arguments(CodeablePBXAggregateTarget.CodingKeys.dependencies, keyOf("dependencies", listOf(objectRef()))),
				arguments(CodeablePBXAggregateTarget.CodingKeys.buildConfigurationList, keyOf("buildConfigurationList", objectRef())),
				arguments(CodeablePBXAggregateTarget.CodingKeys.buildPhases, keyOf("buildPhases", listOf(objectRef()))),

				arguments(CodeablePBXBuildFile.CodingKeys.fileRef, keyOf("fileRef", objectRef())),
				arguments(CodeablePBXBuildFile.CodingKeys.productRef, keyOf("productRef", objectRef())),
				arguments(CodeablePBXBuildFile.CodingKeys.settings, keyOf("settings", dict())),

				arguments(CodeablePBXContainerItemProxy.CodingKeys.containerPortal, keyOf("containerPortal", objectRef())),
				arguments(CodeablePBXContainerItemProxy.CodingKeys.proxyType, keyOf("proxyType", proxyType())),
				arguments(CodeablePBXContainerItemProxy.CodingKeys.remoteGlobalIDString, keyOf("remoteGlobalIDString", string())),
				arguments(CodeablePBXContainerItemProxy.CodingKeys.remoteInfo, keyOf("remoteInfo", string())),

				arguments(CodeablePBXCopyFilesBuildPhase.CodingKeys.files, keyOf("files", listOf(objectRef()))),
				arguments(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstSubfolderSpec, keyOf("dstSubfolderSpec", subfolderSpec())),
				arguments(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstPath, keyOf("dstPath", string())),

				arguments(CodeablePBXFileReference.CodingKeys.name, keyOf("name", string())),
				arguments(CodeablePBXFileReference.CodingKeys.path, keyOf("path", string())),
				arguments(CodeablePBXFileReference.CodingKeys.sourceTree, keyOf("sourceTree", sourceTree())),
				arguments(CodeablePBXFileReference.CodingKeys.explicitFileType, keyOf("explicitFileType", string())),
				arguments(CodeablePBXFileReference.CodingKeys.lastKnownFileType, keyOf("lastKnownFileType", string())),

				arguments(CodeablePBXFrameworksBuildPhase.CodingKeys.files, keyOf("files", listOf(objectRef()))),

				arguments(CodeablePBXGroup.CodingKeys.name, keyOf("name", string())),
				arguments(CodeablePBXGroup.CodingKeys.path, keyOf("path", string())),
				arguments(CodeablePBXGroup.CodingKeys.sourceTree, keyOf("sourceTree", sourceTree())),
				arguments(CodeablePBXGroup.CodingKeys.children, keyOf("children", listOf(objectRef()))),

				arguments(CodeablePBXHeadersBuildPhase.CodingKeys.files, keyOf("files", listOf(objectRef()))),

				arguments(CodeablePBXLegacyTarget.CodingKeys.name, keyOf("name", string())),
				arguments(CodeablePBXLegacyTarget.CodingKeys.productName, keyOf("productName", string())),
				arguments(CodeablePBXLegacyTarget.CodingKeys.productType, keyOf("productType", productType())),
				arguments(CodeablePBXLegacyTarget.CodingKeys.productReference, keyOf("productReference", objectRef())),
				arguments(CodeablePBXLegacyTarget.CodingKeys.dependencies, keyOf("dependencies", listOf(objectRef()))),
				arguments(CodeablePBXLegacyTarget.CodingKeys.buildConfigurationList, keyOf("buildConfigurationList", objectRef())),
				arguments(CodeablePBXLegacyTarget.CodingKeys.buildPhases, keyOf("buildPhases", listOf(objectRef()))),
				arguments(CodeablePBXLegacyTarget.CodingKeys.buildToolPath, keyOf("buildToolPath", string())),
				arguments(CodeablePBXLegacyTarget.CodingKeys.buildArgumentsString, keyOf("buildArgumentsString", string())),
				arguments(CodeablePBXLegacyTarget.CodingKeys.buildWorkingDirectory, keyOf("buildWorkingDirectory", string())),
				arguments(CodeablePBXLegacyTarget.CodingKeys.passBuildSettingsInEnvironment, keyOf("passBuildSettingsInEnvironment", oneZeroBoolean())),

				arguments(CodeablePBXNativeTarget.CodingKeys.name, keyOf("name", string())),
				arguments(CodeablePBXNativeTarget.CodingKeys.productName, keyOf("productName", string())),
				arguments(CodeablePBXNativeTarget.CodingKeys.productType, keyOf("productType", productType())),
				arguments(CodeablePBXNativeTarget.CodingKeys.productReference, keyOf("productReference", objectRef())),
				arguments(CodeablePBXNativeTarget.CodingKeys.dependencies, keyOf("dependencies", listOf(objectRef()))),
				arguments(CodeablePBXNativeTarget.CodingKeys.buildConfigurationList, keyOf("buildConfigurationList", objectRef())),
				arguments(CodeablePBXNativeTarget.CodingKeys.buildPhases, keyOf("buildPhases", listOf(objectRef()))),
				arguments(CodeablePBXNativeTarget.CodingKeys.packageProductDependencies, keyOf("packageProductDependencies", listOf(objectRef()))),

				arguments(CodeablePBXProject.CodingKeys.mainGroup, keyOf("mainGroup", objectRef())),
				arguments(CodeablePBXProject.CodingKeys.projectReferences, keyOf("projectReferences", listOf(object()))),
				arguments(CodeablePBXProject.CodingKeys.buildConfigurationList, keyOf("buildConfigurationList", objectRef())),
				arguments(CodeablePBXProject.CodingKeys.packageReferences, keyOf("packageReferences", listOf(objectRef()))),
				arguments(CodeablePBXProject.CodingKeys.compatibilityVersion, keyOf("compatibilityVersion", string())),
				arguments(CodeablePBXProject.CodingKeys.targets, keyOf("targets", listOf(objectRef()))),

				arguments(CodeablePBXReferenceProxy.CodingKeys.name, keyOf("name", string())),
				arguments(CodeablePBXReferenceProxy.CodingKeys.path, keyOf("path", string())),
				arguments(CodeablePBXReferenceProxy.CodingKeys.sourceTree, keyOf("sourceTree", sourceTree())),
				arguments(CodeablePBXReferenceProxy.CodingKeys.remoteRef, keyOf("remoteRef", objectRef())),
				arguments(CodeablePBXReferenceProxy.CodingKeys.fileType, keyOf("fileType", string())),

				arguments(CodeablePBXResourcesBuildPhase.CodingKeys.files, keyOf("files", listOf(objectRef()))),

				arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.files, keyOf("files", listOf(objectRef()))),
				arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.name, keyOf("name", string())),
				arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.shellPath, keyOf("shellPath", string())),
				arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.shellScript, keyOf("shellScript", string())),
				arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.inputPaths, keyOf("inputPaths", listOf(string()))),
				arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.inputFileListPaths, keyOf("inputFileListPaths", listOf(string()))),
				arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.outputPaths, keyOf("outputPaths", listOf(string()))),
				arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.outputFileListPaths, keyOf("outputFileListPaths", listOf(string()))),

				arguments(CodeablePBXResourcesBuildPhase.CodingKeys.files, keyOf("files", listOf(objectRef()))),

				arguments(CodeablePBXTargetDependency.CodingKeys.name, keyOf("name", string())),
				arguments(CodeablePBXTargetDependency.CodingKeys.target, keyOf("target", objectRef())),
				arguments(CodeablePBXTargetDependency.CodingKeys.targetProxy, keyOf("targetProxy", objectRef())),

				arguments(CodeablePBXVariantGroup.CodingKeys.name, keyOf("name", string())),
				arguments(CodeablePBXVariantGroup.CodingKeys.path, keyOf("path", string())),
				arguments(CodeablePBXVariantGroup.CodingKeys.sourceTree, keyOf("sourceTree", sourceTree())),
				arguments(CodeablePBXVariantGroup.CodingKeys.children, keyOf("children", listOf(objectRef()))),

				arguments(CodeableProjectReference.CodingKeys.ProjectRef, keyOf("ProjectRef", objectRef())),
				arguments(CodeableProjectReference.CodingKeys.ProductGroup, keyOf("ProductGroup", objectRef())),

				arguments(CodeableVersionRequirementBranch.CodingKeys.kind, keyOf("kind", versionRequirementKind())),
				arguments(CodeableVersionRequirementBranch.CodingKeys.branch, keyOf("branch", string())),

				arguments(CodeableVersionRequirementExact.CodingKeys.kind, keyOf("kind", versionRequirementKind())),
				arguments(CodeableVersionRequirementExact.CodingKeys.version, keyOf("version", string())),

				arguments(CodeableVersionRequirementRange.CodingKeys.kind, keyOf("kind", versionRequirementKind())),
				arguments(CodeableVersionRequirementRange.CodingKeys.minimumVersion, keyOf("minimumVersion", string())),
				arguments(CodeableVersionRequirementRange.CodingKeys.maximumVersion, keyOf("maximumVersion", string())),

				arguments(CodeableVersionRequirementRevision.CodingKeys.kind, keyOf("kind", versionRequirementKind())),
				arguments(CodeableVersionRequirementRevision.CodingKeys.revision, keyOf("revision", string())),

				arguments(CodeableVersionRequirementUpToNextMajorVersion.CodingKeys.kind, keyOf("kind", versionRequirementKind())),
				arguments(CodeableVersionRequirementUpToNextMajorVersion.CodingKeys.minimumVersion, keyOf("minimumVersion", string())),

				arguments(CodeableVersionRequirementUpToNextMinorVersion.CodingKeys.kind, keyOf("kind", versionRequirementKind())),
				arguments(CodeableVersionRequirementUpToNextMinorVersion.CodingKeys.minimumVersion, keyOf("minimumVersion", string())),

				arguments(CodeableXCBuildConfiguration.CodingKeys.name, keyOf("name", string())),
				arguments(CodeableXCBuildConfiguration.CodingKeys.baseConfigurationReference, keyOf("baseConfigurationReference", objectRef())),
				arguments(CodeableXCBuildConfiguration.CodingKeys.buildSettings, keyOf("buildSettings", buildSettings())),

				arguments(CodeableXCConfigurationList.CodingKeys.buildConfigurations, keyOf("buildConfigurations", listOf(objectRef()))),
				arguments(CodeableXCConfigurationList.CodingKeys.defaultConfigurationIsVisible, keyOf("defaultConfigurationIsVisible", oneZeroBoolean())),
				arguments(CodeableXCConfigurationList.CodingKeys.defaultConfigurationName, keyOf("defaultConfigurationName", string())),

				arguments(CodeableXCRemoteSwiftPackageReference.CodingKeys.repositoryUrl, keyOf("repositoryURL", string())),
				arguments(CodeableXCRemoteSwiftPackageReference.CodingKeys.requirement, keyOf("requirement", object())),

				arguments(CodeableXCSwiftPackageProductDependency.CodingKeys.productName, keyOf("productName", string())),
				arguments(CodeableXCSwiftPackageProductDependency.CodingKeys.packageReference, keyOf("package", objectRef())),

				arguments(CodeableXCVersionGroup.CodingKeys.name, keyOf("name", string())),
				arguments(CodeableXCVersionGroup.CodingKeys.path, keyOf("path", string())),
				arguments(CodeableXCVersionGroup.CodingKeys.sourceTree, keyOf("sourceTree", sourceTree())),
				arguments(CodeableXCVersionGroup.CodingKeys.children, keyOf("children", listOf(objectRef()))),
				arguments(CodeableXCVersionGroup.CodingKeys.currentVersion, keyOf("currentVersion", objectRef())),
				arguments(CodeableXCVersionGroup.CodingKeys.versionGroupType, keyOf("versionGroupType", string()))
			);
		}
	}

	private static <T> Matcher<Optional<? extends KeyedCoder<T>>> keyOf(String key, Matcher<? super ValueCoder<T>> matcher) {
		return optionalWithValue(allOf(isA(FieldCoder.class), new FeatureMatcher<KeyedCoder<T>, String>(equalTo(key), "", "") {
			@Override
			protected String featureValueOf(KeyedCoder<T> actual) {
				assert actual instanceof FieldCoder;
				return ((FieldCoder<T>) actual).getKey();
			}
		}, new FeatureMatcher<KeyedCoder<T>, ValueCoder<T>>(matcher, "", "") {
			@Override
			protected ValueCoder<T> featureValueOf(KeyedCoder<T> actual) {
				assert actual instanceof FieldCoder;
				return ((FieldCoder<T>) actual).getDelegate();
			}
		}));
	}

	private static Matcher<ValueCoder<String>> string() {
		return isA(StringCoder.class);
	}

	private static Matcher<ValueCoder<String>> oneZeroBoolean() {
		return isA(ZeroOneBooleanCoder.class);
	}

	private static Matcher<ValueCoder<Map<String, ?>>> dict() {
		return isA(DictionaryCoder.class);
	}

	private static <T> Matcher<ValueCoder<List<T>>> listOf(Matcher<? super ValueCoder<T>> matcher) {
		return allOf(isA(ListCoder.class), new FeatureMatcher<ValueCoder<List<T>>, ValueCoder<T>>(matcher, "", "") {
			@Override
			protected ValueCoder<T> featureValueOf(ValueCoder<List<T>> actual) {
				assert actual instanceof ListCoder;
				return ((ListCoder<T>) actual).getDelegate();
			}
		});
	}

	private static <T> Matcher<ValueCoder<T>> object() {
		return isA(ObjectCoder.class);
	}

	private static <T> Matcher<ValueCoder<T>> objectRef() {
		return isA(ObjectRefCoder.class);
	}

	private static Matcher<ValueCoder<String>> productType() {
		return isA(ProductTypeCoder.class);
	}

	private static Matcher<ValueCoder<PBXContainerItemProxy.ProxyType>> proxyType() {
		return isA(ProxyTypeCoder.class);
	}

	private static Matcher<ValueCoder<Integer>> subfolderSpec() {
		return isA(SubFolderCoder.class);
	}

	private static Matcher<ValueCoder<PBXSourceTree>> sourceTree() {
		return isA(SourceTreeCoder.class);
	}

	private static Matcher<ValueCoder<PBXSourceTree>> buildSettings() {
		return isA(BuildSettingsCoder.class);
	}

	private static Matcher<ValueCoder<XCRemoteSwiftPackageReference.VersionRequirement.Kind>> versionRequirementKind() {
		return isA(VersionRequirementKindCoder.class);
	}
}
