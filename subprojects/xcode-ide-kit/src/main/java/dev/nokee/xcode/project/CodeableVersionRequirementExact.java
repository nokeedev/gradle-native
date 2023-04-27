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

public final class CodeableVersionRequirementExact extends AbstractCodeable implements XCRemoteSwiftPackageReference.VersionRequirement.Exact {
	public enum CodingKeys implements CodingKey {
		kind,
		version,
		;


		@Override
		public String getName() {
			return name();
		}
	}

	public CodeableVersionRequirementExact(String version) {
		this(new DefaultKeyedObject.Builder().put(CodingKeys.kind, Kind.EXACT).put(CodingKeys.version, version).build());
	}

	public CodeableVersionRequirementExact(KeyedObject delegate) {
		super(delegate);
	}

	@Override
	public Kind getKind() {
		return getOrNull(CodingKeys.kind);
	}

	public String getVersion() {
		return getOrNull(CodingKeys.version);
	}

	@Override
	public String toString() {
		return "require exact version '" + getVersion() + "'";
	}

	public static CodeableVersionRequirementExact newInstance(KeyedObject delegate) {
		return new CodeableVersionRequirementExact(new RecodeableKeyedObject(delegate, of(CodingKeys.values())));
	}
}
