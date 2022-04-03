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

import org.junit.jupiter.api.Test;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class XCWorkspaceDataWriterTest {
	private final ByteArrayOutputStream outStream = new ByteArrayOutputStream();
	private final XCWorkspaceDataWriter subject = new XCWorkspaceDataWriter(new OutputStreamWriter(outStream));

	@Test
	void canWriteWorkspaceDataWithoutFileReferences() {
		subject.write(XCWorkspaceData.builder().build());

		assertThat(output(), equalTo(withXmlHeader("<Workspace version=\"1.0\"></Workspace>")));
	}

	@Test
	void canWriteWorkspaceDataWithGroupFileReference() {
		subject.write(XCWorkspaceData.builder().fileRef(XCFileReference.of("group:a/b.xcodeproj")).build());

		assertThat(output(), equalTo(withXmlHeader("<Workspace version=\"1.0\"><FileRef location=\"group:a/b.xcodeproj\"></FileRef></Workspace>")));
	}

	private String output() {
		return outStream.toString().replaceAll("\n\r?", "\n");
	}

	private static String withXmlHeader(String content) {
		return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + content;
	}
}
