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

import static dev.nokee.utils.TextCaseUtils.toCamelCase;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class TextCaseUtils_ToCamelCaseTests {
	@Test
	void returnsEmptyStringAsIs() {
		assertThat(toCamelCase(""), equalTo(""));
	}

	@Test
	void capitalizeSingleWorkInputString() {
		assertThat(toCamelCase("word"), equalTo("Word"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "twoWords", "TwoWords", "two-words", "two.words", "two words", "two Words", "Two Words" })
	void capitalizesAndJoinsMultipleWordsInputString(String inputString) {
		assertThat(toCamelCase(inputString), equalTo("TwoWords"));
	}

	@Test
	void ignoresExtraWhitespaces() {
		assertThat(toCamelCase(" Two  \t words\n"), equalTo("TwoWords"));
	}

	@Test
	void canCamelCaseMultipleWords() {
		assertThat(toCamelCase("four or so Words"), equalTo("FourOrSoWords"));
	}

	@Test
	void treatsDigitOnlyAsChunk() {
		assertThat(toCamelCase("123-project"), equalTo("123Project"));
	}

	@Test
	void treatsMixedDigitAndLetterAsChunks() {
		assertThat(toCamelCase("i18n-admin"), equalTo("I18nAdmin"));
	}

	@Test
	void ignoresTailingSeparators() {
		assertThat(toCamelCase("trailing-"), equalTo("Trailing"));
	}

	@Test
	void treatsEachUpperCaseLetterAsSeparateChunks() {
		assertThat(toCamelCase("ABC"), equalTo("ABC"));
	}

	@ParameterizedTest
	@ValueSource(strings = { ".", "-", " " })
	void ignoresSeparatorsOnlyString(String inputString) {
		assertThat(toCamelCase(inputString), equalTo(""));
	}
}
