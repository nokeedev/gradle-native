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
package dev.nokee.buildadapter.xcode.internal.plugins.vfsoverlay;

import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.MoreObjects.toStringHelper;

// The model is losely based on the unofficial vfsoverlay specification under https://llvm.org/doxygen/VirtualFileSystem_8h_source.html
//   We made this decision with the idea the specification may change which the reader/writer will act as a buffer zone.
//   It will ensure this model is not strictly tied to the specification causing all code depending on the model to be refactored.
//   In the future, we should ensure the model represent *what* the overlay represent and leave the on-disk representation to the IO classes.
public final class VirtualFileSystemOverlay implements Iterable<VirtualFileSystemOverlay.VirtualDirectory>, Serializable {
	private final List<VirtualDirectory> roots;

	public VirtualFileSystemOverlay(List<VirtualDirectory> roots) {
		assert roots != null : "'roots' must not be null";
		this.roots = roots;
	}

	@Override
	public Iterator<VirtualDirectory> iterator() {
		return roots.iterator();
	}

	@Override
	public String toString() {
		return toStringHelper(VirtualFileSystemOverlay.class)
			.add("roots", roots)
			.toString();
	}

	public static final class VirtualDirectory implements Iterable<VirtualDirectory.RemappedEntry>, Serializable {
		private final String name;
		private final List<RemappedEntry> contents;

		public VirtualDirectory(String name, List<RemappedEntry> contents) {
			this.name = name;
			this.contents = contents;
		}

		public String getName() {
			return name;
		}

		@Override
		public Iterator<RemappedEntry> iterator() {
			return contents.iterator();
		}

		@Override
		public String toString() {
			return toStringHelper(VirtualDirectory.class)
				.add("name", name)
				.add("contents", contents)
				.toString();
		}

		public static RemappedEntry file(String path, Path location) {
			// it should create a file remap, for now we only support file remap
			return new RemappedEntry(path, location.toString());
		}

		public static final class RemappedEntry implements Serializable {
			private final String name;
			private final String externalContents;

			public RemappedEntry(String name, String externalContents) {
				this.name = name;
				this.externalContents = externalContents;
			}

			public String getName() {
				return name;
			}

			public String getExternalContents() {
				return externalContents;
			}

			@Override
			public String toString() {
				return toStringHelper(RemappedEntry.class)
					.add("name", name)
					.add("externalContents", externalContents)
					.toString();
			}
		}

		public static Builder from(String path) {
			return new Builder(path);
		}

		public static final class Builder {
			private final List<RemappedEntry> entries = new ArrayList<>();
			private final String name;

			private Builder(String name) {
				this.name = name;
			}

			public Builder remap(RemappedEntry entry) {
				entries.add(entry);
				return this;
			}

			public VirtualDirectory build() {
				return new VirtualDirectory(name, entries);
			}
		}
	}
}
