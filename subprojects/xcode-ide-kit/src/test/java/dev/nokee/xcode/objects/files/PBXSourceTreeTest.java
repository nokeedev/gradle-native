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

import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasToString;

class PBXSourceTreeTest {
	@Test
	void hasGroupSourceTree() {
		assertThat(PBXSourceTree.GROUP, hasToString("<group>"));
	}

	@Test
	void hasAbsoluteSourceTree() {
		assertThat(PBXSourceTree.ABSOLUTE, hasToString("<absolute>"));
	}

	@Test
	void hasBuiltProductDirSourceTree() {
		assertThat(PBXSourceTree.BUILT_PRODUCTS_DIR, hasToString("BUILT_PRODUCTS_DIR"));
	}

	@Test
	void hasSdkRootSourceTree() {
		assertThat(PBXSourceTree.SDKROOT, hasToString("SDKROOT"));
	}

	@Test
	void hasSourceRootSourceTree() {
		assertThat(PBXSourceTree.SOURCE_ROOT, hasToString("SOURCE_ROOT"));
	}

	@Test
	void hasDeveloperDirSourceTree() {
		assertThat(PBXSourceTree.DEVELOPER_DIR, hasToString("DEVELOPER_DIR"));
	}
}
