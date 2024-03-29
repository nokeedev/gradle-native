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

import com.google.common.collect.Streams;
import lombok.val;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.Iterator;
import java.util.stream.Collectors;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.runtime.core.CoordinateTestUtils.xAxis;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
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

	@Test
	void canFlattenCoordinateStream() {
		val x = xAxis().create(1L);
		val subject = CoordinateTuple.of(x, A.a2);
		val flattenCoordinates = Streams.stream(subject).flatMap(Coordinates::flatten).collect(Collectors.toList());
		assertThat(flattenCoordinates, contains(x, B.b2, C.c1));
	}

	@Test
	void canFlattenTupleUsingStream() {
		val x = xAxis().create(2L);
		val subject = CoordinateTuple.of(x, A.a3);
		val flattenCoordinates = Streams.stream(subject).flatMap(Coordinates::flatten).collect(Coordinates.toCoordinateTuple());
		assertThat(flattenCoordinates, contains(x, B.b3, C.c1));
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
