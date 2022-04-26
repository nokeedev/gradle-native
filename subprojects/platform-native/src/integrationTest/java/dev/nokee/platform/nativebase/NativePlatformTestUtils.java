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
package dev.nokee.platform.nativebase;

import org.gradle.nativeplatform.platform.NativePlatform;

import static dev.nokee.language.nativebase.internal.NativePlatformFactory.create;
import static dev.nokee.runtime.nativebase.internal.TargetMachines.of;

public final class NativePlatformTestUtils {
	private NativePlatformTestUtils() {}

	public static NativePlatform windowsPlatform() {
		return create(of("windows-x86"));
	}

	public static NativePlatform nixPlatform() {
		return create(of("linux-x86"));
	}

	public static NativePlatform macosPlatform() {
		return create(of("osx-x64"));
	}
}
