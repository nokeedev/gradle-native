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
package dev.nokee.util.internal;

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.sameInstance;

class IdentityUnpackerTests {
	IdentityUnpacker subject = new IdentityUnpacker();

	@Test
	void returnsSameValue() {
		Object target = new Object();
		assertThat(subject.unpack(target), sameInstance(target));
	}

	@Test
	void cannotUnpack() {
		assertThat(subject.canUnpack(new Object()), equalTo(false));
	}
}
