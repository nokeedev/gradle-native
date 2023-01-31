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
package dev.nokee.xcode.project.coders;

import dev.nokee.xcode.objects.buildphase.PBXBuildFile;
import dev.nokee.xcode.objects.buildphase.PBXBuildPhase;
import dev.nokee.xcode.project.Codeable;
import dev.nokee.xcode.project.CodeablePBXCopyFilesBuildPhase;
import dev.nokee.xcode.project.CodeablePBXFrameworksBuildPhase;
import dev.nokee.xcode.project.CodeablePBXHeadersBuildPhase;
import dev.nokee.xcode.project.CodeablePBXResourcesBuildPhase;
import dev.nokee.xcode.project.CodeablePBXShellScriptBuildPhase;
import dev.nokee.xcode.project.CodeablePBXSourcesBuildPhase;
import dev.nokee.xcode.project.KeyedObject;
import dev.nokee.xcode.project.ValueDecoder;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class BuildPhaseDecoder<T extends PBXBuildPhase & Codeable> implements ValueDecoder<T, KeyedObject> {
	@Override
	public T decode(KeyedObject object, Context context) {
		return Select.newInstance(KeyedObject::isa)
			.forCase("PBXCopyFilesBuildPhase", CodeablePBXCopyFilesBuildPhase::newInstance)
			.forCase("PBXFrameworksBuildPhase", CodeablePBXFrameworksBuildPhase::newInstance)
			.forCase("PBXHeadersBuildPhase", CodeablePBXHeadersBuildPhase::newInstance)
			.forCase("PBXResourcesBuildPhase", CodeablePBXResourcesBuildPhase::newInstance)
			.forCase("PBXShellScriptBuildPhase", CodeablePBXShellScriptBuildPhase::newInstance)
			.forCase("PBXSourcesBuildPhase", CodeablePBXSourcesBuildPhase::newInstance)
			.select(object);
	}

	@Override
	public CoderType<PBXBuildPhase> getDecodeType() {
		return CoderType.anyOf(PBXBuildPhase.class);
	}
}
