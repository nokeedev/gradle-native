/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.base.internal;

import dev.nokee.runtime.core.Coordinate;
import dev.nokee.runtime.core.CoordinateSpace;
import dev.nokee.runtime.core.CoordinateTuple;
import org.gradle.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

class BuildVariantEmptyCoordinateValueNameTest {
	@Nested
	class FromSingleCoordinateTupleTest {
		private final DefaultBuildVariant subject = DefaultBuildVariant.of(MyAxis.INSTANCE, MyEmptyNamedAxis.EMPTY);

		@Test
		void onlyShowsNonEmptyNamedAxisInAllDimensionsName() {
			assertThat(subject.getAllDimensions(), contains("myAxis"));
		}

		@Test
		void onlyShowsNonEmptyNamedAxisInAmbiguousDimensionName() {
			assertThat(subject.getAmbiguousDimensions(), contains("myAxis"));
		}
	}

	@Nested
	class FromCoordinateSpaceTest {
		private final List<DefaultBuildVariant> subject = DefaultBuildVariant.fromSpace(CoordinateSpace.of(
			CoordinateTuple.of(MyAxis.INSTANCE, MyEmptyNamedAxis.EMPTY), CoordinateTuple.of(MyAxis.INSTANCE, MyEmptyNamedAxis.NON_EMPTY)));

		@Test
		void onlyShowsNonEmptyNamedAxisInAllDimensionsName() {
			assertThat(subject.get(0).getAllDimensions(), contains("myAxis"));
			assertThat(subject.get(1).getAllDimensions(), contains("myAxis", "nonEmpty"));
		}

		@Test
		void onlyShowsNonEmptyNamedAxisInAmbiguousDimensionName() {
			assertThat(subject.get(0).getAmbiguousDimensions(), emptyIterable());
			assertThat(subject.get(1).getAmbiguousDimensions(), contains("nonEmpty"));
		}
	}

	private enum MyAxis implements Named, Coordinate<MyAxis> {
		INSTANCE {
			@Override
			public String getName() {
				return "myAxis";
			}
		}
	}

	private enum MyEmptyNamedAxis implements Named, Coordinate<MyEmptyNamedAxis> {
		EMPTY {
			@Override
			public String getName() {
				return "";
			}
		},
		NON_EMPTY {
			@Override
			public String getName() {
				return "nonEmpty";
			}
		}
	}
}
