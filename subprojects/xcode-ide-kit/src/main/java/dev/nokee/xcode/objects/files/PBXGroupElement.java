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
package dev.nokee.xcode.objects.files;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import static com.google.common.collect.Streams.stream;

/**
 * A collection of files in Xcode's virtual filesystem hierarchy.
 */
public interface PBXGroupElement extends PBXReference {
	// Unfortunately, we can't determine this at constructor time, because CacheBuilder
	// calls our constructor and it's not easy to pass arguments to it.
//	private SortPolicy sortPolicy;

	// It seems the name or path can be null but not both which is a bit different from PBXFileReference.
	//   Except for the mainGroup which both the name and path is null

	List<GroupChild> getChildren();

	SortPolicy getSortPolicy();

	/**
	 * Method by which group contents will be sorted.
	 */
	enum SortPolicy {
		/**
		 * By name, in default Java sort order.
		 */
		BY_NAME,

		/**
		 * Group contents will not be sorted, and will remain in the
		 * order they were added.
		 */
		UNSORTED;
	}

	@SuppressWarnings("unchecked")
	abstract class Builder<SELF extends Builder<SELF, RESULT>, RESULT extends PBXGroupElement> implements org.apache.commons.lang3.builder.Builder<RESULT> {
		private String name;
		private String path;
		private PBXSourceTree sourceTree = PBXSourceTree.GROUP;
		private final List<GroupChild> children = new ArrayList<>();

		public SELF name(String name) {
			this.name = Objects.requireNonNull(name);
			return (SELF) this;
		}

		public SELF path(String path) {
			this.path = Objects.requireNonNull(path);
			return (SELF) this;
		}

		public SELF child(GroupChild reference) {
			assertValid(reference);
			children.add(Objects.requireNonNull(reference));
			return (SELF) this;
		}

		private void assertValid(GroupChild reference) {
			if (reference instanceof PBXGroupElement && !reference.getName().isPresent() && !reference.getPath().isPresent()) {
				throw new NullPointerException("either 'name' or 'path' must not be null for non-main PBXGroup");
			}
		}

		public SELF sourceTree(PBXSourceTree sourceTree) {
			this.sourceTree = Objects.requireNonNull(sourceTree);
			return (SELF) this;
		}

		public SELF children(Iterable<? extends GroupChild> references) {
			this.children.clear();
			stream(references).map(Objects::requireNonNull).peek(this::assertValid).forEach(this.children::add);
			return (SELF) this;
		}

		@Override
		public RESULT build() {
			return newGroupElement(name, path, sourceTree, children);
		}

		protected abstract RESULT newGroupElement(@Nullable String name, @Nullable String path, @Nullable PBXSourceTree sourceTree, List<GroupChild> children);
	}
}
