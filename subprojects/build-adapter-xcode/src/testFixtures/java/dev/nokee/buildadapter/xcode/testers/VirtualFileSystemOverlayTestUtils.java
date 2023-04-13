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
package dev.nokee.buildadapter.xcode.testers;

import com.google.common.collect.ImmutableList;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.DefaultWritable;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VFSOverlaySpec;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VirtualFileSystemOverlay;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VirtualFileSystemOverlayReader;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VirtualFileSystemOverlayWriter;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.Writable;
import lombok.val;
import org.gradle.api.provider.Provider;
import org.hamcrest.FeatureMatcher;
import org.hamcrest.Matcher;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static org.hamcrest.Matchers.allOf;

public final class VirtualFileSystemOverlayTestUtils {

	public static VirtualFileSystemOverlay overlayAt(Path path) throws IOException {
		try (val reader = new VirtualFileSystemOverlayReader(Files.newBufferedReader(path))) {
			return reader.read();
		}
	}

	public static Matcher<VirtualFileSystemOverlay.VirtualDirectory.RemappedEntry> remappedFile(String name, Matcher<? super File> matcher) {
		return allOf(named(name), new FeatureMatcher<VirtualFileSystemOverlay.VirtualDirectory.RemappedEntry, File>(matcher, "", "") {
			@Override
			protected File featureValueOf(VirtualFileSystemOverlay.VirtualDirectory.RemappedEntry actual) {
				return Paths.get(actual.getExternalContents()).toFile();
			}
		});
	}

	public static Writable<VirtualFileSystemOverlay> overlayOf(VirtualFileSystemOverlay.VirtualDirectory virtualDirectory) {
		return new DefaultWritable<>(VirtualFileSystemOverlayWriter::new, new VirtualFileSystemOverlay(ImmutableList.of(virtualDirectory)));
	}

	public static Matcher<VFSOverlaySpec> entries(Matcher<? super Iterable<VFSOverlaySpec.EntrySpec>> matcher) {
		return new FeatureMatcher<VFSOverlaySpec, Iterable<VFSOverlaySpec.EntrySpec>>(matcher, "", "") {

			@Override
			protected Iterable<VFSOverlaySpec.EntrySpec> featureValueOf(VFSOverlaySpec actual) {
				return actual.getEntries().getElements().get();
			}
		};
	}

	public static Matcher<VFSOverlaySpec.EntrySpec> location(Matcher<? super Provider<String>> matcher) {
		return new FeatureMatcher<VFSOverlaySpec.EntrySpec, Provider<String>>(matcher, "", "") {
			@Override
			protected Provider<String> featureValueOf(VFSOverlaySpec.EntrySpec actual) {
				return actual.getLocation();
			}
		};
	}
}
