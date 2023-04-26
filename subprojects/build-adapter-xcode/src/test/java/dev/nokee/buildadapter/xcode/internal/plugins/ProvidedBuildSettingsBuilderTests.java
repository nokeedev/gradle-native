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

import com.google.common.jimfs.Jimfs;
import dev.nokee.xcode.XCBuildSetting;
import org.hamcrest.Matcher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.nio.file.FileSystem;
import java.nio.file.Path;
import java.util.Map;

import static com.google.common.jimfs.Configuration.unix;
import static dev.nokee.buildadapter.xcode.TestProjectReference.project;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;
import static dev.nokee.internal.testing.util.ProjectTestUtils.providerFactory;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasEntry;
import static org.hamcrest.Matchers.hasKey;
import static org.hamcrest.Matchers.hasToString;
import static org.hamcrest.Matchers.not;

class ProvidedBuildSettingsBuilderTests {
	FileSystem fs = Jimfs.newFileSystem(unix());
	Path testDirectory = fs.getPath("/test");
	ProvidedBuildSettingsBuilder subject = new ProvidedBuildSettingsBuilder(objectFactory())
		.derivedDataPath(providerFactory().provider(() -> testDirectory.resolve("derived-data")))
		.developerDir(providerFactory().provider(() -> testDirectory.resolve("/Applications/Xcode.app/Contents/Developer")))
		.targetReference(providerFactory().provider(() -> project("Foo").inDirectory(testDirectory).ofTarget("MyTarget")))
		.configuration(providerFactory().provider(() -> "Debug"));

	@Test
	void hasObjRoot() {
		assertThat(subject.build().findAll(), hasBuildSetting("OBJROOT", "/test/derived-data/Build/Intermediates.noindex"));
	}

	@Test
	void hasSymRoot() {
		assertThat(subject.build().findAll(), hasBuildSetting("SYMROOT", "/test/derived-data/Build/Products"));
	}

	@Test
	void hasProjectName() {
		assertThat(subject.build().findAll(), hasBuildSetting("PROJECT_NAME", "Foo"));
	}

	@Test
	void hasTargetName() {
		assertThat(subject.build().findAll(), hasBuildSetting("TARGET_NAME", "MyTarget"));
	}

	@Test
	void hasConfiguration() {
		assertThat(subject.build().findAll(), hasBuildSetting("CONFIGURATION", "Debug"));
	}

	@Test
	void hasNoPlatformName() {
		assertThat(subject.build().findAll(), not(hasKey("PLATFORM_NAME")));
	}

	@Test
	void hasDeveloperDir() {
		assertThat(subject.build().findAll(), hasBuildSetting("DEVELOPER_DIR", "/Applications/Xcode.app/Contents/Developer"));
	}

	@Test
	void hasNoSdkRoot() {
		assertThat(subject.build().findAll(), not(hasKey("SDKROOT")));
	}

	@Nested
	class WhenPlatformNameIsMacOsX {
		@BeforeEach
		void givenPlatformName() {
			subject.platformName(providerFactory().provider(() -> "macosx"));
		}

		@Test
		void hasSdkRoot() {
			assertThat(subject.build().findAll(), //
				hasBuildSetting("SDKROOT", "/Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX.sdk"));
		}

		@Test
		void hasPlatformName() {
			assertThat(subject.build().findAll(), hasBuildSetting("PLATFORM_NAME", "macosx"));
		}
	}

	@Nested
	class WhenPlatformNameIsIphone {
		@BeforeEach
		void givenPlatformName() {
			subject.platformName(providerFactory().provider(() -> "iphoneos"));
		}

		@Test
		void hasSdkRoot() {
			assertThat(subject.build().findAll(), //
				hasBuildSetting("SDKROOT", "/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneOS.platform/Developer/SDKs/iPhoneOS.sdk"));
		}

		@Test
		void hasPlatformName() {
			assertThat(subject.build().findAll(), hasBuildSetting("PLATFORM_NAME", "iphoneos"));
		}
	}

	@Nested
	class WhenPlatformNameIsIphonesimulator {
		@BeforeEach
		void givenPlatformName() {
			subject.platformName(providerFactory().provider(() -> "iphonesimulator"));
		}

		@Test
		void hasSdkRoot() {
			assertThat(subject.build().findAll(), //
				hasBuildSetting("SDKROOT", "/Applications/Xcode.app/Contents/Developer/Platforms/iPhoneSimulator.platform/Developer/SDKs/iPhoneSimulator.sdk"));
		}

		@Test
		void hasPlatformName() {
			assertThat(subject.build().findAll(), hasBuildSetting("PLATFORM_NAME", "iphonesimulator"));
		}
	}

	private static Matcher<Map<? extends String, ? extends XCBuildSetting>> hasBuildSetting(String name, String value) {
		return hasEntry(equalTo(name), allOf(named(name), hasToString(value)));
	}
}
