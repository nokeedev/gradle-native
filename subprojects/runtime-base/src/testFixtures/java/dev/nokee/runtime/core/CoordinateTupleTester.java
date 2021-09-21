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

import com.google.common.testing.NullPointerTester;
import lombok.val;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

public interface CoordinateTupleTester<T extends CoordinateTuple> {
	T createSubject();

	@Test
	default void hasCoordinates() {
		// Empty tuple are meaningless, always expect at least one coordinate
		assertThat(createSubject(), iterableWithSize(greaterThan(0)));
	}

	@Test
	default void canAccessEachCoordinates() {
		val subject = createSubject();
		subject.forEach(coordinate -> {
			assertThat(subject.find(coordinate.getAxis()), optionalWithValue(is(coordinate.getValue())));
		});
	}

	@Test
	default void cannotFindUnknownAxis() {
		assertThat(createSubject().find(CoordinateAxis.of(UnknownAxis.class, "unknown-axis")), emptyOptional());
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	default void checkNulls() {
		new NullPointerTester().testAllPublicInstanceMethods(createSubject());
	}

	interface UnknownAxis {}
}
