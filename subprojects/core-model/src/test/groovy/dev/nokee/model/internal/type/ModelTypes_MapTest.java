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

import org.junit.jupiter.api.Test;

import java.util.Map;

import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelType.untyped;
import static dev.nokee.model.internal.type.ModelTypes.map;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ModelTypes_MapTest {
	@Test
	void returnsMapOfStringAndMyTypeModelType() {
		assertEquals(of(new TypeOf<Map<String, MyType>>() {}), map(of(String.class), of(MyType.class)));
	}

	@Test
	void returnsMapOfObjectAndObjectModelType() {
		assertEquals(of(new TypeOf<Map<Object, Object>>() {}), map(untyped(), untyped()));
	}

	private interface MyType {}
}
