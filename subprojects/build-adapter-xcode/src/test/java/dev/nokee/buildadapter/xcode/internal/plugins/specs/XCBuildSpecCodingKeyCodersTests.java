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
package dev.nokee.buildadapter.xcode.internal.plugins.specs;

import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind;
import dev.nokee.xcode.objects.targets.ProductType;
import dev.nokee.xcode.project.CodeablePBXAggregateTarget;
import dev.nokee.xcode.project.CodeablePBXBuildFile;
import dev.nokee.xcode.project.CodeablePBXContainerItemProxy;
import dev.nokee.xcode.project.CodeablePBXCopyFilesBuildPhase;
import dev.nokee.xcode.project.CodeablePBXFileReference;
import dev.nokee.xcode.project.CodeablePBXFrameworksBuildPhase;
import dev.nokee.xcode.project.CodeablePBXGroup;
import dev.nokee.xcode.project.CodeablePBXHeadersBuildPhase;
import dev.nokee.xcode.project.CodeablePBXLegacyTarget;
import dev.nokee.xcode.project.CodeablePBXNativeTarget;
import dev.nokee.xcode.project.CodeablePBXProject;
import dev.nokee.xcode.project.CodeablePBXReferenceProxy;
import dev.nokee.xcode.project.CodeablePBXResourcesBuildPhase;
import dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase;
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
import dev.nokee.xcode.project.KeyedCoder;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.coders.CoderType;
import dev.nokee.xcode.project.coders.FieldCoder;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.ArgumentsProvider;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.stream.Stream;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.buildadapter.xcode.internal.plugins.specs.CompositeSpecEncoder.nested;
import static dev.nokee.buildadapter.xcode.internal.plugins.specs.InputSpecEncoder.input;
import static dev.nokee.xcode.project.coders.CoderType.dict;
import static dev.nokee.xcode.project.coders.CoderType.list;
import static dev.nokee.xcode.project.coders.CoderType.of;
import static dev.nokee.xcode.project.coders.CoderType.string;
import static dev.nokee.xcode.project.coders.CoderType.trueFalseBoolean;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.params.provider.Arguments.arguments;

class XCBuildSpecCodingKeyCodersTests {
	XCBuildSpecCodingKeyCoders subject = new XCBuildSpecCodingKeyCoders();

	@ParameterizedTest
	@ArgumentsSource(CodingKeysProvider.class)
	void bob(CodingKey key, Matcher<Optional<KeyedCoder<?>>> expected) {
		assertThat(subject.get(key), expected);
	}

	private static final class CodingKeysProvider implements ArgumentsProvider {
		private static final Collection<Arguments> ARGUMENTS = Collections.unmodifiableCollection(new ArrayList<Arguments>() {{
			add(arguments(KeyedCoders.ISA, toKey("isa", input(string()))));

			add(arguments(CodeablePBXAggregateTarget.CodingKeys.name, toKey("name", input(string()))));
			add(arguments(CodeablePBXAggregateTarget.CodingKeys.productName, toKey("productName", input(string()))));
			add(arguments(CodeablePBXAggregateTarget.CodingKeys.productType, toKey("productType", input(of(ProductType.class)))));
			add(arguments(CodeablePBXAggregateTarget.CodingKeys.productReference, toKey("productReference", outputLocation(resolvablePaths()))));
			add(arguments(CodeablePBXAggregateTarget.CodingKeys.dependencies, ignore())); // not required (3)
			add(arguments(CodeablePBXAggregateTarget.CodingKeys.buildConfigurationList, toKey("buildConfigurationList", object(/*XCConfigurationList*/))));
			add(arguments(CodeablePBXAggregateTarget.CodingKeys.buildPhases, toKey("buildPhases", nested(list(object(/*PBXBuildPhase*/))))));

			add(arguments(CodeablePBXBuildFile.CodingKeys.fileRef, toKey("fileRef", inputLocation(resolvablePaths())))); // TODO: Input files
			add(arguments(CodeablePBXBuildFile.CodingKeys.productRef, toKey("productRef", object())));
			add(arguments(CodeablePBXBuildFile.CodingKeys.settings, toKey("settings", input(dict()))));

			add(arguments(CodeablePBXContainerItemProxy.CodingKeys.containerPortal, ignore()));
			add(arguments(CodeablePBXContainerItemProxy.CodingKeys.proxyType, ignore()));
			add(arguments(CodeablePBXContainerItemProxy.CodingKeys.remoteGlobalIDString, ignore()));
			add(arguments(CodeablePBXContainerItemProxy.CodingKeys.remoteInfo, ignore()));

			add(arguments(CodeablePBXCopyFilesBuildPhase.CodingKeys.files, toKey("files", nested(list(object(/*PBXBuildFile*/))))));
			add(arguments(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstSubfolderSpec, ignore()));
			add(arguments(CodeablePBXCopyFilesBuildPhase.CodingKeys.dstPath, ignore()));
			add(arguments(XCBuildSpecCodingKeyCoders.DESTINATION, toKey("destination", nested(list(outputLocation(resolvablePaths()))))));

			add(arguments(CodeablePBXFileReference.CodingKeys.name, ignore()));
			add(arguments(CodeablePBXFileReference.CodingKeys.path, ignore()));
			add(arguments(CodeablePBXFileReference.CodingKeys.sourceTree, ignore()));
			add(arguments(CodeablePBXFileReference.CodingKeys.explicitFileType, ignore()));
			add(arguments(CodeablePBXFileReference.CodingKeys.lastKnownFileType, ignore()));

			add(arguments(CodeablePBXFrameworksBuildPhase.CodingKeys.files, toKey("files", nested(list(object(/*PBXBuildFile*/))))));

			// TODO: PBXGroup don't make sense for target build spec...
			add(arguments(CodeablePBXGroup.CodingKeys.name, ignore()));
			add(arguments(CodeablePBXGroup.CodingKeys.path, ignore()));
			add(arguments(CodeablePBXGroup.CodingKeys.sourceTree, ignore()));
			add(arguments(CodeablePBXGroup.CodingKeys.children, ignore()));

			add(arguments(CodeablePBXHeadersBuildPhase.CodingKeys.files, toKey("files", nested(list(object(/*PBXBuildFile*/))))));

			add(arguments(CodeablePBXLegacyTarget.CodingKeys.name, toKey("name", input(string()))));
			add(arguments(CodeablePBXLegacyTarget.CodingKeys.productName, toKey("productName", input(string()))));
			add(arguments(CodeablePBXLegacyTarget.CodingKeys.productType, toKey("productType", input(of(ProductType.class)))));
			add(arguments(CodeablePBXLegacyTarget.CodingKeys.productReference, toKey("productReference", outputLocation(resolvablePaths()))));
			add(arguments(CodeablePBXLegacyTarget.CodingKeys.dependencies, ignore())); // not required (3)
			add(arguments(CodeablePBXLegacyTarget.CodingKeys.buildConfigurationList, toKey("buildConfigurationList", object(/*XCConfigurationList*/))));
			add(arguments(CodeablePBXLegacyTarget.CodingKeys.buildPhases, toKey("buildPhases", nested(list(object(/*PBXBuildPhase*/))))));
			add(arguments(CodeablePBXLegacyTarget.CodingKeys.buildToolPath, toKey("buildToolPath", input(string())))); // TODO: Should we snapshot the build tool path version?
			add(arguments(CodeablePBXLegacyTarget.CodingKeys.buildArgumentsString, toKey("buildArgumentsString", input(string()))));
			add(arguments(CodeablePBXLegacyTarget.CodingKeys.buildWorkingDirectory, toKey("buildWorkingDirectory", input(string())))); // TODO: We should snapshot the normalized path, not the content
			add(arguments(CodeablePBXLegacyTarget.CodingKeys.passBuildSettingsInEnvironment, toKey("passBuildSettingsInEnvironment", input(trueFalseBoolean()))));

			add(arguments(CodeablePBXNativeTarget.CodingKeys.name, toKey("name", input(string()))));
			add(arguments(CodeablePBXNativeTarget.CodingKeys.productName, toKey("productName", input(string()))));
			add(arguments(CodeablePBXNativeTarget.CodingKeys.productType, toKey("productType", input(of(ProductType.class)))));
			add(arguments(CodeablePBXNativeTarget.CodingKeys.productReference, toKey("productReference", outputLocation(resolvablePaths()))));
			add(arguments(CodeablePBXNativeTarget.CodingKeys.dependencies, ignore())); // not required (3)
			add(arguments(CodeablePBXNativeTarget.CodingKeys.buildConfigurationList, toKey("buildConfigurationList", object(/*XCConfigurationList*/))));
			add(arguments(CodeablePBXNativeTarget.CodingKeys.buildPhases, toKey("buildPhases", nested(list(object(/*PBXBuildPhase*/))))));
			add(arguments(CodeablePBXNativeTarget.CodingKeys.packageProductDependencies, ignore())); // not required (5)

			add(arguments(CodeablePBXProject.CodingKeys.mainGroup, ignore())); // not required (2)
			add(arguments(CodeablePBXProject.CodingKeys.projectReferences, ignore())); // not required (2)
			add(arguments(CodeablePBXProject.CodingKeys.buildConfigurationList, ignore())); // not required (2)
			add(arguments(CodeablePBXProject.CodingKeys.packageReferences, ignore())); // not required (2)
			add(arguments(CodeablePBXProject.CodingKeys.compatibilityVersion, ignore())); // not required (2)
			add(arguments(CodeablePBXProject.CodingKeys.targets, ignore())); // not required (2)

			add(arguments(CodeablePBXReferenceProxy.CodingKeys.name, ignore()));
			add(arguments(CodeablePBXReferenceProxy.CodingKeys.path, ignore()));
			add(arguments(CodeablePBXReferenceProxy.CodingKeys.sourceTree, ignore()));
			add(arguments(CodeablePBXReferenceProxy.CodingKeys.remoteRef, ignore()));
			add(arguments(CodeablePBXReferenceProxy.CodingKeys.fileType, ignore()));

			add(arguments(CodeablePBXResourcesBuildPhase.CodingKeys.files, toKey("files", nested(list(object(/*PBXBuildFile*/))))));

			add(arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.files, toKey("files", nested(list(object(/*PBXBuildFile*/))))));
			add(arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.name, toKey("name", input(string()))));
			add(arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.shellPath, toKey("shellPath", input(string()))));
			add(arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.shellScript, toKey("shellScript", input(string()))));
			add(arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.inputPaths, toKey("inputPaths", nested(list(inputLocation(resolvablePaths()))))));
			add(arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.inputFileListPaths, ignore())); // TODO: implement support
			add(arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.outputPaths, toKey("outputPaths", nested(list(outputLocation(resolvablePaths()))))));
			add(arguments(CodeablePBXShellScriptBuildPhase.CodingKeys.outputFileListPaths, ignore())); // TODO: implement support

			add(arguments(CodeablePBXResourcesBuildPhase.CodingKeys.files, toKey("files", nested(list(object(/*PBXBuildFile*/))))));

			add(arguments(CodeablePBXTargetDependency.CodingKeys.name, ignore())); // not required (4)
			add(arguments(CodeablePBXTargetDependency.CodingKeys.target, ignore())); // not required (4)
			add(arguments(CodeablePBXTargetDependency.CodingKeys.targetProxy, ignore())); // not required (4)

			add(arguments(CodeablePBXVariantGroup.CodingKeys.name, ignore()));
			add(arguments(CodeablePBXVariantGroup.CodingKeys.path, ignore()));
			add(arguments(CodeablePBXVariantGroup.CodingKeys.sourceTree, ignore()));
			add(arguments(CodeablePBXVariantGroup.CodingKeys.children, ignore()));

			add(arguments(CodeableProjectReference.CodingKeys.ProjectRef, ignore()));
			add(arguments(CodeableProjectReference.CodingKeys.ProductGroup, ignore()));

			add(arguments(CodeableVersionRequirementBranch.CodingKeys.kind, toKey("kind", input(of(Kind.class)))));
			add(arguments(CodeableVersionRequirementBranch.CodingKeys.branch, toKey("branch", input(string()))));

			add(arguments(CodeableVersionRequirementExact.CodingKeys.kind, toKey("kind", input(of(Kind.class)))));
			add(arguments(CodeableVersionRequirementExact.CodingKeys.version, toKey("version", input(string()))));

			add(arguments(CodeableVersionRequirementRange.CodingKeys.kind, toKey("kind", input(of(Kind.class)))));
			add(arguments(CodeableVersionRequirementRange.CodingKeys.minimumVersion, toKey("minimumVersion", input(string()))));
			add(arguments(CodeableVersionRequirementRange.CodingKeys.maximumVersion, toKey("maximumVersion", input(string()))));

			add(arguments(CodeableVersionRequirementRevision.CodingKeys.kind, toKey("kind", input(of(Kind.class)))));
			add(arguments(CodeableVersionRequirementRevision.CodingKeys.revision, toKey("revision", input(string()))));

			add(arguments(CodeableVersionRequirementUpToNextMajorVersion.CodingKeys.kind, toKey("kind", input(of(Kind.class)))));
			add(arguments(CodeableVersionRequirementUpToNextMajorVersion.CodingKeys.minimumVersion, toKey("minimumVersion", input(string()))));

			add(arguments(CodeableVersionRequirementUpToNextMinorVersion.CodingKeys.kind, toKey("kind", input(of(Kind.class)))));
			add(arguments(CodeableVersionRequirementUpToNextMinorVersion.CodingKeys.minimumVersion, toKey("minimumVersion", input(string()))));

			add(arguments(CodeableXCBuildConfiguration.CodingKeys.name, toKey("name", input(string()))));
			add(arguments(CodeableXCBuildConfiguration.CodingKeys.baseConfigurationReference, ignore())); // ignore for now
			add(arguments(CodeableXCBuildConfiguration.CodingKeys.buildSettings, ignore())); // ignores for now

			add(arguments(CodeableXCConfigurationList.CodingKeys.buildConfigurations, toKey("buildConfigurations", nested(list(object())))));
			add(arguments(CodeableXCConfigurationList.CodingKeys.defaultConfigurationIsVisible, ignore())); // not required (6)
			add(arguments(CodeableXCConfigurationList.CodingKeys.defaultConfigurationName, ignore())); // not required (6)

			add(arguments(CodeableXCRemoteSwiftPackageReference.CodingKeys.repositoryUrl, toKey("repositoryURL", input(string())))); // TODO: Should we normalize the path?
			add(arguments(CodeableXCRemoteSwiftPackageReference.CodingKeys.requirement, toKey("requirement", object())));

			add(arguments(CodeableXCSwiftPackageProductDependency.CodingKeys.productName, toKey("productName", input(string()))));
			add(arguments(CodeableXCSwiftPackageProductDependency.CodingKeys.packageReference, toKey("package", object(/*XCRemoteSwiftPackageReference*/))));

			add(arguments(CodeableXCVersionGroup.CodingKeys.name, ignore()));
			add(arguments(CodeableXCVersionGroup.CodingKeys.path, ignore()));
			add(arguments(CodeableXCVersionGroup.CodingKeys.sourceTree, ignore()));
			add(arguments(CodeableXCVersionGroup.CodingKeys.children, ignore()));
			add(arguments(CodeableXCVersionGroup.CodingKeys.currentVersion, ignore()));
			add(arguments(CodeableXCVersionGroup.CodingKeys.versionGroupType, ignore()));
			add(arguments(CodeableXCVersionGroup.CodingKeys.versionGroupType, ignore()));
		}});

		@Override
		public Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception {
			return ARGUMENTS.stream();
		}
	}

	// Note 2: PBXProject objects are never captured for target encoding
	// Note 4: target dependencies are tracked differently (aka via input files)
	// Note 5: Swift package dependencies are tracked differently
	// Note 6: don't care about build configurations

	private static <T> Matcher<Optional<? extends KeyedCoder<T>>> toKey(String key, CoderType<XCBuildSpec> expectedType) {
		return optionalWithValue(allOf(isA(FieldCoder.class), new FeatureMatcher<KeyedCoder<T>, String>(equalTo(key), "", "") {
			@Override
			protected String featureValueOf(KeyedCoder<T> actual) {
				assert actual instanceof FieldCoder;
				return ((FieldCoder<T>) actual).getKey();
			}
		}, new FeatureMatcher<KeyedCoder<T>, CoderType<?>>(equalTo(expectedType), "encode type", "encode type") {
			@Override
			protected CoderType<?> featureValueOf(KeyedCoder<T> actual) {
				assert actual instanceof FieldCoder;
				return ((FieldCoder<T>) actual).getDelegate().getEncodeType();
			}
		}));
	}

	private static <T> Matcher<Optional<KeyedCoder<T>>> ignore() {
		return emptyOptional();
	}

	private static CoderType<XCBuildSpec> object() {
		return NestedObjectSpecEncoder.object();
	}

	private static CoderType<Path> resolvablePaths() {
		return of(Path.class);
	}

	private static CoderType<XCBuildSpec> inputLocation(CoderType<Path> type) {
		return InputLocationSpecEncoder.inputLocation(type);
	}

	private static CoderType<XCBuildSpec> outputLocation(CoderType<Path> type) {
		return CoderOutputLocationType.outputFile(type);
	}
}
