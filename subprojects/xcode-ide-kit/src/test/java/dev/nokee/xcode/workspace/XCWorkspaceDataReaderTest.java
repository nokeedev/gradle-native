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
package dev.nokee.xcode.workspace;

import lombok.val;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.emptyIterable;

class XCWorkspaceDataReaderTest {
	@Test
	void canReadWorkspaceDataWithoutFileReferences() {
		val subject = new XCWorkspaceDataReader(new InputStreamReader(new ByteArrayInputStream(content(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
			"<Workspace",
			"   version = \"1.0\">",
			"</Workspace>"
		)))).read();
		assertThat(subject.getFileRefs(), emptyIterable());
	}

	@Test
	void canReadWorkspaceDataWithGroupFileReferences() {
		val subject = new XCWorkspaceDataReader(new InputStreamReader(new ByteArrayInputStream(content(
			"<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
			"<Workspace",
			"   version = \"1.0\">",
			"   <FileRef location=\"group:g/k.xcodeproj\">",
			"   </FileRef>",
			"</Workspace>"
		)))).read();
		assertThat(subject.getFileRefs(), contains(XCFileReference.of("group:g/k.xcodeproj")));
	}

	private static byte[] content(String... lines) {
		return Arrays.stream(lines).collect(Collectors.joining(System.lineSeparator())).getBytes(StandardCharsets.UTF_8);
	}
}
