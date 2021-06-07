package dev.nokee.runtime.core;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.runtime.core.CoordinateTestUtils.xAxis;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

class NestedCoordinateSpaceTest {
	@Test
	void canFindNestedAxisValuesUsingCoordinateAlgorithms() {
		val subject = CoordinateTuple.of(xAxis().create(0L), A.a1);
		assertAll(
			() -> assertThat("can find A-axis", Coordinates.find(subject, aAxis, true), optionalWithValue(equalTo(A.a1))),
			() -> assertThat("can find X-axis", Coordinates.find(subject, xAxis(), true), optionalWithValue(equalTo(0L))),
			() -> assertThat("can find nested B-axis", Coordinates.find(subject, bAxis, true), optionalWithValue(equalTo(B.b1))),
			() -> assertThat("can find nested C-axis", Coordinates.find(subject, cAxis, true), optionalWithValue(equalTo(C.c2))),
			() -> assertThat("can find A-axis directly on subject", subject.find(aAxis), optionalWithValue(equalTo(A.a1))),
			() -> assertThat("cannot find nested B-axis directly on subject", subject.find(bAxis), emptyOptional()),
			() -> assertThat("cannot find nested C-axis directly on subject", subject.find(cAxis), emptyOptional())
		);
	}

	private static final CoordinateAxis<A> aAxis = CoordinateAxis.of(A.class);
	private static final CoordinateAxis<B> bAxis = CoordinateAxis.of(B.class);
	private static final CoordinateAxis<C> cAxis = CoordinateAxis.of(C.class);
	enum A implements CoordinateTuple, Coordinate<A> {
		a1(B.b1, C.c2), a2(B.b2, C.c1), a3(B.b3, C.c1);

		private final B b;
		private final C c;

		A(B b, C c) {
			this.b = b;
			this.c = c;
		}

		@Override
		public Iterator<Coordinate<?>> iterator() {
			return Arrays.<Coordinate<?>>asList(b, c).iterator();
		}

		@Override
		public CoordinateAxis<A> getAxis() {
			return aAxis;
		}
	}
	enum B implements Coordinate<B> {
		b1, b2, b3;


		@Override
		public CoordinateAxis<B> getAxis() {
			return bAxis;
		}
	}
	enum C implements Coordinate<C> {
		c1, c2, c3;

		@Override
		public CoordinateAxis<C> getAxis() {
			return cAxis;
		}
	}
}
