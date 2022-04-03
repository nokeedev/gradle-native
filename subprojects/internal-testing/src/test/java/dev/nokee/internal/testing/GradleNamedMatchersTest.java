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
package dev.nokee.internal.testing;

import lombok.val;
import org.gradle.api.Named;
import org.hamcrest.Description;
import org.hamcrest.StringDescription;
import org.junit.jupiter.api.Test;

import java.util.function.Consumer;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.internal.testing.util.ProjectTestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasToString;

class GradleNamedMatchersTest {
	@Test
	void givesSensibleErrorMessage() {
		assertThat(description(it -> named("foo").describeMismatch(objectFactory().named(Named.class, "bar"), it)),
			hasToString("the object's name was \"bar\""));
	}

	@Test
	void canCheckNamedObject() {
		assertThat(objectFactory().named(Named.class, "aNamedObject"), named("aNamedObject"));
	}

	@Test
	void canCheckConfigurationObject() {
		assertThat(rootProject().getConfigurations().create("aConfiguration"), named("aConfiguration"));
	}

	@Test
	void canCheckTaskObject() {
		assertThat(rootProject().getTasks().create("aTask"), named("aTask"));
	}

	@Test
	void canCheckNamedUsingMatcher() {
		assertThat(objectFactory().named(Named.class, "somethingSomethingDarkSide"), named(containsString("Dark")));
	}

	@Test
	void canCheckGetNameObject() {
		assertThat(GetNameObject.Instance, named("my-name"));
	}

	private static Description description(Consumer<? super Description> action) {
		val description = new StringDescription();
		action.accept(description);
		return description;
	}

	private enum GetNameObject {
		Instance;

		public String getName() {
			return "my-name";
		}
	}
}
