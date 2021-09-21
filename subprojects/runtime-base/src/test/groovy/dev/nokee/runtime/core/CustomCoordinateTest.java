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
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CustomCoordinateTest {
	@Test
	void infersTrivialCoordinateAxisByDefault() {
		assertThat(new MyAxis().getAxis(), equalTo(CoordinateAxis.of(MyAxis.class)));
	}

	@Test
	void infersGetValueImplementationIfClassIsCoordinateValueItself() {
		val subject = new MyAxis();
		assertThat(subject.getValue(), equalTo(subject));
	}

	@Test
	void infersGetValueImplementationIfClassIsCoordinateValueSupertype() {
		val subject = new IAxis() {};
		assertThat(subject.getValue(), equalTo(subject));
	}

	@Test
	void throwsExceptionWithInstructionOnCompletingTheCoordinateImplementation() {
		val subject = new MyCoordinate();
		val ex = assertThrows(UnsupportedOperationException.class, subject::getValue);
		assertThat(ex.getMessage(), equalTo("Please implement Coordinate#getValue() for class dev.nokee.runtime.core.CustomCoordinateTest$MyCoordinate."));
	}

	interface IAxis extends Coordinate<IAxis> {}

	static final class MyAxis implements Coordinate<MyAxis> {}

	static final class MyCoordinate implements Coordinate<MyAxis> {}
}
