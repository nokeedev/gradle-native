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
package dev.nokee.xcode.objects.files;

import com.google.common.testing.EqualsTester;
import org.junit.jupiter.api.Test;

import static dev.nokee.xcode.objects.files.PBXSourceTree.ABSOLUTE;
import static dev.nokee.xcode.objects.files.PBXSourceTree.BUILT_PRODUCTS_DIR;
import static dev.nokee.xcode.objects.files.PBXSourceTree.DEVELOPER_DIR;
import static dev.nokee.xcode.objects.files.PBXSourceTree.GROUP;
import static dev.nokee.xcode.objects.files.PBXSourceTree.SDKROOT;
import static dev.nokee.xcode.objects.files.PBXSourceTree.SOURCE_ROOT;
import static dev.nokee.xcode.objects.files.PBXSourceTree.of;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;

class PBXSourceTreeTest {
	@Test
	void hasGroupSourceTree() {
		assertThat(GROUP, hasToString("<group>"));
	}

	@Test
	void hasAbsoluteSourceTree() {
		assertThat(ABSOLUTE, hasToString("<absolute>"));
	}

	@Test
	void hasBuiltProductDirSourceTree() {
		assertThat(BUILT_PRODUCTS_DIR, hasToString("BUILT_PRODUCTS_DIR"));
	}

	@Test
	void hasSdkRootSourceTree() {
		assertThat(SDKROOT, hasToString("SDKROOT"));
	}

	@Test
	void hasSourceRootSourceTree() {
		assertThat(SOURCE_ROOT, hasToString("SOURCE_ROOT"));
	}

	@Test
	void hasDeveloperDirSourceTree() {
		assertThat(DEVELOPER_DIR, hasToString("DEVELOPER_DIR"));
	}

	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(GROUP, of("<group>"))
			.addEqualityGroup(ABSOLUTE, of("<absolute>"))
			.addEqualityGroup(BUILT_PRODUCTS_DIR, of("BUILT_PRODUCTS_DIR"))
			.addEqualityGroup(SDKROOT, of("SDKROOT"))
			.addEqualityGroup(SOURCE_ROOT, of("SOURCE_ROOT"))
			.addEqualityGroup(DEVELOPER_DIR, of("DEVELOPER_DIR"))
			.addEqualityGroup(of("FOO"), of("FOO"))
			.testEquals();
	}
}
