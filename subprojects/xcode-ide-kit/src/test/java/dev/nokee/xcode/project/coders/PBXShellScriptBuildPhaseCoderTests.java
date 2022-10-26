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

import dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase;
import dev.nokee.xcode.project.CodingKey;
import dev.nokee.xcode.project.KeyedCoders;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase.CodingKeys.files;
import static dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase.CodingKeys.inputFileListPaths;
import static dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase.CodingKeys.inputPaths;
import static dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase.CodingKeys.name;
import static dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase.CodingKeys.outputFileListPaths;
import static dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase.CodingKeys.outputPaths;
import static dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase.CodingKeys.shellPath;
import static dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase.CodingKeys.shellScript;
import static dev.nokee.xcode.project.coders.KeyedCoderMatcherBuilder.ofKey;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@Nested
class PBXShellScriptBuildPhaseCoderTests {
	@ParameterizedTest
	@EnumSource(CodeablePBXShellScriptBuildPhase.CodingKeys.class)
	void hasCodersForEachCodingKeys(CodingKey key) {
		MatcherAssert.assertThat(KeyedCoders.DECODERS.get(key), notNullValue());
	}

	@Test
	void hasFilesCoder() {
		assertThat(KeyedCoders.DECODERS.get(files), ofKey("files").asListOfRefObject(PBXBuildFileFactory.class));
	}

	@Test
	void hasNameCoder() {
		assertThat(KeyedCoders.DECODERS.get(name), ofKey("name").asString());
	}

	@Test
	void hasShellScriptCoder() {
		assertThat(KeyedCoders.DECODERS.get(shellScript), ofKey("shellScript").asString());
	}

	@Test
	void hasShellPathCoder() {
		assertThat(KeyedCoders.DECODERS.get(shellPath), ofKey("shellPath").asString());
	}

	@Test
	void hasInputPathsCoder() {
		assertThat(KeyedCoders.DECODERS.get(inputPaths), ofKey("inputPaths").asListOfString());
	}

	@Test
	void hasInputFileListPathsCoder() {
		assertThat(KeyedCoders.DECODERS.get(inputFileListPaths), ofKey("inputFileListPaths").asListOfString());
	}

	@Test
	void hasOutputPathsCoder() {
		assertThat(KeyedCoders.DECODERS.get(outputPaths), ofKey("outputPaths").asListOfString());
	}

	@Test
	void hasOutputFileListPathsCoder() {
		assertThat(KeyedCoders.DECODERS.get(outputFileListPaths), ofKey("outputFileListPaths").asListOfString());
	}
}
