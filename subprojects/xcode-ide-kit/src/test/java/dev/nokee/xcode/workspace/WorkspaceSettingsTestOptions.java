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

public class WorkspaceSettingsTestOptions {
	public enum MyOption implements WorkspaceSettings.Option<String> {
		A, B, C;

		@Override
		public String get() {
			return name();
		}
	}

	public enum MyOtherOption implements WorkspaceSettings.Option<String> {
		D, E;

		@Override
		public String get() {
			return name();
		}
	}

	public enum BooleanOption implements WorkspaceSettings.Option<Boolean> {
		TRUE, FALSE;

		@Override
		public Boolean get() {
			return this == TRUE;
		}
	}

	public enum StringOption implements WorkspaceSettings.Option<String> {
		Value, OtherValue;

		@Override
		public String get() {
			return name();
		}
	}

	interface WrongOption extends WorkspaceSettings.Option<String> {}
}
