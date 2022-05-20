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
package dev.nokee.xcode.utils;

import dev.nokee.xcode.workspace.XCWorkspaceData;
import dev.nokee.xcode.workspace.XCWorkspaceDataWriter;
import lombok.val;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class XCWorkspaceDataTestUtils {
	public static XCWorkspaceData emptyWorkspaceData() {
		return XCWorkspaceData.builder().build();
	}

	public static void writeTo(XCWorkspaceData self, Path path) throws IOException {
		try (val writer = new XCWorkspaceDataWriter(Files.newBufferedWriter(path))) {
			writer.write(self);
		}
	}
}
