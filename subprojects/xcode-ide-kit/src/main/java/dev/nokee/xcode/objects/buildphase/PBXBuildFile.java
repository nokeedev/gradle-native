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
package dev.nokee.xcode.objects.buildphase;

import com.google.common.collect.ImmutableMap;
import dev.nokee.xcode.objects.PBXProjectItem;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * File referenced by a build phase, unique to each build phase.
 *
 * Contains a dictionary {@link #settings} which holds additional information to be interpreted by
 * the particular phase referencing this object, e.g.:
 *
 * - {@link PBXSourcesBuildPhase } may read <code>{"COMPILER_FLAGS": "-foo"}</code> and interpret
 * that this file should be compiled with the additional flag {@code "-foo" }.
 */
@EqualsAndHashCode(callSuper = false)
public final class PBXBuildFile extends PBXProjectItem {
	@Nullable private final FileReference fileRef;
	@Nullable private final XCSwiftPackageProductDependency productRef;
	private final ImmutableMap<String, ?> settings;

	private PBXBuildFile(@Nullable FileReference fileRef, @Nullable XCSwiftPackageProductDependency productRef, ImmutableMap<String, ?> settings) {
		this.fileRef = fileRef;
		this.productRef = productRef;
		this.settings = settings;
	}

	public Optional<FileReference> getFileRef() {
		return Optional.ofNullable(fileRef);
	}

	public Optional<XCSwiftPackageProductDependency> getProductRef() {
		return Optional.ofNullable(productRef);
	}

	public Map<String, ?> getSettings() {
		return settings;
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s fileRef=%s settings=%s", super.toString(), this.getClass().getSimpleName(), fileRef, settings);
	}

	public static PBXBuildFile ofFile(FileReference fileReference) {
		return new PBXBuildFile(fileReference, null, ImmutableMap.of());
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private FileReference fileRef;
		private XCSwiftPackageProductDependency productRef;
		private final Map<String, Object> settings = new LinkedHashMap<>();

		public Builder fileRef(FileReference fileRef) {
			this.fileRef = fileRef;
			return this;
		}

		public Builder productRef(XCSwiftPackageProductDependency productRef) {
			this.productRef = productRef;
			return this;
		}

		public Builder settings(Map<String, ?> settings) {
			this.settings.clear();
			this.settings.putAll(settings);
			return this;
		}

		public PBXBuildFile build() {
			if (fileRef == null && productRef == null) {
				throw new NullPointerException("either 'fileRef' and 'productRef' must not be null");
			}
			return new PBXBuildFile(fileRef, productRef, ImmutableMap.copyOf(settings));
		}
	}

	/**
	 * Represent a file reference for a {@link PBXBuildFile}.
	 */
	public interface FileReference {}
}
