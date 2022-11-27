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

import static dev.nokee.utils.TextCaseUtils.toConstant;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class TextCaseUtils_ToConstantTests {
	@Test
	void returnsEmptyStringAsIs() {
		assertThat(toConstant(""), equalTo(""));
	}

	@Test
	void upperCaseSingleWorkInputString() {
		assertThat(toConstant("word"), equalTo("WORD"));
	}

	@ParameterizedTest
	@ValueSource(strings = { "twoWords", "TwoWords", "two-words", "TWO_WORDS", "two.words", "two words", "two Words", "Two Words" })
	void capitalizesAndJoinsMultipleWordsInputString(String inputString) {
		assertThat(toConstant(inputString), equalTo("TWO_WORDS"));
	}

	@Test
	void ignoresExtraWhitespaces() {
		assertThat(toConstant(" Two  \t words\n"), equalTo("TWO_WORDS"));
	}

	@Test
	void canCamelCaseMultipleWords() {
		assertThat(toConstant("four or so Words"), equalTo("FOUR_OR_SO_WORDS"));
	}

	@Test
	void treatsDigitOnlyAsChunk() {
		assertThat(toConstant("123-project"), equalTo("123_PROJECT"));
	}

	@Test
	void treatsDigitFollowedByLettersSegmentAsChunk() {
		assertThat(toConstant("123abc-project"), equalTo("123ABC_PROJECT"));
	}

	@Test
	void otherTests() {
		assertThat(toConstant("a"), equalTo("A"));
		assertThat(toConstant("A"), equalTo("A"));
		assertThat(toConstant("aB"), equalTo("A_B"));
		assertThat(toConstant("ABCThing"), equalTo("ABC_THING"));
		assertThat(toConstant("ABC Thing"), equalTo("ABC_THING"));
	}

	@Test
	void treatsMixedDigitAndLetterAsChunks() {
		assertThat(toConstant("i18n-admin"), equalTo("I18N_ADMIN"));
	}

	@Test
	void ignoresTailingSeparators() {
		assertThat(toConstant("trailing-"), equalTo("TRAILING"));
	}

	@Test
	void treatsEachUpperCaseLetterAsSeparateChunks() {
		assertThat(toConstant("ABC"), equalTo("ABC"));
	}

	@ParameterizedTest
	@ValueSource(strings = { ".", "-", " " })
	void ignoresSeparatorsOnlyString(String inputString) {
		assertThat(toConstant(inputString), equalTo(""));
	}
}
