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
package dev.nokee.platform.xcode;

import dev.gradleplugins.fixtures.sources.SourceElement;
import dev.gradleplugins.fixtures.sources.SourceFile;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public final class XcodeCustomFileRef extends SourceElement {
	@Override
	public List<SourceFile> getFiles() {
		final InputStream inStream = XcodeCustomFileRef.class.getResourceAsStream("/xcode-custom-file-reference.sample");
		try (final BufferedReader reader = new BufferedReader(new InputStreamReader(inStream))) {
			return reader.lines().map(it -> {
				Path fullPath = Paths.get(it.substring(it.indexOf('/') + 1));
				return sourceFile(Optional.ofNullable(fullPath.getParent()).map(Objects::toString).orElse(""), fullPath.getFileName().toString(), readAll(it));
			}).collect(Collectors.toList());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private static String readAll(String path) {
		try (InputStream inStream = XcodeCustomFileRef.class.getResourceAsStream("/" + path)) {
			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			for (int length; (length = inStream.read(buffer)) != -1; ) {
				result.write(buffer, 0, length);
			}
			// StandardCharsets.UTF_8.name() > JDK 7
			return result.toString(StandardCharsets.UTF_8.name());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void writeToProject(File projectDir) {
		for (SourceFile sourceFile : getFiles()) {
			sourceFile.writeToDirectory(projectDir);
		}
	}

	public void writeToProject(Path projectDir) {
		writeToProject(projectDir.toFile());
	}
}
