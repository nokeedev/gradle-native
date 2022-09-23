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

import dev.nokee.xcode.XCBuildSettings;
import lombok.val;
import org.gradle.api.Transformer;

import java.nio.file.Path;

final class DerivedDataPathAsBuildSettings implements Transformer<XCBuildSettings, Path> {
	@Override
	public XCBuildSettings transform(Path derivedDataPath) {
		val builder = XCBuildSettings.builder();
		builder.put("BUILD_DIR", derivedDataPath.resolve("Build/Products").toString());
		builder.put("BUILD_ROOT", derivedDataPath.resolve("Build/Products").toString());
		builder.put("PROJECT_TEMP_DIR", derivedDataPath.resolve("Build/Intermediates.noindex/$(PROJECT_NAME).build").toString());
		builder.put("OBJROOT", derivedDataPath.resolve("Build/Intermediates.noindex").toString());
		builder.put("SYMROOT", derivedDataPath.resolve("Build/Products").toString());

		// According to xcodebuildsettings.com, it should be $(OBJROOT)/SharedPrecompiledHeaders
		builder.put("SHARED_PRECOMPS_DIR", derivedDataPath.resolve("Build/Intermediates.noindex/PrecompiledHeaders").toString());
		return builder.build();
	}
}
