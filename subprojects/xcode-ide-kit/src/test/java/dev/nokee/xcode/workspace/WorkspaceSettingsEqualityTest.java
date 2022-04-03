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

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.xcode.workspace.WorkspaceSettings.builder;
import static dev.nokee.xcode.workspace.WorkspaceSettingsTestOptions.MyOption.A;
import static dev.nokee.xcode.workspace.WorkspaceSettingsTestOptions.MyOption.B;

class WorkspaceSettingsEqualityTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(builder().build(), builder().build())
			.addEqualityGroup(builder().put(A).build())
			.addEqualityGroup(builder().put(B).build())
			.addEqualityGroup(builder().put(B).put(WorkspaceSettingsTestOptions.MyOtherOption.D).build())
			.testEquals();
	}
}
