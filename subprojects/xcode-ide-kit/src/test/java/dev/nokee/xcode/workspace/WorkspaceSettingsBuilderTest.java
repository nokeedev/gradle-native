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
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WorkspaceSettingsBuilderTest {
	@Test
	void replacePreviousOptionValueOfSameType() {
		val subject = WorkspaceSettings.builder().put(MyOption.A).put(MyOption.C).put(MyOption.B).build();
		assertThat(subject, contains(MyOption.B));
	}

	@Test
	void canBuildWorkspaceSettingsWithoutOptions() {
		assertThat(WorkspaceSettings.builder().build(), emptyIterable());
	}

	@Test
	void canBuildWorkspaceSettingsWithOptions() {
		val subject = WorkspaceSettings.builder().put(MyOption.A).put(MyOtherOption.D).build();
		assertThat(subject, hasItem(MyOption.A));
		assertThat(subject, hasItem(MyOtherOption.D));
	}

	@Test
	void ordersWorkspaceSettingsOptionsAsAdded() {
		val subject = WorkspaceSettings.builder().put(MyOtherOption.E).put(MyOption.C).build();
		assertThat(subject, contains(MyOtherOption.E, MyOption.C));
	}

	@Test
	void throwsExceptionIfOptionIsNull() {
		assertThrows(NullPointerException.class, () -> WorkspaceSettings.builder().put(null));
	}
}
