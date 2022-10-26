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

import dev.nokee.xcode.project.CodeablePBXProject;
import dev.nokee.xcode.project.CodingKey;
import dev.nokee.xcode.project.KeyedCoders;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.buildConfigurationList;
import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.compatibilityVersion;
import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.mainGroup;
import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.packageReferences;
import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.projectReferences;
import static dev.nokee.xcode.project.CodeablePBXProject.CodingKeys.targets;
import static dev.nokee.xcode.project.coders.KeyedCoderMatcherBuilder.ofKey;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@Nested
class PBXProjectCoderTests {
	@ParameterizedTest
	@EnumSource(CodeablePBXProject.CodingKeys.class)
	void hasCodersForEachCodingKeys(CodingKey key) {
		MatcherAssert.assertThat(KeyedCoders.DECODERS.get(key), notNullValue());
	}

	@Test
	void hasMainGroupCoder() {
		assertThat(KeyedCoders.DECODERS.get(mainGroup), ofKey("mainGroup").asRefObject(PBXGroupFactory.class));
	}

	@Test
	void hasTargetsCoder() {
		assertThat(KeyedCoders.DECODERS.get(targets), ofKey("targets").asListOfRefObject(TargetFactory.class));
	}

	@Test
	void hasBuildConfigurationListCoder() {
		assertThat(KeyedCoders.DECODERS.get(buildConfigurationList), ofKey("buildConfigurationList").asRefObject(XCConfigurationListFactory.class));
	}

	@Test
	void hasCompatibilityVersionCoder() {
		assertThat(KeyedCoders.DECODERS.get(compatibilityVersion), ofKey("compatibilityVersion").asString());
	}

	@Test
	void hasProjectReferencesCoder() {
		assertThat(KeyedCoders.DECODERS.get(projectReferences), ofKey("projectReferences").asListOfObject(ProjectReferenceFactory.class));
	}

	@Test
	void hasPackageReferencesCoder() {
		assertThat(KeyedCoders.DECODERS.get(packageReferences), ofKey("packageReferences").asListOfRefObject(XCRemoteSwiftPackageReferenceFactory.class));
	}
}
