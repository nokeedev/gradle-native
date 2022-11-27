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
package dev.nokee.utils;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static dev.nokee.utils.TextCaseUtils.toLowerCamelCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class TextCaseUtils_ToLowerCamelCaseTests {
	@Test
	void returnsEmptyStringAsIs() {
		assertThat(toLowerCamelCase(""), equalTo(""));
	}

	@Test
	void doesNotCapitalizeSingleWorkInputString() {
		assertThat(toLowerCamelCase("word"), equalTo("word"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "twoWords", "TwoWords", "two-words", "two.words", "two words", "two Words", "Two Words" })
	void capitalizesOnlySecondChunkAndJoinsMultipleWordsInputString(String inputString) {
		assertThat(toLowerCamelCase(inputString), equalTo("twoWords"));
	}

	@Test
	void ignoresExtraWhitespaces() {
		assertThat(toLowerCamelCase(" Two  \t words\n"), equalTo("twoWords"));
	}

	@Test
	void canLowerCamelCaseMultipleWords() {
		assertThat(toLowerCamelCase("four or so Words"), equalTo("fourOrSoWords"));
	}

	@Test
	void treatsDigitOnlyAsChunk() {
		assertThat(toLowerCamelCase("123-project"), equalTo("123Project"));
	}

	@Test
	void treatsMixedDigitAndLetterAsChunks() {
		assertThat(toLowerCamelCase("i18n-admin"), equalTo("i18nAdmin"));
	}

	@Test
	void ignoresTailingSeparators() {
		assertThat(toLowerCamelCase("trailing-"), equalTo("trailing"));
	}

	@Test
	void treatsEachUpperCaseLetterAsSeparateChunks() {
		assertThat(toLowerCamelCase("ABC"), equalTo("aBC"));
	}

	@ParameterizedTest
	@ValueSource(strings = { ".", "-", " " })
	void ignoresSeparatorsOnlyString(String inputString) {
		assertThat(toLowerCamelCase(inputString), equalTo(""));
	}
}
