/*
 * Copyright 2023 the original author or authors.
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
import dev.nokee.util.internal.DefaultUnpackingBuilder;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class DeferredUtils_UnpackTests {
	Unpacker unpacker = newAlwaysThrowingMock(Unpacker.class);

	@Test
	void canBuildUnpackingExecutable() {
		assertThat(DeferredUtils.unpack(unpacker), equalTo(new DefaultUnpackingBuilder<>(unpacker)));
	}
}
