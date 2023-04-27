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
import dev.nokee.xcode.objects.buildphase.PBXCopyFilesBuildPhase;

import java.util.List;
import java.util.Optional;

import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

public final class CodeablePBXCopyFilesBuildPhase extends AbstractCodeable implements PBXCopyFilesBuildPhase {
	public enum CodingKeys implements CodingKey {
		files,
		name,
		dstPath,
		dstSubfolderSpec,
		;

		@Override
		public String getName() {
			return name();
		}
	}

	public CodeablePBXCopyFilesBuildPhase(KeyedObject delegate) {
		super(delegate);
	}

	@Override
	public List<PBXBuildFile> getFiles() {
		return getOrEmptyList(CodingKeys.files);
	}

	@Override
	public Optional<String> getName() {
		return getAsOptional(CodingKeys.name);
	}

	@Override
	public String getDstPath() {
		return tryDecode(CodingKeys.dstPath);
	}

	@Override
	public SubFolder getDstSubfolderSpec() {
		return tryDecode(CodingKeys.dstSubfolderSpec);
	}

	@Override
	public Builder toBuilder() {
		return new Builder(delegate());
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
	}

	public static CodeablePBXCopyFilesBuildPhase newInstance(KeyedObject delegate) {
		return new CodeablePBXCopyFilesBuildPhase(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
