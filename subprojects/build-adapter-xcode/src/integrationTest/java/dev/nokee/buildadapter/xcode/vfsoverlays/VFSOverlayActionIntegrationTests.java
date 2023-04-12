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
package dev.nokee.buildadapter.xcode.vfsoverlays;

import dev.nokee.VirtualFileSystemOverlays;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.ConfigurableOverlays;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VFSOverlayAction;
import dev.nokee.xcode.XCProjectReference;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static dev.nokee.buildadapter.xcode.testers.VirtualFileSystemOverlayTestUtils.entries;
import static dev.nokee.buildadapter.xcode.testers.VirtualFileSystemOverlayTestUtils.location;
import static dev.nokee.buildadapter.xcode.testers.XCBuildSettingTestUtils.buildSettings;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.endsWith;

@ExtendWith(TestDirectoryExtension.class)
class VFSOverlayActionIntegrationTests {
	@TestDirectory Path testDirectory;
	ConfigurableOverlays property = objectFactory().newInstance(ConfigurableOverlays.class);


	@BeforeEach
	void givenXcodeProject() {
		new VirtualFileSystemOverlays().writeToProject(testDirectory);

		property.configure(new VFSOverlayAction(objectFactory(),
			providerFactory().provider(() -> XCProjectReference.of(testDirectory.resolve("VirtualFileSystemOverlays.xcodeproj")).ofTarget("MyFramework")),
			providerFactory().provider(() -> buildSettings(builder -> builder
				.put("PUBLIC_HEADERS_FOLDER_PATH", "$(TARGET_NAME).framework/Headers")
				.put("TARGET_NAME", "MyFramework")
				.put("SOURCE_ROOT", testDirectory.toString())
				.put("BUILT_PRODUCTS_DIR", testDirectory.resolve("derived-data/Build/Products").toString()))),
			providerFactory().provider(() -> testDirectory.resolve("derived-data"))));
	}

	@Test
	void hasAllPublicHeaders() {
		assertThat(property, contains(allOf(
			named("Build/Products/MyFramework.framework/Headers"),
			entries(contains(allOf(
				named("MyFramework.h"), location(providerOf(endsWith("/MyFramework/MyFramework.h"))),
				named("MyPublicHeader.h"), location(providerOf(endsWith("/MyFramework/MyPublicHeader.h")))
			)))
		)));
	}
}
