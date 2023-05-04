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
package dev.nokee.xcode.project;

import com.google.common.collect.ImmutableSet;
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static com.google.common.collect.ImmutableMap.of;
import static dev.nokee.xcode.objects.buildphase.PBXBuildFile.ofFile;
import static dev.nokee.xcode.project.CodeablePBXBuildFile.CodingKeys.fileRef;
import static dev.nokee.xcode.project.KeyedCoders.ISA;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class CodeablePBXBuildFileFactoryTests {
	@Mock PBXBuildFile.FileReference reference;

	@Test
	void canCreateBuildFileFromFileReference() {
		assertThat(ofFile(reference), equalTo(new CodeablePBXBuildFile(new DefaultKeyedObject(of(ISA, "PBXBuildFile", fileRef, reference), ImmutableSet.<CodingKey>builder().add(CodeablePBXBuildFile.CodingKeys.values()).add(ISA).build()))));
	}
}
