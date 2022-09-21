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
package dev.nokee.xcode;

import com.google.common.collect.ImmutableList;
import dev.nokee.xcode.workspace.XCWorkspaceDataReader;
import lombok.val;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;

import static java.nio.charset.StandardCharsets.UTF_8;

public final class WorkspaceProjectReferencesLoader implements XCLoader<Iterable<XCProjectReference>, XCWorkspaceReference> {
	@Override
	public Iterable<XCProjectReference> load(XCWorkspaceReference reference) {
		val layout = new XCWorkspaceLayout(reference.getLocation());

		try {
			val data = new XCWorkspaceDataReader(Files.newBufferedReader(layout.getContentFile(), UTF_8)).read();
			val resolver = new XCFileReferenceResolver(layout.getBaseDirectory().toFile());
			return data.getFileRefs().stream().map(resolver::resolve).map(File::toPath).map(XCProjectReference::of).collect(ImmutableList.toImmutableList());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}
}
