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
import java.util.Iterator;
import java.util.List;

public final class VirtualFileSystemOverlay implements Iterable<VirtualFileSystemOverlay.VirtualDirectory>, Serializable {
	private final List<VirtualDirectory> roots;

	public VirtualFileSystemOverlay(List<VirtualDirectory> roots) {
		this.roots = roots;
	}

	@Override
	public Iterator<VirtualDirectory> iterator() {
		return roots.iterator();
	}

	public static final class VirtualDirectory implements Iterable<VirtualDirectory.RemappedEntry>, Serializable {
		private final String from;
		private final List<RemappedEntry> contents;

		public VirtualDirectory(String from, List<RemappedEntry> contents) {
			this.from = from;
			this.contents = contents;
		}

		public String getName() {
			return from;
		}

		@Override
		public Iterator<RemappedEntry> iterator() {
			return contents.iterator();
		}

		public static abstract class RemappedEntry implements Serializable {
			private final Type type;
			private final String name;
			private final String externalContents;


			protected RemappedEntry(Type type, String name, String externalContents) {
				this.type = type;
				this.name = name;
				this.externalContents = externalContents;
			}

			public Type getType() {
				return type;
			}

			public String getName() {
				return name;
			}

			public String getExternalContents() {
				return externalContents;
			}

			public enum Type {
				directory, file
			}
		}

		public static final class RemappedFile extends RemappedEntry {
			public RemappedFile(String name, String externalContents) {
				super(Type.file, name, externalContents);
			}
		}
	}
}
