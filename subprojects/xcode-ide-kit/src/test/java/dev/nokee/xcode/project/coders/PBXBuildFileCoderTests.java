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

import dev.nokee.xcode.project.CodeablePBXBuildFile;
import dev.nokee.xcode.project.CodingKey;
import dev.nokee.xcode.project.KeyedCoders;
import org.hamcrest.MatcherAssert;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static dev.nokee.xcode.project.CodeablePBXBuildFile.CodingKeys.fileRef;
import static dev.nokee.xcode.project.CodeablePBXBuildFile.CodingKeys.productRef;
import static dev.nokee.xcode.project.CodeablePBXBuildFile.CodingKeys.settings;
import static dev.nokee.xcode.project.coders.KeyedCoderMatcherBuilder.ofKey;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.notNullValue;

@Nested
class PBXBuildFileCoderTests {
	@ParameterizedTest
	@EnumSource(CodeablePBXBuildFile.CodingKeys.class)
	void hasCodersForEachCodingKeys(CodingKey key) {
		MatcherAssert.assertThat(KeyedCoders.DECODERS.get(key), notNullValue());
	}

	@Test
	void hasFileRefCoder() {
		assertThat(KeyedCoders.DECODERS.get(fileRef), ofKey("fileRef").asRefObject(FileReferenceFactory.class));
	}

	@Test
	void hasProductRefCoder() {
		assertThat(KeyedCoders.DECODERS.get(productRef), ofKey("productRef").asRefObject(XCSwiftPackageProductDependencyFactory.class));
	}

	@Test
	void hasSettingsCoder() {
		assertThat(KeyedCoders.DECODERS.get(settings), ofKey("settings").asDictionary());
	}
}
