/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.model;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.lang.reflect.InvocationTargetException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public interface NameTester {
	Object createSubject(String name);

	@Test
	default void canConvertNameToStringViaGetMethod() throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {
		val subject = createSubject("xusu");
		val get = subject.getClass().getMethod("get");
		assertEquals("xusu", get.invoke(subject));
	}

	@Test
	default void canConvertNameToString() {
		assertEquals("kura", createSubject("kura").toString());
		assertEquals("kaco", createSubject("kaco").toString());
		assertEquals("regu", createSubject("regu").toString());
	}

	@Test
	default void throwsExceptionWhenNameIsNull() {
		assertThrows(NullPointerException.class, () -> createSubject(null));
	}

	@Test
	default void disallowsCamelCaseName() {
		assertThrows(IllegalArgumentException.class, () -> createSubject("FoxiFuxa"));
	}

	@Test
	default void disallowsEmptyName() {
		assertThrows(IllegalArgumentException.class, () -> createSubject(""));
	}

	@Test
	default void disallowsBlankName() {
		assertThrows(IllegalArgumentException.class, () -> createSubject("  "));
	}

	@Test
	default void disallowsNameThatContainsSpace() {
		assertThrows(IllegalArgumentException.class, () -> createSubject("wire zoda"));
	}

	@ParameterizedTest
	@ValueSource(chars = { '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '.', ':', ';', '<', '=', '>', '?', '@', '[', '\\', ']', '^', '`', '{', '|', '}', '~'})
	default void disallowsNameWithPrintableSpecialCharacters(char specialCharacter) {
		assertThrows(IllegalArgumentException.class, () -> createSubject(specialCharacter + "tuka"));
		assertThrows(IllegalArgumentException.class, () -> createSubject("tu" + specialCharacter + "ka"));
	}

	@ParameterizedTest
	@ValueSource(chars = { 'é', 'à', 'ö', 'î', 'ç'})
	default void disallowsNonAsciiCharacters(char nonAsciiCharacters) {
		assertThrows(IllegalArgumentException.class, () -> createSubject(nonAsciiCharacters + "muti"));
		assertThrows(IllegalArgumentException.class, () -> createSubject("mu" + nonAsciiCharacters + "ti"));
	}

	@Test
	default void allowsKebabCaseName() {
		assertEquals("dika-zuho", createSubject("dika-zuho").toString());
	}

	@Test
	default void allowsSnakeCaseName() {
		assertEquals("tano_dece", createSubject("tano_dece").toString());
	}

	@Test
	default void allowsLowerCamelCaseName() {
		assertEquals("loxeSepe", createSubject("loxeSepe").toString());
	}

	@Test
	default void canStartNameWithAny_A_to_Z_lowerCaseCharacters() {
		for (char ch = 'a'; ch <= 'z'; ++ch) {
			assertEquals(ch + "oqa", createSubject(ch + "oqa").toString());
		}
	}

	@ParameterizedTest
	@ValueSource(chars = { '-', '_' })
	default void disallowNameThatStartsWithCaseSeparatorCharacters(char caseSeparatorCharacters) {
		assertThrows(IllegalArgumentException.class, () -> createSubject(caseSeparatorCharacters + "ihu"));
	}

	@ParameterizedTest
	@ValueSource(chars = { '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' })
	default void disallowNameThatStartsWithDigit(char digitCharacter) {
		assertThrows(IllegalArgumentException.class, () -> createSubject(digitCharacter + "uba"));
	}

	@Test
	default void disallowsNameThatStartsWithAny_A_to_Z_upperCaseCharacters() {
		for (char ch = 'A'; ch <= 'Z'; ++ch) {
			val upperCaseCharacter = ch;
			assertThrows(IllegalArgumentException.class, () -> createSubject(upperCaseCharacter + "oqa"));
		}
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(createSubject("goxo"), createSubject("goxo"))
			.addEqualityGroup(createSubject("daze"))
			.addEqualityGroup(createSubject("mafe"))
			.addEqualityGroup(createSubject("fizoPada"))
			.addEqualityGroup(createSubject("fizo-Pada"))
			.addEqualityGroup(createSubject("fizo_Pada"))
			.testEquals();
	}
}
