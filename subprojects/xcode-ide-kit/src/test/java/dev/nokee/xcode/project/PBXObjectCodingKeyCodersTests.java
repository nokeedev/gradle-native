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
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase;
import dev.nokee.xcode.objects.configuration.BuildSettings;
import dev.nokee.xcode.objects.configuration.XCBuildConfiguration;
import dev.nokee.xcode.objects.configuration.XCConfigurationList;
import dev.nokee.xcode.objects.files.GroupChild;
import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import dev.nokee.xcode.objects.targets.PBXTarget;
import dev.nokee.xcode.objects.targets.PBXTargetDependency;
import dev.nokee.xcode.objects.targets.ProductType;
import dev.nokee.xcode.project.coders.CoderType;
import dev.nokee.xcode.project.coders.FieldCoder;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.Optional;
import java.util.stream.Stream;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.xcode.project.coders.CoderType.anyOf;
import static dev.nokee.xcode.project.coders.CoderType.byCopy;
import static dev.nokee.xcode.project.coders.CoderType.byRef;
import static dev.nokee.xcode.project.coders.CoderType.dict;
import static dev.nokee.xcode.project.coders.CoderType.list;
import static dev.nokee.xcode.project.coders.CoderType.of;
import static dev.nokee.xcode.project.coders.CoderType.oneZeroBoolean;
import static dev.nokee.xcode.project.coders.CoderType.string;
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
				arguments(CodeablePBXAggregateTarget.CodingKeys.productType, keyOf("productType", of(ProductType.class))),
				arguments(CodeablePBXAggregateTarget.CodingKeys.productReference, keyOf("productReference", byRef(of(PBXFileReference.class)))),
				arguments(CodeablePBXAggregateTarget.CodingKeys.dependencies, keyOf("dependencies", list(byRef(of(PBXTargetDependency.class))))),
				arguments(CodeablePBXAggregateTarget.CodingKeys.buildConfigurationList, keyOf("buildConfigurationList", byRef(of(XCConfigurationList.class)))),
				arguments(CodeablePBXAggregateTarget.CodingKeys.buildPhases, keyOf("buildPhases", list(byRef(anyOf(PBXBuildPhase.class))))),

				arguments(CodeablePBXBuildFile.CodingKeys.fileRef, keyOf("fileRef", byRef(anyOf(PBXBuildFile.FileReference.class)))),
				arguments(CodeablePBXBuildFile.CodingKeys.productRef, keyOf("productRef", byRef(of(XCSwiftPackageProductDependency.class)))),
				arguments(CodeablePBXBuildFile.CodingKeys.settings, keyOf("settings", dict())),

				arguments(CodeablePBXContainerItemProxy.CodingKeys.containerPortal, keyOf("containerPortal", byRef(anyOf(PBXContainerItemProxy.ContainerPortal.class)))),
				arguments(CodeablePBXContainerItemProxy.CodingKeys.proxyType, keyOf("proxyType", of(PBXContainerItemProxy.ProxyType.class))),
				arguments(CodeablePBXContainerItemProxy.CodingKeys.remoteGlobalIDString, keyOf("remoteGlobalIDString", string())),
				arguments(CodeablePBXContainerItemProxy.CodingKeys.remoteInfo, keyOf("remoteInfo", string())),

				arguments(CodeablePBXCopyFilesBuildPhase.CodingKeys.files, keyOf("files", list(byRef(of(PBXBuildFile.class))))),
				arguments(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstSubfolderSpec, keyOf("dstSubfolderSpec", of(PBXCopyFilesBuildPhase.SubFolder.class))),
				arguments(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstPath, keyOf("dstPath", string())),

				arguments(CodeablePBXFileReference.CodingKeys.name, keyOf("name", string())),
				arguments(CodeablePBXFileReference.CodingKeys.path, keyOf("path", string())),
				arguments(CodeablePBXFileReference.CodingKeys.sourceTree, keyOf("sourceTree", of(PBXSourceTree.class))),
				arguments(CodeablePBXFileReference.CodingKeys.explicitFileType, keyOf("explicitFileType", string())),
				arguments(CodeablePBXFileReference.CodingKeys.lastKnownFileType, keyOf("lastKnownFileType", string())),

				arguments(CodeablePBXFrameworksBuildPhase.CodingKeys.files, keyOf("files", list(byRef(of(PBXBuildFile.class))))),

				arguments(CodeablePBXGroup.CodingKeys.name, keyOf("name", string())),
				arguments(CodeablePBXGroup.CodingKeys.path, keyOf("path", string())),
				arguments(CodeablePBXGroup.CodingKeys.sourceTree, keyOf("sourceTree", of(PBXSourceTree.class))),
				arguments(CodeablePBXGroup.CodingKeys.children, keyOf("children", list(byRef(anyOf(GroupChild.class))))),

				arguments(CodeablePBXHeadersBuildPhase.CodingKeys.files, keyOf("files", list(byRef(of(PBXBuildFile.class))))),

				arguments(CodeablePBXLegacyTarget.CodingKeys.name, keyOf("name", string())),
				arguments(CodeablePBXLegacyTarget.CodingKeys.productName, keyOf("productName", string())),
				arguments(CodeablePBXLegacyTarget.CodingKeys.productType, keyOf("productType", of(ProductType.class))),
				arguments(CodeablePBXLegacyTarget.CodingKeys.productReference, keyOf("productReference", byRef(of(PBXFileReference.class)))),
				arguments(CodeablePBXLegacyTarget.CodingKeys.dependencies, keyOf("dependencies", list(byRef(of(PBXTargetDependency.class))))),
				arguments(CodeablePBXLegacyTarget.CodingKeys.buildConfigurationList, keyOf("buildConfigurationList", byRef(of(XCConfigurationList.class)))),
				arguments(CodeablePBXLegacyTarget.CodingKeys.buildPhases, keyOf("buildPhases", list(byRef(anyOf(PBXBuildPhase.class))))),
				arguments(CodeablePBXLegacyTarget.CodingKeys.buildToolPath, keyOf("buildToolPath", string())),
				arguments(CodeablePBXLegacyTarget.CodingKeys.buildArgumentsString, keyOf("buildArgumentsString", string())),
				arguments(CodeablePBXLegacyTarget.CodingKeys.buildWorkingDirectory, keyOf("buildWorkingDirectory", string())),
				arguments(CodeablePBXLegacyTarget.CodingKeys.passBuildSettingsInEnvironment, keyOf("passBuildSettingsInEnvironment", oneZeroBoolean())),

				arguments(CodeablePBXNativeTarget.CodingKeys.name, keyOf("name", string())),
				arguments(CodeablePBXNativeTarget.CodingKeys.productName, keyOf("productName", string())),
				arguments(CodeablePBXNativeTarget.CodingKeys.productType, keyOf("productType", of(ProductType.class))),
				arguments(CodeablePBXNativeTarget.CodingKeys.productReference, keyOf("productReference", byRef(of(PBXFileReference.class)))),
				arguments(CodeablePBXNativeTarget.CodingKeys.dependencies, keyOf("dependencies", list(byRef(of(PBXTargetDependency.class))))),
				arguments(CodeablePBXNativeTarget.CodingKeys.buildConfigurationList, keyOf("buildConfigurationList", byRef(of(XCConfigurationList.class)))),
				arguments(CodeablePBXNativeTarget.CodingKeys.buildPhases, keyOf("buildPhases", list(byRef(anyOf(PBXBuildPhase.class))))),
				arguments(CodeablePBXNativeTarget.CodingKeys.packageProductDependencies, keyOf("packageProductDependencies", list(byRef(of(XCSwiftPackageProductDependency.class))))),

				arguments(CodeablePBXProject.CodingKeys.mainGroup, keyOf("mainGroup", byRef(of(PBXGroup.class)))),
				arguments(CodeablePBXProject.CodingKeys.projectReferences, keyOf("projectReferences", list(byCopy(of(PBXProject.ProjectReference.class))))),
				arguments(CodeablePBXProject.CodingKeys.buildConfigurationList, keyOf("buildConfigurationList", byRef(of(XCConfigurationList.class)))),
				arguments(CodeablePBXProject.CodingKeys.packageReferences, keyOf("packageReferences", list(byRef(of(XCRemoteSwiftPackageReference.class))))),
				arguments(CodeablePBXProject.CodingKeys.compatibilityVersion, keyOf("compatibilityVersion", string())),
				arguments(CodeablePBXProject.CodingKeys.targets, keyOf("targets", list(byRef(anyOf(PBXTarget.class))))),

				arguments(CodeablePBXReferenceProxy.CodingKeys.name, keyOf("name", string())),
				arguments(CodeablePBXReferenceProxy.CodingKeys.path, keyOf("path", string())),
				arguments(CodeablePBXReferenceProxy.CodingKeys.sourceTree, keyOf("sourceTree", of(PBXSourceTree.class))),
				arguments(CodeablePBXReferenceProxy.CodingKeys.remoteRef, keyOf("remoteRef", byRef(of(PBXContainerItemProxy.class)))),
				arguments(CodeablePBXReferenceProxy.CodingKeys.fileType, keyOf("fileType", string())),

				arguments(CodeablePBXResourcesBuildPhase.CodingKeys.files, keyOf("files", list(byRef(of(PBXBuildFile.class))))),

				arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.files, keyOf("files", list(byRef(of(PBXBuildFile.class))))),
				arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.name, keyOf("name", string())),
				arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.shellPath, keyOf("shellPath", string())),
				arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.shellScript, keyOf("shellScript", string())),
				arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.inputPaths, keyOf("inputPaths", list(string()))),
				arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.inputFileListPaths, keyOf("inputFileListPaths", list(string()))),
				arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.outputPaths, keyOf("outputPaths", list(string()))),
				arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.outputFileListPaths, keyOf("outputFileListPaths", list(string()))),

				arguments(CodeablePBXResourcesBuildPhase.CodingKeys.files, keyOf("files", list(byRef(of(PBXBuildFile.class))))),

				arguments(CodeablePBXTargetDependency.CodingKeys.name, keyOf("name", string())),
				arguments(CodeablePBXTargetDependency.CodingKeys.target, keyOf("target", byRef(anyOf(PBXTarget.class)))),
				arguments(CodeablePBXTargetDependency.CodingKeys.targetProxy, keyOf("targetProxy", byRef(of(PBXContainerItemProxy.class)))),

				arguments(CodeablePBXVariantGroup.CodingKeys.name, keyOf("name", string())),
				arguments(CodeablePBXVariantGroup.CodingKeys.path, keyOf("path", string())),
				arguments(CodeablePBXVariantGroup.CodingKeys.sourceTree, keyOf("sourceTree", of(PBXSourceTree.class))),
				arguments(CodeablePBXVariantGroup.CodingKeys.children, keyOf("children", list(byRef(anyOf(GroupChild.class))))),

				arguments(CodeableProjectReference.CodingKeys.ProjectRef, keyOf("ProjectRef", byRef(of(PBXFileReference.class)))),
				arguments(CodeableProjectReference.CodingKeys.ProductGroup, keyOf("ProductGroup", byRef(of(PBXGroup.class)))),

				arguments(CodeableVersionRequirementBranch.CodingKeys.kind, keyOf("kind", of(XCRemoteSwiftPackageReference.VersionRequirement.Kind.class))),
				arguments(CodeableVersionRequirementBranch.CodingKeys.branch, keyOf("branch", string())),

				arguments(CodeableVersionRequirementExact.CodingKeys.kind, keyOf("kind", of(XCRemoteSwiftPackageReference.VersionRequirement.Kind.class))),
				arguments(CodeableVersionRequirementExact.CodingKeys.version, keyOf("version", string())),

				arguments(CodeableVersionRequirementRange.CodingKeys.kind, keyOf("kind", of(XCRemoteSwiftPackageReference.VersionRequirement.Kind.class))),
				arguments(CodeableVersionRequirementRange.CodingKeys.minimumVersion, keyOf("minimumVersion", string())),
				arguments(CodeableVersionRequirementRange.CodingKeys.maximumVersion, keyOf("maximumVersion", string())),

				arguments(CodeableVersionRequirementRevision.CodingKeys.kind, keyOf("kind", of(XCRemoteSwiftPackageReference.VersionRequirement.Kind.class))),
				arguments(CodeableVersionRequirementRevision.CodingKeys.revision, keyOf("revision", string())),

				arguments(CodeableVersionRequirementUpToNextMajorVersion.CodingKeys.kind, keyOf("kind", of(XCRemoteSwiftPackageReference.VersionRequirement.Kind.class))),
				arguments(CodeableVersionRequirementUpToNextMajorVersion.CodingKeys.minimumVersion, keyOf("minimumVersion", string())),

				arguments(CodeableVersionRequirementUpToNextMinorVersion.CodingKeys.kind, keyOf("kind", of(XCRemoteSwiftPackageReference.VersionRequirement.Kind.class))),
				arguments(CodeableVersionRequirementUpToNextMinorVersion.CodingKeys.minimumVersion, keyOf("minimumVersion", string())),

				arguments(CodeableXCBuildConfiguration.CodingKeys.name, keyOf("name", string())),
				arguments(CodeableXCBuildConfiguration.CodingKeys.baseConfigurationReference, keyOf("baseConfigurationReference", byRef(of(PBXFileReference.class)))),
				arguments(CodeableXCBuildConfiguration.CodingKeys.buildSettings, keyOf("buildSettings", of(BuildSettings.class))),

				arguments(CodeableXCConfigurationList.CodingKeys.buildConfigurations, keyOf("buildConfigurations", list(byRef(of(XCBuildConfiguration.class))))),
				arguments(CodeableXCConfigurationList.CodingKeys.defaultConfigurationIsVisible, keyOf("defaultConfigurationIsVisible", oneZeroBoolean())),
				arguments(CodeableXCConfigurationList.CodingKeys.defaultConfigurationName, keyOf("defaultConfigurationName", string())),

				arguments(CodeableXCRemoteSwiftPackageReference.CodingKeys.repositoryUrl, keyOf("repositoryURL", string())),
				arguments(CodeableXCRemoteSwiftPackageReference.CodingKeys.requirement, keyOf("requirement", byCopy(anyOf(XCRemoteSwiftPackageReference.VersionRequirement.class)))),

				arguments(CodeableXCSwiftPackageProductDependency.CodingKeys.productName, keyOf("productName", string())),
				arguments(CodeableXCSwiftPackageProductDependency.CodingKeys.packageReference, keyOf("package", byRef(of(XCRemoteSwiftPackageReference.class)))),

				arguments(CodeableXCVersionGroup.CodingKeys.name, keyOf("name", string())),
				arguments(CodeableXCVersionGroup.CodingKeys.path, keyOf("path", string())),
				arguments(CodeableXCVersionGroup.CodingKeys.sourceTree, keyOf("sourceTree", of(PBXSourceTree.class))),
				arguments(CodeableXCVersionGroup.CodingKeys.children, keyOf("children", list(byRef(anyOf(GroupChild.class))))),
				arguments(CodeableXCVersionGroup.CodingKeys.currentVersion, keyOf("currentVersion", byRef(of(PBXFileReference.class)))),
				arguments(CodeableXCVersionGroup.CodingKeys.versionGroupType, keyOf("versionGroupType", string()))
			);
		}

		// Redeclare here to avoid parenthesis mismatch causing some test cases to be ignored.
		private static Arguments arguments(CodingKey codingKey, Matcher<Optional<? extends KeyedCoder<Object>>> matcher) {
			return Arguments.arguments(codingKey, matcher);
		}
	}

	private static <T> Matcher<Optional<? extends KeyedCoder<T>>> keyOf(String key, CoderType<?> expectedType) {
		return optionalWithValue(allOf(isA(FieldCoder.class), new FeatureMatcher<KeyedCoder<T>, String>(equalTo(key), "", "") {
			@Override
			protected String featureValueOf(KeyedCoder<T> actual) {
				assert actual instanceof FieldCoder;
				return ((FieldCoder<T>) actual).getKey();
			}
		}, new FeatureMatcher<KeyedCoder<T>, CoderType<?>>(equalTo(expectedType), "decode type", "decode type") {
			@Override
			protected CoderType<?> featureValueOf(KeyedCoder<T> actual) {
				assert actual instanceof FieldCoder;
				return ((FieldCoder<T>) actual).getDelegate().getDecodeType();
			}
		}, new FeatureMatcher<KeyedCoder<T>, CoderType<?>>(equalTo(expectedType), "encode type", "encode type") {
			@Override
			protected CoderType<?> featureValueOf(KeyedCoder<T> actual) {
				assert actual instanceof FieldCoder;
				return ((FieldCoder<T>) actual).getDelegate().getEncodeType();
			}
		}));
	}
}
