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
package dev.nokee.xcode.project;

import dev.nokee.xcode.objects.files.GroupChild;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.files.PBXSourceTree;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

public final class CodeablePBXGroup extends AbstractCodeable implements PBXGroup {
	public enum CodingKeys implements CodingKey {
		children,
		name,
		path,
		sourceTree,
		;

		@Override
		public String getName() {
			return name();
		}
	}

	public CodeablePBXGroup(KeyedObject delegate) {
		super(delegate);
	}

	@Override
	public List<GroupChild> getChildren() {
		return getOrEmptyList(CodingKeys.children);
	}

	@Override
	public Optional<String> getName() {
		return getAsOptional(CodingKeys.name);
	}

	@Override
	public Optional<String> getPath() {
		return getAsOptional(CodingKeys.path);
	}

	@Override
	public PBXSourceTree getSourceTree() {
		return getOrNull(CodingKeys.sourceTree);
	}

	@Override
	public Builder toBuilder() {
		return new Builder(delegate());
	}

	@Override
	public String toString() {
		return String.format(
			"%s isa=%s name=%s path=%s sourceTree=%s children=%s",
			super.toString(),
			this.getClass().getSimpleName(),
			getName().orElse(null),
			getPath().orElse(null),
			getSourceTree(),
			getChildren());
	}

	@Override
	public int stableHash() {
		return Objects.hash(getName().orElse(null));
	}

	public static CodeablePBXGroup newInstance(KeyedObject delegate) {
		return new CodeablePBXGroup(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
