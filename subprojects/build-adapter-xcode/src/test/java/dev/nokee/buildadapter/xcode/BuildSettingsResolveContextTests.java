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
package dev.nokee.buildadapter.xcode;

import com.google.common.jimfs.Jimfs;
import dev.nokee.buildadapter.xcode.internal.plugins.BuildSettingsResolveContext;
import dev.nokee.xcode.XCBuildSettings;
import org.junit.jupiter.api.Test;

import static com.google.common.jimfs.Configuration.unix;
import static dev.nokee.internal.testing.FileSystemMatchers.absolutePath;
import static dev.nokee.buildadapter.xcode.testers.XCBuildSettingTestUtils.buildSettings;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class BuildSettingsResolveContextTests {
	XCBuildSettings buildSettings = buildSettings(it -> it.put("BUILT_PRODUCTS_DIR", "/derived-data/Products") //
		.put("SOURCE_ROOT", "/test/Foo-Project"));
	BuildSettingsResolveContext subject = new BuildSettingsResolveContext(Jimfs.newFileSystem(unix()), buildSettings);

	@Test
	void returnsPathToBuiltProductDir() {
		assertThat(subject.getBuiltProductsDirectory(), absolutePath(equalTo("/derived-data/Products")));
	}

	@Test
	void returnsPathToSourceRoot() {
		assertThat(subject.get("SOURCE_ROOT"), absolutePath(equalTo("/test/Foo-Project")));
	}
}
