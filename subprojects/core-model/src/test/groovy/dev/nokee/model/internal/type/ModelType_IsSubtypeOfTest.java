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

class ModelType_IsSubtypeOfTest {
	@Test
	void sameTypeInstanceIsSubtypeOfItself() {
		val myType = of(MyType.class);
		assertAll(
			() -> assertTrue(myType.isSubtypeOf(myType.getRawType())),
			() -> assertTrue(myType.isSubtypeOf(myType.getType())),
			() -> assertTrue(myType.isSubtypeOf(myType))
		);
	}

	@Test
	void sameTypeIsSubtypeOfItself() {
		assertAll(
			() -> assertTrue(of(MyType.class).isSubtypeOf(MyType.class)),
			() -> assertTrue(of(MyType.class).isSubtypeOf(of(MyType.class)))
		);
	}

	@Test
	void allTypesAreSubtypeOfObject() {
		assertAll(
			() -> assertTrue(of(MyType.class).isSubtypeOf(Object.class)),
			() -> assertTrue(of(MyType.class).isSubtypeOf(of(Object.class))),
			() -> assertTrue(of(String.class).isSubtypeOf(Object.class))
		);
	}

	@Test
	void objectIsNotSubtypeOfAnyTypes() {
		assertAll(
			() -> assertFalse(of(Object.class).isSubtypeOf(MyType.class)),
			() -> assertFalse(of(Object.class).isSubtypeOf(of(MyType.class))),
			() -> assertFalse(of(Object.class).isSubtypeOf(String.class))
		);
	}

	@Test
	void unrelatedTypesAreNotSubtype() {
		assertAll(
			() -> assertFalse(of(MyType.class).isSubtypeOf(String.class)),
			() -> assertFalse(of(MyType.class).isSubtypeOf(of(String.class))),
			() -> assertFalse(of(String.class).isSubtypeOf(Number.class))
		);
	}

	private interface MyType {}
}
