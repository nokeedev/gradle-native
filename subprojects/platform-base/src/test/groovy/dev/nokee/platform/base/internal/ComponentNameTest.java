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
package dev.nokee.platform.base.internal;

import com.google.common.testing.EqualsTester;
import com.google.common.testing.NullPointerTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.platform.base.internal.ComponentName.of;
import static dev.nokee.platform.base.internal.ComponentName.ofMain;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ComponentNameTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkNulls() {
		new NullPointerTester().testAllPublicStaticMethods(ComponentName.class);
	}

	@Test
	void throwsExceptionWhenNameIsEmpty() {
		assertThrows(IllegalArgumentException.class, () -> of(""));
	}

	@Test
	void canCreateComponentName() {
		assertThat(of("main").get(), equalTo("main"));
		assertThat(of("foo").get(), equalTo("foo"));
		assertThat(of("bar").get(), equalTo("bar"));
		assertThat(of("Foo").get(), equalTo("Foo"));
		assertThat(of("Bar").get(), equalTo("Bar"));
	}

	@Test
	void checkToStrings() {
		assertThat(of("main"), hasToString("main"));
		assertThat(of("foo"), hasToString("foo"));
		assertThat(of("bar"), hasToString("bar"));
		assertThat(of("Foo"), hasToString("Foo"));
		assertThat(of("Bar"), hasToString("Bar"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(of("main"), of("main"), ofMain())
			.addEqualityGroup(of("foo"))
			.addEqualityGroup(of("bar"))
			.testEquals();
	}

	@Test
	void canCreateMainComponentName() {
		assertThat(ofMain().get(), equalTo("main"));
		assertThat(ofMain(), hasToString("main"));
	}

	@Test
	void canCheckMainComponentName() {
		assertThat(ofMain().isMain(), equalTo(true));
		assertThat(of("main").isMain(), equalTo(true));
		assertThat(of("test").isMain(), equalTo(false));
	}
}
