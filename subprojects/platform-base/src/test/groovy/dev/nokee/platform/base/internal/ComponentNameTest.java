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

import dev.nokee.model.NameTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.platform.base.internal.ComponentName.of;
import static dev.nokee.platform.base.internal.ComponentName.ofMain;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasToString;

class ComponentNameTest implements NameTester {
	@Override
	public Object createSubject(String name) {
		return ComponentName.of(name);
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
