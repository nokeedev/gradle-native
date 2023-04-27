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

import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;

import java.util.Map;
import java.util.Optional;

import static dev.nokee.xcode.project.PBXTypeSafety.orEmptyMap;
import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

public final class CodeablePBXBuildFile extends AbstractCodeable implements PBXBuildFile {
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

	public CodeablePBXBuildFile(KeyedObject delegate) {
		super(delegate);
	}

	@Override
	public Optional<FileReference> getFileRef() {
		return getAsOptional(CodingKeys.fileRef);
	}

	@Override
	public Optional<XCSwiftPackageProductDependency> getProductRef() {
		return getAsOptional(CodingKeys.productRef);
	}

	@Override
	public Map<String, ?> getSettings() {
		return orEmptyMap(tryDecode(CodingKeys.settings));
	}

	@Override
	public Builder toBuilder() {
		return new Builder(delegate());
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s fileRef=%s settings=%s", super.toString(), this.getClass().getSimpleName(), getFileRef().orElse(null), getSettings());
	}

	@Override
	public int stableHash() {
		return getFileRef().map(Codeable.class::cast).map(Codeable::stableHash)
			.orElseGet(() -> getProductRef().map(Codeable.class::cast).map(Codeable::stableHash).orElse(0));
	}

	public static CodeablePBXBuildFile newInstance(KeyedObject delegate) {
		return new CodeablePBXBuildFile(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
