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
package dev.nokee.utils;

import dev.nokee.util.Unpacker;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.function.Predicate;

import static com.google.common.base.Predicates.instanceOf;
import static com.google.common.base.Predicates.not;
import static dev.nokee.utils.DeferredUtils.DEFAULT_FLATTENER;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class DeferredUtils_FlatUnpackBuilderTests {
	@Test
	void createsFlatUnpackBuilderUsingDefaultFlattenerAndUnpacker() {
		assertThat(DeferredUtils.flatUnpack(),
				equalTo(new DeferredUtils.FlatUnpackBuilder<>(DEFAULT_FLATTENER, DeferredUtils.DEFAULT_UNPACKER)));
	}

	@Test
	void createsFlatUnpackBuilderUsingDefaultFlattenerAndCustomUnpacker() {
		Unpacker unpacker = mock(Unpacker.class);
		assertThat(DeferredUtils.flatUnpack(unpacker),
				equalTo(new DeferredUtils.FlatUnpackBuilder<>(DEFAULT_FLATTENER, unpacker)));
	}

	@Nested
	class GivenSubject {
		@Mock DeferredUtils.Flattener flattener;
		@Mock Unpacker unpacker;
		@InjectMocks DeferredUtils.FlatUnpackBuilder<MyType> subject;

		@Test
		void createsFlatUnpackWhileExecutableWithUntilCondition() {
			assertThat(subject.until(MyType.class), equalTo(new DeferredUtils.FlatUnpackWhileExecutable<>(flattener, unpacker, not(instanceOf(MyType.class)))));
		}

		@Test
		void createsFlatUnpackWhileExecutableWithSpecifiedPredicate() {
			@SuppressWarnings("unchecked")
			Predicate<Object> predicate = (Predicate<Object>) mock(Predicate.class);
			assertThat(subject.whileTrue(predicate), equalTo(new DeferredUtils.FlatUnpackWhileExecutable<>(flattener, unpacker, predicate)));
		}
	}

	interface MyType {}
}
