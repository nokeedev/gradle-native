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

import com.google.common.collect.ImmutableSet;
import dev.nokee.xcode.objects.files.GroupChild;
import dev.nokee.xcode.objects.files.PBXGroup;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static dev.nokee.xcode.project.PBXTypeSafety.orEmpty;
import static dev.nokee.xcode.project.PBXTypeSafety.orEmptyList;

@EqualsAndHashCode
public final class CodeablePBXGroup implements PBXGroup, Codeable {
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
	private final KeyedObject delegate;

	public CodeablePBXGroup(KeyedObject delegate) {
		this.delegate = delegate;
	}

	@Override
	public List<GroupChild> getChildren() {
		return orEmptyList(delegate.tryDecode(CodingKeys.children));
	}

	@Override
	public Optional<String> getName() {
		return orEmpty(delegate.tryDecode(CodingKeys.name));
	}

	@Override
	public Optional<String> getPath() {
		return orEmpty(delegate.tryDecode(CodingKeys.path));
	}

	@Override
	public PBXSourceTree getSourceTree() {
		return delegate.tryDecode(CodingKeys.sourceTree);
	}

	@Override
	public Builder toBuilder() {
		return new Builder(delegate);
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
	public String isa() {
		return delegate.isa();
	}

	@Nullable
	@Override
	public String globalId() {
		return delegate.globalId();
	}

	@Override
	public void encode(EncodeContext context) {
		delegate.encode(context);
	}

	@Override
	public int stableHash() {
		return Objects.hash(getName().orElse(null));
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		return delegate.tryDecode(key);
	}

	public static CodeablePBXGroup newInstance(KeyedObject delegate) {
		return new CodeablePBXGroup(new RecodeableKeyedObject(delegate, ImmutableSet.copyOf(CodingKeys.values())));
	}
}
