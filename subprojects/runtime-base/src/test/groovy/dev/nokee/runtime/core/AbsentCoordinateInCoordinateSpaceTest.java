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

import java.util.Arrays;
import java.util.Iterator;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.runtime.core.CoordinateTestUtils.xAxis;
import static dev.nokee.runtime.core.CoordinateTestUtils.yAxis;
import static dev.nokee.runtime.core.Coordinates.absentCoordinate;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class AbsentCoordinateInCoordinateSpaceTest {
	@Test
	void ignoresAbsentCoordinateInTuple() {
		val subject = CoordinateTuple.of(absentCoordinate(xAxis()), yAxis().create(3L));
		assertThat("cannot find absent X-axis value", subject.find(xAxis()), emptyOptional());
	}

	@Test
	void ignoresAbsentCoordinateInNestedTuple() {
		val subject = CoordinateTuple.of(A.a);
		assertThat("can find A-axis value", Coordinates.find(subject, aAxis, true), optionalWithValue(equalTo(A.a)));
		assertThat("can find B-axis value", Coordinates.find(subject, bAxis, true), optionalWithValue(equalTo(B.b)));
		assertThat("cannot find absent C-axis value", Coordinates.find(subject, cAxis, true), emptyOptional());
	}

	private static final CoordinateAxis<A> aAxis = CoordinateAxis.of(A.class);
	private static final CoordinateAxis<B> bAxis = CoordinateAxis.of(B.class);
	private static final CoordinateAxis<C> cAxis = CoordinateAxis.of(C.class);
	enum A implements CoordinateTuple, Coordinate<A> {
		a(B.b, absentCoordinate(cAxis));

		private final Coordinate<B> b;
		private final Coordinate<C> c;

		A(Coordinate<B> b, Coordinate<C> c) {
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
		b;

		@Override
		public CoordinateAxis<B> getAxis() {
			return bAxis;
		}
	}
	enum C implements Coordinate<C> {
		c;

		@Override
		public CoordinateAxis<C> getAxis() {
			return cAxis;
		}
	}
}
