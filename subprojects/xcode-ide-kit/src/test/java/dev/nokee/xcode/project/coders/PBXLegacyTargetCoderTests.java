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
package dev.nokee.xcode.project.coders;

import dev.nokee.xcode.project.CodeablePBXLegacyTarget;
import dev.nokee.xcode.project.CodingKey;
import dev.nokee.xcode.project.KeyedCoders;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static dev.nokee.xcode.project.CodeablePBXLegacyTarget.CodingKeys.buildArgumentsString;
import static dev.nokee.xcode.project.CodeablePBXLegacyTarget.CodingKeys.buildConfigurationList;
import static dev.nokee.xcode.project.CodeablePBXLegacyTarget.CodingKeys.buildPhases;
import static dev.nokee.xcode.project.CodeablePBXLegacyTarget.CodingKeys.buildToolPath;
import static dev.nokee.xcode.project.CodeablePBXLegacyTarget.CodingKeys.buildWorkingDirectory;
import static dev.nokee.xcode.project.CodeablePBXLegacyTarget.CodingKeys.dependencies;
import static dev.nokee.xcode.project.CodeablePBXLegacyTarget.CodingKeys.name;
import static dev.nokee.xcode.project.CodeablePBXLegacyTarget.CodingKeys.passBuildSettingsInEnvironment;
import static dev.nokee.xcode.project.CodeablePBXLegacyTarget.CodingKeys.productName;
import static dev.nokee.xcode.project.CodeablePBXLegacyTarget.CodingKeys.productReference;
import static dev.nokee.xcode.project.CodeablePBXLegacyTarget.CodingKeys.productType;
import static dev.nokee.xcode.project.coders.KeyedCoderMatcherBuilder.ofKey;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@Nested
class PBXLegacyTargetCoderTests {
	@ParameterizedTest
	@EnumSource(CodeablePBXLegacyTarget.CodingKeys.class)
	void hasCodersForEachCodingKeys(CodingKey key) {
		MatcherAssert.assertThat(KeyedCoders.DECODERS.get(key), notNullValue());
	}

	@Test
	void hasBuildArgumentsStringCoder() {
		assertThat(KeyedCoders.DECODERS.get(buildArgumentsString), ofKey("buildArgumentsString").asString());
	}

	@Test
	void hasBuildToolPathCoder() {
		assertThat(KeyedCoders.DECODERS.get(buildToolPath), ofKey("buildToolPath").asString());
	}

	@Test
	void hasBuildWorkingDirectoryCoder() {
		assertThat(KeyedCoders.DECODERS.get(buildWorkingDirectory), ofKey("buildWorkingDirectory").asString());
	}

	@Test
	void hasPassBuildSettingsInEnvironmentCoder() {
		assertThat(KeyedCoders.DECODERS.get(passBuildSettingsInEnvironment), ofKey("passBuildSettingsInEnvironment").asZeroOneBoolean());
	}

	@Test
	void hasNameCoder() {
		assertThat(KeyedCoders.DECODERS.get(name), ofKey("name").asString());
	}

	@Test
	void hasBuildPhasesCoder() {
		assertThat(KeyedCoders.DECODERS.get(buildPhases), ofKey("buildPhases").asListOfRefObject(BuildPhaseFactory.class));
	}

	@Test
	void hasBuildConfigurationListCoder() {
		assertThat(KeyedCoders.DECODERS.get(buildConfigurationList), ofKey("buildConfigurationList").asRefObject(XCConfigurationListFactory.class));
	}

	@Test
	void hasDependenciesCoder() {
		assertThat(KeyedCoders.DECODERS.get(dependencies), ofKey("dependencies").asListOfRefObject(PBXTargetDependencyFactory.class));
	}

	@Test
	void hasProductNameCoder() {
		assertThat(KeyedCoders.DECODERS.get(productName), ofKey("productName").asString());
	}

	@Test
	void hasProductTypeCoder() {
		assertThat(KeyedCoders.DECODERS.get(productType), ofKey("productType").as(ProductTypeCoder.class));
	}

	@Test
	void hasProductReferenceCoder() {
		assertThat(KeyedCoders.DECODERS.get(productReference), ofKey("productReference").asRefObject(PBXFileReferenceFactory.class));
	}
}
