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
package dev.nokee.xcode.objects.buildphase;

import com.google.common.testing.EqualsTester;
import dev.nokee.xcode.objects.files.PBXFileReference;
import org.junit.jupiter.api.Test;

import java.io.File;

import static dev.nokee.xcode.objects.buildphase.PBXBuildFile.builder;
import static dev.nokee.xcode.objects.buildphase.PBXBuildFile.ofFile;
import static dev.nokee.xcode.objects.files.PBXFileReference.ofAbsolutePath;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class PBXBuildFileEqualityTest {
	@Test
	@SuppressWarnings("UnstableApiUsage")
	void checkEquals() {
		new EqualsTester()
			.addEqualityGroup(ofFile(ofAbsolutePath(new File("a"))), ofFile(ofAbsolutePath(new File("a"))))
			.addEqualityGroup(ofFile(ofAbsolutePath(new File("b"))))
			.testEquals();
	}

	@Test
	void createsSameBuildFileUsingBuilder() {
		assertThat(ofFile(ofAbsolutePath(new File("a.txt"))),
			equalTo(builder().fileRef(ofAbsolutePath(new File("a.txt"))).build()));
	}
}
