/*
 * Copyright 2023 the original author or authors.
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

import dev.nokee.xcode.DefaultXCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSetting;
import dev.nokee.xcode.XCBuildSettingLayer;
import dev.nokee.xcode.XCBuildSettingLiteral;

import java.util.stream.Stream;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static java.util.function.Function.identity;

// Disable code signing, see https://stackoverflow.com/a/39901677/13624023
public final class CodeSigningDisabledBuildSettingLayerBuilder {
	public XCBuildSettingLayer build() {
		return new DefaultXCBuildSettingLayer(Stream.of(new XCBuildSettingLiteral("CODE_SIGN_IDENTITY", ""), new XCBuildSettingLiteral("CODE_SIGNING_REQUIRED", "NO"), new XCBuildSettingLiteral("CODE_SIGN_ENTITLEMENTS", ""), new XCBuildSettingLiteral("CODE_SIGNING_ALLOWED", "NO")).collect(toImmutableMap(XCBuildSetting::getName, identity())));
	}
}
