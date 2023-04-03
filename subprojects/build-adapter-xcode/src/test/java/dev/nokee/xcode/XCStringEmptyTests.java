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
package dev.nokee.xcode;

import dev.nokee.internal.testing.SerializableMatchers;
import dev.nokee.xcode.XCStringEmpty;
import dev.nokee.xcode.XCString;
import org.junit.jupiter.api.Test;

import static dev.nokee.internal.testing.SerializableMatchers.isSerializable;
import static dev.nokee.internal.testing.testdoubles.MockitoBuilder.newAlwaysThrowingMock;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.nullValue;

class XCStringEmptyTests {
	XCStringEmpty subject = new XCStringEmpty();

	@Test
	void resolvesToNull() {
		assertThat(subject.resolve(newAlwaysThrowingMock(XCString.ResolveContext.class)), nullValue());
	}

	@Test
	void checkToString() {
		assertThat(subject, hasToString("<<empty>>"));
	}

	@Test
	void canSerialize() {
		assertThat(subject, isSerializable());
	}
}
