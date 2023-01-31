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
import dev.nokee.xcode.project.ValueDecoder;

public final class VersionRequirementKindDecoder implements ValueDecoder<XCRemoteSwiftPackageReference.VersionRequirement.Kind, String> {
	@Override
	public XCRemoteSwiftPackageReference.VersionRequirement.Kind decode(String object, Context context) {
		return Select.newInstance()
			.forCase("branch", XCRemoteSwiftPackageReference.VersionRequirement.Kind.BRANCH)
			.forCase("exactVersion", XCRemoteSwiftPackageReference.VersionRequirement.Kind.EXACT)
			.forCase("versionRange", XCRemoteSwiftPackageReference.VersionRequirement.Kind.RANGE)
			.forCase("revision", XCRemoteSwiftPackageReference.VersionRequirement.Kind.REVISION)
			.forCase("upToNextMajorVersion", XCRemoteSwiftPackageReference.VersionRequirement.Kind.UP_TO_NEXT_MAJOR_VERSION)
			.forCase("upToNextMinorVersion", XCRemoteSwiftPackageReference.VersionRequirement.Kind.UP_TO_NEXT_MINOR_VERSION)
			.select(object);
	}

	@Override
	public CoderType<XCRemoteSwiftPackageReference.VersionRequirement.Kind> getDecodeType() {
		return CoderType.of(XCRemoteSwiftPackageReference.VersionRequirement.Kind.class);
	}
}
