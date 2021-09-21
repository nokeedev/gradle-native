/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.model.internal.registry;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.junit.jupiter.api.Test;
import spock.lang.Subject;

import javax.inject.Inject;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.model.internal.type.ModelType.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static org.junit.jupiter.api.Assertions.*;

@Subject(ManagedModelProjection.class)
class ManagedModelProjectionTest extends TypeCompatibilityModelProjectionSupportTest {
	@Test
	void managedProjectionCannotBeUsedAsIs() {
		val projection = createSubject(MyType.class);
		assertThrows(UnsupportedOperationException.class, () -> projection.get(of(MyType.class)));
	}

	@Test
	void canBindProjectionToAnInstantiator() {
		assertAll(() -> {
			val projection = createSubject(MyType.class).bind(objectFactory()::newInstance);
			assertTrue(projection.canBeViewedAs(of(MyType.class)));
			assertFalse(projection.canBeViewedAs(of(WrongType.class)));

			assertThat(projection.get(of(MyType.class)), isA(MyType.class));
		});
	}

	@Test
	void canCreateProjectionWithSpecifiedParametersAfterBind() {
		val projection = createSubject(MyTypeWithParameters.class, "foo", 42).bind(objectFactory()::newInstance);
		val instance = projection.get(of(MyTypeWithParameters.class));
		assertThat(instance.name, equalTo("foo"));
		assertThat(instance.answer, equalTo(42));
	}

	static class MyTypeWithParameters implements MyType {
		private final String name;
		private final int answer;

		@Inject
		public MyTypeWithParameters(String name, int answer) {
			this.name = name;
			this.answer = answer;
		}
	}

	@Override
	protected ManagedModelProjection<?> createSubject(Class<?> type) {
		return new ManagedModelProjection<>(of(type));
	}

	protected ManagedModelProjection<?> createSubject(Class<?> type, Object... parameters) {
		return new ManagedModelProjection<>(of(type), parameters);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(ManagedModelProjection.class);
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(createSubject(MyType.class), createSubject(MyType.class))
			.addEqualityGroup(createSubject(MyOtherType.class))
			.addEqualityGroup(createSubject(MyTypeWithParameters.class, "foo", 42), createSubject(MyTypeWithParameters.class, "foo", 42))
			.addEqualityGroup(createSubject(MyTypeWithParameters.class, "bar", 24))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(createSubject(MyType.class),
			hasToString("ModelProjections.managed(interface dev.nokee.model.internal.registry.ManagedModelProjectionTest$MyType)"));
		assertThat(createSubject(MyTypeWithParameters.class, "foo", 42),
			hasToString("ModelProjections.managed(class dev.nokee.model.internal.registry.ManagedModelProjectionTest$MyTypeWithParameters, foo, 42)"));
	}

	interface MyType {}
	interface MyOtherType {}
	interface WrongType {}
}
