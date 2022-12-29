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
import dev.nokee.xcode.project.CodeableVersionRequirementBranch;
import dev.nokee.xcode.project.CodeableVersionRequirementExact;
import dev.nokee.xcode.project.CodeableVersionRequirementRange;
import dev.nokee.xcode.project.CodeableVersionRequirementRevision;
import dev.nokee.xcode.project.CodeableVersionRequirementUpToNextMajorVersion;
import dev.nokee.xcode.project.CodeableVersionRequirementUpToNextMinorVersion;
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.KeyedObject;
import dev.nokee.xcode.project.ValueDecoder;

import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.BRANCH;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.EXACT;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.RANGE;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.REVISION;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.UP_TO_NEXT_MAJOR_VERSION;
import static dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind.UP_TO_NEXT_MINOR_VERSION;

public final class VersionRequirementDecoder<T extends XCRemoteSwiftPackageReference.VersionRequirement & Codeable> implements ValueDecoder<T, KeyedObject> {
	@Override
	public T decode(KeyedObject object, Context context) {
		return Select.newInstance((KeyedObject it) -> it.<XCRemoteSwiftPackageReference.VersionRequirement.Kind>tryDecode(KeyedCoders.VERSION_REQUIREMENT_KIND))
			.forCase(BRANCH, CodeableVersionRequirementBranch::newInstance)
			.forCase(EXACT, CodeableVersionRequirementExact::newInstance)
			.forCase(RANGE, CodeableVersionRequirementRange::newInstance)
			.forCase(REVISION, CodeableVersionRequirementRevision::newInstance)
			.forCase(UP_TO_NEXT_MAJOR_VERSION, CodeableVersionRequirementUpToNextMajorVersion::newInstance)
			.forCase(UP_TO_NEXT_MINOR_VERSION, CodeableVersionRequirementUpToNextMinorVersion::newInstance)
			.select(object);
	}

	@Override
	public CoderType<?> getDecodeType() {
		return CoderType.anyOf(XCRemoteSwiftPackageReference.VersionRequirement.class);
	}
}
