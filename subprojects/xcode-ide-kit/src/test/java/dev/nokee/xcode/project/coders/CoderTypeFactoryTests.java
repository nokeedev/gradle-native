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
package dev.nokee.xcode.project.coders;

import lombok.EqualsAndHashCode;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class CoderTypeFactoryTests {
	@Test
	void canCreateStringType() {
		assertThat(CoderType.string(), equalTo(new CoderOfType<>(String.class)));
	}

	@Test
	void canCreateIntegerType() {
		assertThat(CoderType.integer(), equalTo(new CoderOfType<>(Integer.class)));
	}

	@Test
	void canCreateListType() {
		assertThat(CoderType.list(new MyType()), equalTo(new CoderListType<>(new MyType())));
	}

	@Test
	void canCreateDictionaryType() {
		assertThat(CoderType.dict(), equalTo(new CoderDictionaryType()));
	}

	@Test
	void canCreateByRefType() {
		assertThat(CoderType.byRef(new MyType()), equalTo(new CoderByRefType<>(new MyType())));
	}

	@Test
	void canCreateByCopyType() {
		assertThat(CoderType.byCopy(new MyType()), equalTo(new CoderByCopyType<>(new MyType())));
	}

	@Test
	void canCreateAnyOfType() {
		assertThat(CoderType.anyOf(MyType.class), equalTo(new CoderAnyOfType<>(MyType.class)));
	}

	@Test
	void canCreateOfType() {
		assertThat(CoderType.of(MyType.class), equalTo(new CoderOfType<>(MyType.class)));
	}

	@Test
	void canCreateYesNoBooleanType() {
		assertThat(CoderType.yesNoBoolean(), equalTo(new CoderYesNoBooleanType()));
	}

	@Test
	void canCreateOneZeroBooleanType() {
		assertThat(CoderType.oneZeroBoolean(), equalTo(new CoderOneZeroBooleanType()));
	}

	@Test
	void canCreateTrueFalseBooleanType() {
		assertThat(CoderType.trueFalseBoolean(), equalTo(new CoderTrueFalseBooleanType()));
	}

	@EqualsAndHashCode(callSuper = false)
	static final class MyType extends CoderType<CoderByCopyTypeTests.MyType> {
		@Override
		public String toString() {
			return "MyType";
		}
	}
}
