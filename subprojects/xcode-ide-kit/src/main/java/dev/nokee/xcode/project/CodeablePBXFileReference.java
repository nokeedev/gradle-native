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

import dev.nokee.xcode.objects.files.PBXFileReference;
import dev.nokee.xcode.objects.files.PBXSourceTree;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.Objects;
import java.util.Optional;

import static dev.nokee.xcode.project.PBXTypeSafety.orEmpty;
import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

@EqualsAndHashCode
public final class CodeablePBXFileReference implements PBXFileReference, Codeable {
	public enum CodingKeys implements CodingKey {
		name,
		path,
		sourceTree,
		explicitFileType,
		lastKnownFileType,
		;

		@Override
		public String getName() {
			return name();
		}
	}

	private final KeyedObject delegate;

	public CodeablePBXFileReference(KeyedObject delegate) {
		this.delegate = delegate;
	}

	@Override
	public Optional<String> getExplicitFileType() {
		return orEmpty(delegate.tryDecode(CodingKeys.explicitFileType));
	}

	@Override
	public Optional<String> getLastKnownFileType() {
		return orEmpty(delegate.tryDecode(CodingKeys.lastKnownFileType));
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
	public String toString() {
		return String.format(
			"%s isa=%s name=%s path=%s sourceTree=%s explicitFileType=%s",
			super.toString(),
			this.getClass().getSimpleName(),
			getName().orElse(null),
			getPath().orElse(null),
			getSourceTree(),
			getExplicitFileType());
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

	public static CodeablePBXFileReference newInstance(KeyedObject delegate) {
		return new CodeablePBXFileReference(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
