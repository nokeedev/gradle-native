/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.model.internal.type;

import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;

class ModelType_ToStringTest {
	@Test
	void checkToStringForInterfaceType() {
		assertThat(of(MyInterfaceType.class), hasToString("interface dev.nokee.model.internal.type.ModelType_ToStringTest$MyInterfaceType"));
	}

	@Test
	void checkToStringForClassType() {
		assertThat(of(MyClassType.class), hasToString("class dev.nokee.model.internal.type.ModelType_ToStringTest$MyClassType"));
	}

	@Test
	void checkToStringForGenericInterfaceType() {
		assertThat(of(new TypeOf<GenericList<String>>() {}),
			hasToString("interface dev.nokee.model.internal.type.ModelType_ToStringTest$GenericList<java.lang.String>"));
	}

	@Test
	void checkToStringForGenericClassType() {
		assertThat(of(new TypeOf<BaseList<Integer>>() {}),
			hasToString("class dev.nokee.model.internal.type.ModelType_ToStringTest$BaseList<java.lang.Integer>"));
	}

	interface MyInterfaceType {}
	static class MyClassType {}
	interface GenericList<T> {}
	static class BaseList<T> {}

}
