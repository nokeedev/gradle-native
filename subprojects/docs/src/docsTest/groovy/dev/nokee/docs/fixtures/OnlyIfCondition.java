/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.docs.fixtures;

import org.apache.commons.lang3.SystemUtils;

import javax.annotation.Nullable;

public abstract class OnlyIfCondition {

	public abstract boolean canExecute();

	public static OnlyIfCondition of(@Nullable String condition) {
		if (condition == null) {
			return new NoCondition();
		} else if (condition.equals("macos")) {
			return new OnlyIfOperatingSystemIsMacOSCondition();
		}
		throw new IllegalArgumentException(String.format("only-if keyword only supports 'macos', got %s", condition));
	}

	private static class NoCondition extends OnlyIfCondition {
		@Override
		public boolean canExecute() {
			return true;
		}
	}

	private static class OnlyIfOperatingSystemIsMacOSCondition extends OnlyIfCondition {
		@Override
		public boolean canExecute() {
			return SystemUtils.IS_OS_MAC;
		}
	}
}
