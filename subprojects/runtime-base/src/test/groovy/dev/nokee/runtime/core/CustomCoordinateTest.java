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
