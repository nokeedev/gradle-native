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
import org.junit.jupiter.api.Test;

import java.io.File;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

final class DerivedDataPathAsBuildSettingsTest {
	XCBuildSettings buildSettings = new DerivedDataPathAsBuildSettings().transform(new File("/my/derived/data/path").toPath());

	@Test
	void hasBuildDir() {
		assertThat(buildSettings.get("BUILD_DIR"),
			equalTo("/my/derived/data/path/Build/Products"));
	}

	@Test
	void hasBuildRoot() {
		assertThat(buildSettings.get("BUILD_ROOT"),
			equalTo("/my/derived/data/path/Build/Products"));
	}

	@Test
	void hasProjectTempDir() {
		assertThat(buildSettings.get("PROJECT_TEMP_DIR"),
			equalTo("/my/derived/data/path/Build/Intermediates.noindex/$(PROJECT_NAME).build"));
	}

	@Test
	void hasObjRoot() {
		assertThat(buildSettings.get("OBJROOT"),
			equalTo("/my/derived/data/path/Build/Intermediates.noindex"));
	}

	@Test
	void hasSymRoot() {
		assertThat(buildSettings.get("SYMROOT"),
			equalTo("/my/derived/data/path/Build/Products"));
	}
}
