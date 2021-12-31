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

import org.gradle.api.provider.MapProperty;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.type.GradlePropertyTypes.mapProperty;
import static dev.nokee.model.internal.type.ModelType.of;
import static dev.nokee.model.internal.type.ModelType.untyped;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SuppressWarnings("UnstableApiUsage")
class GradlePropertyTypes_MapPropertyTest {
	@Test
	void returnsMapPropertyOfStringAndMyTypeModelType() {
		assertEquals(of(new TypeOf<MapProperty<String, MyType>>() {}), mapProperty(of(String.class), of(MyType.class)));
	}

	@Test
	void returnsMapPropertyOfObjectAndObjectModelType() {
		assertEquals(of(new TypeOf<MapProperty<Object, Object>>() {}), mapProperty(untyped(), untyped()));
	}

	@Test
	void returnsModelTypeForMapPropertyOfStringAndMyTypeInstance() {
		assertEquals(of(new TypeOf<MapProperty<String, MyType>>() {}), GradlePropertyTypes.of(objectFactory().mapProperty(String.class, MyType.class)));
	}

	private interface MyType {}
}
