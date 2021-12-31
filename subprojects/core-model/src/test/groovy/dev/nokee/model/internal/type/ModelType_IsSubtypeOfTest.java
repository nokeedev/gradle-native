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
import static org.junit.jupiter.api.Assertions.assertFalse;

class ModelType_IsSubtypeOfTest {
	@Test
	void canCheckSubtypeCompatibilityJavaType() {
		val myType = of(MyType.class);
		assertAll(
			() -> assertTrue(myType.isSubtypeOf(myType.getRawType()), "same instance should be subtype"),
			() -> assertTrue(of(MyType.class).isSubtypeOf(MyType.class), "same type should be subtype"),
			() -> assertTrue(of(MyType.class).isSubtypeOf(Object.class), "all types are subtype of Object"),
			() -> assertFalse(of(Object.class).isSubtypeOf(MyType.class), "Object is not subtype of any other type"),
			() -> assertFalse(of(MyType.class).isSubtypeOf(String.class), "unrelated types should not be subtype")
		);
	}

	@Test
	void canCheckSubtypeCompatibilityUsingModelType() {
		val myType = of(MyType.class);
		assertAll(
			() -> assertTrue(myType.isSubtypeOf(of(myType.getRawType())), "same instance should be subtype"),
			() -> assertTrue(of(MyType.class).isSubtypeOf(of(MyType.class)), "same type should be subtype"),
			() -> assertTrue(of(MyType.class).isSubtypeOf((Object.class)), "all types are subtype of Object"),
			() -> assertFalse(of(Object.class).isSubtypeOf((MyType.class)), "Object is not subtype of any other type"),
			() -> assertFalse(of(MyType.class).isSubtypeOf((String.class)), "unrelated types should not be subtype")
		);
	}

	private interface MyType {}
}
