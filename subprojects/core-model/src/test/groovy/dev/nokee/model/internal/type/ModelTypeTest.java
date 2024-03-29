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

import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.model.internal.type.ModelType.of;
import static org.junit.jupiter.api.Assertions.*;

public class ModelTypeTest {
	@Test
	void canAccessRawType() {
		assertAll(
			() -> assertEquals(String.class, of(String.class).getRawType()),
			() -> assertEquals(Integer.class, of(Integer.class).getRawType()),
			() -> assertEquals(MyType.class, of(MyType.class).getRawType())
		);
	}

	@Test
	void canAccessConcreteType() {
		assertAll(
			() -> assertEquals(String.class, of(String.class).getConcreteType()),
			() -> assertEquals(Integer.class, of(Integer.class).getConcreteType()),
			() -> assertEquals(MyType.class, of(MyType.class).getConcreteType())
		);
	}

	@Test
	void canAccessType() {
		assertAll(
			() -> assertEquals(String.class, of(String.class).getType()),
			() -> assertEquals(Integer.class, of(Integer.class).getType()),
			() -> assertEquals(MyType.class, of(MyType.class).getType())
		);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(ModelType.class);
	}

	@Test
	void canCheckAssignableCompatibility() {
		val myType = of(MyType.class);
		assertAll(
			() -> assertTrue(myType.isAssignableFrom(myType), "same instance should be assignable"),
			() -> assertTrue(of(MyType.class).isAssignableFrom(of(MyType.class)), "same type should be assignable"),
			() -> assertTrue(of(Object.class).isAssignableFrom(of(MyType.class)), "all types are assignable to Object"),
			() -> assertFalse(of(String.class).isAssignableFrom(of(MyType.class)), "unrelated types should not be assignable")
		);
	}

	interface MyType {}
}
