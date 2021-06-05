package dev.nokee.runtime.core;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;

import static dev.nokee.runtime.core.CoordinateTestUtils.xAxis;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertAll;

class NestedCoordinateSpaceTest {
	@Test
	void canGetNestedAxisValues() {
		val subject = CoordinateTuple.of(xAxis().create(0L), A.a1);
		assertAll(
			() -> assertThat("can get A-axis", subject.get(aAxis), equalTo(A.a1)),
			() -> assertThat("can get X-axis", subject.get(xAxis()), equalTo(0L)),
			() -> assertThat("can get nested B-axis", subject.get(bAxis), equalTo(B.b1)),
			() -> assertThat("can get nested C-axis", subject.get(cAxis), equalTo(C.c2))
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
