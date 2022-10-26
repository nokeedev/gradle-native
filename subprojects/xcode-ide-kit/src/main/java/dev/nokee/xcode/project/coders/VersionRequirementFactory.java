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

import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;
import dev.nokee.xcode.project.Codeable;
import dev.nokee.xcode.project.CodeableObjectFactory;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.KeyedObject;
import dev.nokee.xcode.project.CodeableVersionRequirementBranch;
import dev.nokee.xcode.project.CodeableVersionRequirementExact;
import dev.nokee.xcode.project.CodeableVersionRequirementRange;
import dev.nokee.xcode.project.CodeableVersionRequirementRevision;
import dev.nokee.xcode.project.CodeableVersionRequirementUpToNextMajorVersion;
import dev.nokee.xcode.project.CodeableVersionRequirementUpToNextMinorVersion;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class VersionRequirementFactory<T extends XCRemoteSwiftPackageReference.VersionRequirement & Codeable> implements CodeableObjectFactory<T> {
	@Override
	@SuppressWarnings("unchecked")
	public T create(KeyedObject map) {
		switch (map.<XCRemoteSwiftPackageReference.VersionRequirement.Kind>tryDecode(KeyedCoders.VERSION_REQUIREMENT_KIND)) {
			case BRANCH: return (T) new CodeableVersionRequirementBranch(map);
			case EXACT: return (T) new CodeableVersionRequirementExact(map);
			case RANGE: return (T) new CodeableVersionRequirementRange(map);
			case REVISION: return (T) new CodeableVersionRequirementRevision(map);
			case UP_TO_NEXT_MAJOR_VERSION: return (T) new CodeableVersionRequirementUpToNextMajorVersion(map);
			case UP_TO_NEXT_MINOR_VERSION: return (T) new CodeableVersionRequirementUpToNextMinorVersion(map);
			default: throw new UnsupportedOperationException();
		}
	}
}
