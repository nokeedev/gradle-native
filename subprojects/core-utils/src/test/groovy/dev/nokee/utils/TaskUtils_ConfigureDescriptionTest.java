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
package dev.nokee.utils;

import com.google.common.testing.EqualsTester;
import lombok.val;
import org.gradle.api.Task;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static dev.nokee.utils.TaskUtils.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.isA;

class TaskUtils_ConfigureDescriptionTest {
	@Test
	void canConfigureDescription() {
		val task = Mockito.mock(Task.class);
		configureDescription("Assembles outputs of component '%s'.", ":foo").execute(task);
		Mockito.verify(task).setDescription("Assembles outputs of component ':foo'.");
	}

	@Test
	void returnsEnhanceAction() {
		assertThat(configureDescription("foo"), isA(ActionUtils.Action.class));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(configureDescription("foo"), configureDescription("foo"))
			.addEqualityGroup(configureDescription("Assembles '%s'.", "foo"), configureDescription("Assembles '%s'.", "foo"))
			.addEqualityGroup(configureDescription("bar"))
			.testEquals();
	}

	@Test
	void checkToString() {
		assertThat(configureDescription("foo"), hasToString("TaskUtils.configureDescription(foo)"));
		assertThat(configureDescription("Assembles '%s'.", "foo"), hasToString("TaskUtils.configureDescription(Assembles 'foo'.)"));
	}
}
