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

import dev.nokee.xcode.objects.PBXProject;
import dev.nokee.xcode.project.PBXObjectUnarchiver;
import dev.nokee.xcode.project.PBXProjReader;
import lombok.EqualsAndHashCode;

import java.io.IOException;
import java.io.Serializable;
import java.io.UncheckedIOException;
import java.nio.file.Files;

@EqualsAndHashCode
public final class PBXProjectLoader implements XCLoader<PBXProject, XCProjectReference>, Serializable {
	@Override
	public PBXProject load(XCProjectReference reference) {
		try (final PBXProjReader reader = new PBXProjReader(new AsciiPropertyListReader(Files.newBufferedReader(reference.getLocation().resolve("project.pbxproj"))))) {
			return new PBXObjectUnarchiver().decode(reader.read());
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}  catch (Throwable e) {
			throw new RuntimeException(String.format("Could not load Xcode %s.", this), e);
		}
	}
}
