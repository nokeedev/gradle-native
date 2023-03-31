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

import com.google.common.collect.ImmutableSet;
import dev.nokee.xcode.objects.PBXProject;
import lombok.val;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static org.apache.commons.io.FilenameUtils.removeExtension;

public final class XCProjectLoader implements XCLoader<XCProject, XCProjectReference> {
	private final XCLoader<PBXProject, XCProjectReference> pbxLoader;
	private final XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> fileReferencesLoader;
	private final XCLoader<Set<XCTargetReference>, XCProjectReference> targetReferencesLoader;

	public XCProjectLoader(XCLoader<PBXProject, XCProjectReference> pbxLoader, XCLoader<XCFileReferencesLoader.XCFileReferences, XCProjectReference> fileReferencesLoader, XCLoader<Set<XCTargetReference>, XCProjectReference> targetReferencesLoader) {
		this.pbxLoader = pbxLoader;
		this.fileReferencesLoader = fileReferencesLoader;
		this.targetReferencesLoader = targetReferencesLoader;
	}

	@Override
	public XCProject load(XCProjectReference reference) {
		val pbxproj = pbxLoader.load(reference);

		val it = reference.getLocation().resolve("xcshareddata/xcschemes");
		val builder = ImmutableSet.<String>builder();
		if (Files.isDirectory(it)) {
			try (final DirectoryStream<Path> xcodeSchemeStream = Files.newDirectoryStream(it, "*.xcscheme")) {
				for (Path xcodeSchemeFile : xcodeSchemeStream) {
					builder.add(removeExtension(xcodeSchemeFile.getFileName().toString()));
				}
			} catch (
				IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		val schemeNames = builder.build();

		// TODO: Add support for implicit scheme: xcodebuild -list -project `getLocation()` -json
		return new DefaultXCProject(reference, schemeNames, pbxproj, fileReferencesLoader, targetReferencesLoader);
	}
}
