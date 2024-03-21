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

import lombok.val;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

public final class XcodeVersionPropertyListFinder implements XcodeVersionFinder {
	@Override
	public String find(Path developerDir) {
		// version.plist is located side-by-side with Developer directory
		final Path versionPlist = developerDir.getParent().resolve("version.plist");

		// NOTE: We don't use our XMLPropertyListReader because there seems to be a 100 to 600ms performance hit on initialization
		//   We should revisit when we have more time to debug this particular performance issue.
		try (val reader = Files.newBufferedReader(versionPlist)) {
			String line = null;
			boolean shortVersionFound = false;
			while ((line = reader.readLine()) != null) {
				if (shortVersionFound) {
					return line.replace("<string>", "").replace("</string>", "").trim();
				} else if (line.contains("CFBundleShortVersionString")) {
					shortVersionFound = true;
				}
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		throw new UnsupportedOperationException();
	}
}
