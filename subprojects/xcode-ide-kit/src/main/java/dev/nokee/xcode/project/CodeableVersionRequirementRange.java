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

import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;

import static dev.nokee.xcode.project.RecodeableKeyedObject.of;

public final class CodeableVersionRequirementRange extends AbstractCodeable implements XCRemoteSwiftPackageReference.VersionRequirement.Range {
	public enum CodingKeys implements CodingKey {
		kind,
		minimumVersion,
		maximumVersion,
		;

		@Override
		public String getName() {
			return name();
		}
	}

	public CodeableVersionRequirementRange(String minimumVersion, String maximumVersion) {
		this(new DefaultKeyedObject.Builder().knownKeys(CodingKeys.values())
			.put(CodingKeys.kind, Kind.RANGE)
			.put(CodingKeys.minimumVersion, minimumVersion).put(CodingKeys.maximumVersion, maximumVersion).build());
	}

	public CodeableVersionRequirementRange(KeyedObject delegate) {
		super(delegate);
	}

	@Override
	public Kind getKind() {
		return getOrNull(CodingKeys.kind);
	}

	@Override
	public String getMinimumVersion() {
		return getOrNull(CodingKeys.minimumVersion);
	}

	@Override
	public String getMaximumVersion() {
		return getOrNull(CodingKeys.maximumVersion);
	}

	@Override
	public String toString() {
		return "require range version from '" + getMinimumVersion() + "' to '" + getMaximumVersion() + "'";
	}

	public static CodeableVersionRequirementRange newInstance(KeyedObject delegate) {
		return new CodeableVersionRequirementRange(new RecodeableKeyedObject(delegate, of(CodingKeys.values())));
	}
}
