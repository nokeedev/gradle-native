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

import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Coordinates_OfTest {
	@Test
	void returnsSameInstanceWhenObjectIsAlreadyCoordinateInstance() {
		val subject = new MyCoordinate();
		assertThat(Coordinates.of(subject), is(subject));
	}

	private static final class MyCoordinate implements Coordinate<MyCoordinate> {}

	@Test
	void createsCoordinateUsingCoordinateAxisPublicConstantField() {
		val subject = new BaseValue();
		val coordinate = Coordinates.of(subject);
		assertAll(
			() -> assertThat(coordinate.getAxis(), is(BaseValue.MY_AXIS)),
			() -> assertThat(coordinate.getValue(), is(subject))
		);
	}

	private static class BaseValue {
		public static final CoordinateAxis<BaseValue> MY_AXIS = CoordinateAxis.of(BaseValue.class);
	}

	@Test
	void canCreateCoordinateForNestedImplementation() {
		val subject = new MyValue();
		val coordinate = Coordinates.of(subject);
		assertAll(
			() -> assertThat(coordinate.getAxis(), is(BaseValue.MY_AXIS)),
			() -> assertThat(coordinate.getValue(), is(subject))
		);
	}

	private static class MyValue extends BaseValue {}

	@Test
	void canCreatesCoordinateWhenOnlyOneCoordinateAxisIsPublic() {
		val subject = new MultiValue();
		val coordinate = Coordinates.of(subject);
		assertAll(
			() -> assertThat(coordinate.getAxis(), is(MultiValue.MY_AXIS2)),
			() -> assertThat(coordinate.getValue(), is(subject))
		);
	}

	private static class MultiValue {
		private static final CoordinateAxis<MultiValue> MY_AXIS1 = CoordinateAxis.of(MultiValue.class, "axis-1");
		public static final CoordinateAxis<MultiValue> MY_AXIS2 = CoordinateAxis.of(MultiValue.class, "axis-2");
	}

	@Test
	void throwsExceptionWhenCoordinateAxisIsNotPubliclyAccessible() {
		val subject = new MyNonPublicAxisValue();
		val ex = assertThrows(IllegalArgumentException.class, () -> Coordinates.of(subject));
		assertThat(ex.getMessage(), is("No coordinate axis found in class dev.nokee.runtime.core.Coordinates_OfTest$MyNonPublicAxisValue hierarchy. Verify a CoordinateAxis constant is accessible in class hierarchy or use Coordinate.of(CoordinateAxis, T)."));
	}

	private static final class MyNonPublicAxisValue {
		static final CoordinateAxis<MyNonPublicAxisValue> MY_AXIS = CoordinateAxis.of(MyNonPublicAxisValue.class);
	}

	@Test
	void throwsExceptionWhenCoordinateAxisIsNotStaticAccessible() {
		val subject = new MyNonStaticAxisValue();
		val ex = assertThrows(IllegalArgumentException.class, () -> Coordinates.of(subject));
		assertThat(ex.getMessage(), is("No coordinate axis found in class dev.nokee.runtime.core.Coordinates_OfTest$MyNonStaticAxisValue hierarchy. Verify a CoordinateAxis constant is accessible in class hierarchy or use Coordinate.of(CoordinateAxis, T)."));
	}

	private static final class MyNonStaticAxisValue {
		public final CoordinateAxis<MyNonStaticAxisValue> MY_AXIS = CoordinateAxis.of(MyNonStaticAxisValue.class);
	}

	@Test
	void throwsExceptionWhenCoordinateAxisIsNotFinalAccessible() {
		val subject = new MyNonFinalAxisValue();
		val ex = assertThrows(IllegalArgumentException.class, () -> Coordinates.of(subject));
		assertThat(ex.getMessage(), is("No coordinate axis found in class dev.nokee.runtime.core.Coordinates_OfTest$MyNonFinalAxisValue hierarchy. Verify a CoordinateAxis constant is accessible in class hierarchy or use Coordinate.of(CoordinateAxis, T)."));
	}

	private static final class MyNonFinalAxisValue {
		public static CoordinateAxis<MyNonFinalAxisValue> MY_AXIS = CoordinateAxis.of(MyNonFinalAxisValue.class);
	}

	@Test
	void throwsExceptionWhenCoordinateAxisIsAbsent() {
		val subject = new MyNonAxisValue();
		val ex = assertThrows(IllegalArgumentException.class, () -> Coordinates.of(subject));
		assertThat(ex.getMessage(), is("No coordinate axis found in class dev.nokee.runtime.core.Coordinates_OfTest$MyNonAxisValue hierarchy. Verify a CoordinateAxis constant is accessible in class hierarchy or use Coordinate.of(CoordinateAxis, T)."));
	}

	private static final class MyNonAxisValue {}

	@Test
	void throwsExceptionWhenMultipleCoordinateAxisDeclared() {
		val subject = new MyMultipleAxisValue();
		val ex = assertThrows(IllegalArgumentException.class, () -> Coordinates.of(subject));
		assertThat(ex.getMessage(), is("Multiple coordinate axis found in class dev.nokee.runtime.core.Coordinates_OfTest$MyMultipleAxisValue hierarchy. Please use Coordinate.of(CoordinateAxis, T) instead."));
	}

	private static final class MyMultipleAxisValue {
		public static final CoordinateAxis<MyMultipleAxisValue> MY_AXIS = CoordinateAxis.of(MyMultipleAxisValue.class);
		public static final CoordinateAxis<MyMultipleAxisValue> MY_OTHER_AXIS = CoordinateAxis.of(MyMultipleAxisValue.class);
	}
}
