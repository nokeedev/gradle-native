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

import dev.nokee.xcode.project.CodeablePBXCopyFilesBuildPhase;
import dev.nokee.xcode.project.CodingKey;
import dev.nokee.xcode.project.KeyedCoders;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static dev.nokee.xcode.project.CodeablePBXCopyFilesBuildPhase.CodingKeys.dstPath;
import static dev.nokee.xcode.project.CodeablePBXCopyFilesBuildPhase.CodingKeys.dstSubfolderSpec;
import static dev.nokee.xcode.project.CodeablePBXCopyFilesBuildPhase.CodingKeys.files;
import static dev.nokee.xcode.project.coders.KeyedCoderMatcherBuilder.ofKey;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@Nested
class PBXCopyFilesBuildPhaseCoderTests {
	@ParameterizedTest
	@EnumSource(CodeablePBXCopyFilesBuildPhase.CodingKeys.class)
	void hasCodersForEachCodingKeys(CodingKey key) {
		MatcherAssert.assertThat(KeyedCoders.DECODERS.get(key), notNullValue());
	}

	@Test
	void hasFilesCoder() {
		assertThat(KeyedCoders.DECODERS.get(files), ofKey("files").asListOfRefObject(PBXBuildFileFactory.class));
	}

	@Test
	void hasDstPathCoder() {
		assertThat(KeyedCoders.DECODERS.get(dstPath), ofKey("dstPath").asString());
	}

	@Test
	void hasDstSubfolderSpecCoder() {
		assertThat(KeyedCoders.DECODERS.get(dstSubfolderSpec), ofKey("dstSubfolderSpec").as(SubFolderCoder.class));
	}
}
