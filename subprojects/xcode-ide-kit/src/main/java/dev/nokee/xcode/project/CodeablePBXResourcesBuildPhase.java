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
import dev.nokee.xcode.objects.buildphase.PBXResourcesBuildPhase;

import java.util.List;

import static dev.nokee.xcode.project.PBXTypeSafety.orEmptyList;
import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

public final class CodeablePBXResourcesBuildPhase extends AbstractCodeable implements PBXResourcesBuildPhase {
	public enum CodingKeys implements CodingKey {
		files,
		;


		@Override
		public String getName() {
			return name();
		}
	}

	public CodeablePBXResourcesBuildPhase(KeyedObject delegate) {
		super(delegate);
	}

	@Override
	public List<PBXBuildFile> getFiles() {
		return orEmptyList(tryDecode(CodingKeys.files));
	}

	@Override
	public Builder toBuilder() {
		return new Builder(delegate());
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
	}

	public static CodeablePBXResourcesBuildPhase newInstance(KeyedObject delegate) {
		return new CodeablePBXResourcesBuildPhase(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
