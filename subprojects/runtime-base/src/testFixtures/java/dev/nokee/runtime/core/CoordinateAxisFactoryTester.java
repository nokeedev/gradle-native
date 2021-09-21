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
package dev.nokee.runtime.core;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static dev.nokee.runtime.core.CoordinateAxis.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

public interface CoordinateAxisFactoryTester {
	<T> CoordinateAxis<T> createSubject(Class<T> type);
	<T> CoordinateAxis<T> createSubject(Class<T> type, String name);

	@Test
	default void canCreateAxisFromTypeOnly() {
		val subject = createSubject(TestAxis.class);
		assertAll(
			() -> assertThat(subject.getType(), equalTo(TestAxis.class)),
			() -> assertThat(subject.getName(), notNullValue(String.class))
		);
	}

	@Test
	default void infersAxisNameFromClassName() {
		assertAll(
			() -> assertThat(of(One.class).getName(), equalTo("one")),
			() -> assertThat(of(OneTwo.class).getName(), equalTo("one-two")),
			() -> assertThat(of(OneTwoThree.class).getName(), equalTo("one-two-three"))
		);
	}

	@Test
	default void canCreateAxisFromTypeAndName() {
		val subject = createSubject(TestAxis.class, "test");
		assertAll(
			() -> assertThat(subject.getType(), equalTo(TestAxis.class)),
			() -> assertThat(subject.getName(), equalTo("test"))
		);
	}

	@Test
	default void throwsExceptionWhenNameIsEmpty() {
		createSubject(TestAxis.class, "dummy"); // trigger the assumption

		val ex = assertThrows(IllegalArgumentException.class, () -> createSubject(TestAxis.class, ""));
		assertThat(ex.getMessage(), equalTo("coordinate axis name cannot be empty"));
	}

	@Test
	default void throwsExceptionWhenNameContainsSpaces() {
		createSubject(TestAxis.class, "dummy"); // trigger the assumption

		val ex = assertThrows(IllegalArgumentException.class, () -> createSubject(TestAxis.class, "foo bar"));
		assertThat(ex.getMessage(), equalTo("coordinate axis name cannot contains spaces"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(createSubject(Long.class, "foo"), createSubject(Long.class, "foo"))
			.addEqualityGroup(createSubject(Integer.class, "foo"))
			.addEqualityGroup(createSubject(Long.class, "bar"))
			.addEqualityGroup(createSubject(TestAxis.class), createSubject(TestAxis.class, "test-axis"))
			.testEquals();
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNullTypeOnlyConstruction() throws NoSuchMethodException {
		createSubject(TestAxis.class); // trigger the assumption

		new NullPointerTester()
			.testMethod(this, CoordinateAxisFactoryTester.class.getDeclaredMethod("createSubject", Class.class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNullConstruction() throws NoSuchMethodException {
		createSubject(TestAxis.class, "dummy"); // trigger the assumption

		new NullPointerTester()
			.testMethod(this, CoordinateAxisFactoryTester.class.getDeclaredMethod("createSubject", Class.class, String.class));
	}

	interface One {}
	interface OneTwo {}
	interface OneTwoThree {}
}
