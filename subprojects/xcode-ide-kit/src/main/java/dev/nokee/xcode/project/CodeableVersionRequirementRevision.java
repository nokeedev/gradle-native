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

public final class CodeableVersionRequirementRevision extends AbstractCodeable implements XCRemoteSwiftPackageReference.VersionRequirement.Revision {
	public enum CodingKeys implements CodingKey {
		kind,
		revision,
		;

		@Override
		public String getName() {
			return name();
		}
	}

	public CodeableVersionRequirementRevision(String revision) {
		this(new DefaultKeyedObject.Builder().knownKeys(CodingKeys.values())
			.put(CodingKeys.kind, Kind.REVISION)
			.put(CodingKeys.revision, revision).build());
	}

	public CodeableVersionRequirementRevision(KeyedObject delegate) {
		super(delegate);
	}

	@Override
	public Kind getKind() {
		return getOrNull(CodingKeys.kind);
	}

	@Override
	public String getRevision() {
		return getOrNull(CodingKeys.revision);
	}

	@Override
	public String toString() {
		return "require revision '" + getRevision() + "'";
	}

	public static CodeableVersionRequirementRevision newInstance(KeyedObject delegate) {
		return new CodeableVersionRequirementRevision(new RecodeableKeyedObject(delegate, of(CodingKeys.values())));
	}
}
