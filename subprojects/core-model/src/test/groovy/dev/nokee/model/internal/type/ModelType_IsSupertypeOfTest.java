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
package dev.nokee.model.internal.type;

import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.junit.jupiter.api.Assertions.*;

class ModelType_IsSupertypeOfTest {
	@Test
	void sameTypeInstanceIsSupertypeOfItself() {
		val myType = of(MyType.class);
		assertAll(
			() -> assertTrue(myType.isSupertypeOf(myType.getRawType())),
			() -> assertTrue(myType.isSupertypeOf(myType.getType())),
			() -> assertTrue(myType.isSupertypeOf(myType))
		);
	}

	@Test
	void sameTypeIsSupertypeOfItself() {
		assertAll(
			() -> assertTrue(of(MyType.class).isSupertypeOf(MyType.class)),
			() -> assertTrue(of(MyType.class).isSupertypeOf(of(MyType.class)))
		);
	}

	@Test
	void objectIsSupertypeOfAllTypes() {
		assertAll(
			() -> assertTrue(of(Object.class).isSupertypeOf(MyType.class)),
			() -> assertTrue(of(Object.class).isSupertypeOf(of(MyType.class))),
			() -> assertTrue(of(Object.class).isSupertypeOf(String.class))
		);
	}

	@Test
	void allTypesAreNotSupertypeOfObject() {
		assertAll(
			() -> assertFalse(of(MyType.class).isSupertypeOf(Object.class)),
			() -> assertFalse(of(MyType.class).isSupertypeOf(of(Object.class))),
			() -> assertFalse(of(String.class).isSupertypeOf(Object.class))
		);
	}

	@Test
	void unrelatedTypesAreNotSupertype() {
		assertAll(
			() -> assertFalse(of(MyType.class).isSupertypeOf(String.class)),
			() -> assertFalse(of(MyType.class).isSupertypeOf(of(String.class))),
			() -> assertFalse(of(String.class).isSupertypeOf(Number.class))
		);
	}

	private interface MyType {}
}
