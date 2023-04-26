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
package dev.nokee.buildadapter.xcode.vfsoverlays;

import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.ConfigurableMapContainer;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.GenerateVirtualFileSystemOverlaysTask.Parameters;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.GenerateVirtualFileSystemOverlaysTask.TaskAction;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VFSOverlaySpec;
import dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay.VirtualFileSystemOverlay;
import dev.nokee.buildadapter.xcode.testers.VirtualFileSystemOverlayGenerateTester;
import net.nokeedev.testing.junit.jupiter.io.TestDirectory;
import net.nokeedev.testing.junit.jupiter.io.TestDirectoryExtension;
import org.gradle.api.Action;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;

import java.nio.file.Path;

import static dev.nokee.internal.testing.util.ProjectTestUtils.objectFactory;

@ExtendWith(TestDirectoryExtension.class)
class GenerateVirtualFileSystemOverlaysTaskActionIntegrationTests implements VirtualFileSystemOverlayGenerateTester {
	@TestDirectory Path testDirectory;
	Parameters parameters = objectFactory().newInstance(Parameters.class);
	TaskAction subject = new TaskAction() {
		@Override
		public Parameters getParameters() {
			return parameters;
		}
	};

	@BeforeEach
	void givenConfiguredParameters() {
		parameters.getOverlays().configure(toOverlays(overlay()));
		parameters.getOutputFile().set(outputFile().toFile());

		subject.execute();
	}

	static Action<ConfigurableMapContainer<VFSOverlaySpec>> toOverlays(Iterable<VirtualFileSystemOverlay.VirtualDirectory> roots) {
		return it -> {
			for (VirtualFileSystemOverlay.VirtualDirectory root : roots) {
				it.create(root.getName(), toEntries(root));
			}
		};
	}

	static Action<VFSOverlaySpec> toEntries(Iterable<VirtualFileSystemOverlay.VirtualDirectory.RemappedEntry> entries) {
		return it -> {
			for (VirtualFileSystemOverlay.VirtualDirectory.RemappedEntry entry : entries) {
				it.getEntries().create(entry.getName(), spec -> spec.getLocation().set(entry.getExternalContents()));
			}
		};
	}

	@Override
	public Path testDirectory() {
		return testDirectory;
	}
}
