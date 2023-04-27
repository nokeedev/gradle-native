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

import dev.nokee.xcode.objects.configuration.BuildSettings;
import dev.nokee.xcode.objects.configuration.XCBuildConfiguration;
import dev.nokee.xcode.objects.files.PBXFileReference;

import java.util.Optional;

import static dev.nokee.xcode.project.PBXTypeSafety.orEmpty;
import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

public final class CodeableXCBuildConfiguration extends AbstractCodeable implements XCBuildConfiguration {
	public enum CodingKeys implements CodingKey {
		name,
		buildSettings,
		baseConfigurationReference,
		;

		@Override
		public String getName() {
			return name();
		}
	}

	public CodeableXCBuildConfiguration(KeyedObject delegate) {
		super(delegate);
	}

	@Override
	public String getName() {
		return tryDecode(CodingKeys.name);
	}

	@Override
	public BuildSettings getBuildSettings() {
		return tryDecode(CodingKeys.buildSettings); // TODO: Should return empty BuildSettings if empty
	}

	@Override
	public Optional<PBXFileReference> getBaseConfigurationReference() {
		return orEmpty(tryDecode(CodingKeys.baseConfigurationReference));
	}

	@Override
	public Builder toBuilder() {
		return new Builder(delegate());
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
	}

	@Override
	public int stableHash() {
		return getName().hashCode();
	}

	public static CodeableXCBuildConfiguration newInstance(KeyedObject delegate) {
		return new CodeableXCBuildConfiguration(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
