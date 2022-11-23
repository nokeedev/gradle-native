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
import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.Optional;

import static dev.nokee.xcode.project.PBXTypeSafety.orEmpty;
import static dev.nokee.xcode.project.PBXTypeSafety.orEmptyMap;

@EqualsAndHashCode
public final class CodeablePBXBuildFile implements PBXBuildFile, Codeable {
	public enum CodingKeys implements CodingKey {
		fileRef,
		productRef,
		settings,
		;

		@Override
		public String getName() {
			return name();
		}
	}

	private final KeyedObject delegate;

	public CodeablePBXBuildFile(KeyedObject delegate) {
		this.delegate = delegate;
	}

	@Override
	public Optional<FileReference> getFileRef() {
		return orEmpty(delegate.tryDecode(CodingKeys.fileRef));
	}

	@Override
	public Optional<XCSwiftPackageProductDependency> getProductRef() {
		return orEmpty(delegate.tryDecode(CodingKeys.productRef));
	}

	@Override
	public Map<String, ?> getSettings() {
		return orEmptyMap(delegate.tryDecode(CodingKeys.settings));
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s fileRef=%s settings=%s", super.toString(), this.getClass().getSimpleName(), getFileRef().orElse(null), getSettings());
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
		return getFileRef().map(Codeable.class::cast).map(Codeable::stableHash)
			.orElseGet(() -> getProductRef().map(Codeable.class::cast).map(Codeable::stableHash).orElse(0));
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		return delegate.tryDecode(key);
	}

	public static CodeablePBXBuildFile newInstance(KeyedObject delegate) {
		return new CodeablePBXBuildFile(new RecodeableKeyedObject(delegate, ImmutableSet.copyOf(CodingKeys.values())));
	}
}
