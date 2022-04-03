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
package dev.nokee.xcode.workspace;

import dev.nokee.xcode.workspace.WorkspaceSettingsTestOptions.MyOption;
import dev.nokee.xcode.workspace.WorkspaceSettingsTestOptions.MyOtherOption;
import dev.nokee.xcode.workspace.WorkspaceSettingsTestOptions.WrongOption;
import org.junit.jupiter.api.Test;

import static com.spotify.hamcrest.optional.OptionalMatchers.emptyOptional;
import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkspaceSettingsTest {
	private final WorkspaceSettings subject = WorkspaceSettings.builder().put(MyOption.A).put(MyOtherOption.E).build();

	@Test
	void returnsOptionalWithOptionValueForKnownOptions() {
		assertThat(subject.get(MyOption.class), optionalWithValue(equalTo("A")));
		assertThat(subject.get(MyOtherOption.class), optionalWithValue(equalTo("E")));
	}

	@Test
	void returnsEmptyOptionalForUnknownOptions() {
		assertThat(subject.get(WrongOption.class), emptyOptional());
	}

	@Test
	void throwsExceptionWhenGetOptionWithNullKey() {
		assertThrows(NullPointerException.class, () -> subject.get(null));
	}
}
