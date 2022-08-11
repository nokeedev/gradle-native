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
package dev.nokee.buildadapter.xcode.internal.plugins;

import com.google.common.collect.ImmutableList;

import java.util.List;

final class XCBuildSettingsUtils {
	private XCBuildSettingsUtils() {}

	public static List<String> codeSigningDisabled() {
		// Disable code signing, see https://stackoverflow.com/a/39901677/13624023
		return ImmutableList.of("CODE_SIGN_IDENTITY=\"\"", "CODE_SIGNING_REQUIRED=NO", "CODE_SIGN_ENTITLEMENTS=\"\"", "CODE_SIGNING_ALLOWED=NO");
	}
}
