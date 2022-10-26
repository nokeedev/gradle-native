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

import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference.VersionRequirement.Kind;
import dev.nokee.xcode.project.Decoder;
import dev.nokee.xcode.project.Encoder;
import dev.nokee.xcode.project.ValueCoder;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class VersionRequirementKindCoder implements ValueCoder<Kind> {
	@Override
	public Kind decode(Decoder context) {
		switch (context.decodeString()) {
			case "branch": return Kind.BRANCH;
			case "exactVersion": return Kind.EXACT;
			case "versionRange": return Kind.RANGE;
			case "revision": return Kind.REVISION;
			case "upToNextMajorVersion": return Kind.UP_TO_NEXT_MAJOR_VERSION;
			case "upToNextMinorVersion": return Kind.UP_TO_NEXT_MINOR_VERSION;
			default: throw new UnsupportedOperationException();
		}
	}

	@Override
	public void encode(Kind value, Encoder context) {
		switch (value) {
			case BRANCH: context.encodeString("branch"); break;
			case EXACT: context.encodeString("exactVersion"); break;
			case RANGE: context.encodeString("versionRange"); break;
			case REVISION: context.encodeString("revision"); break;
			case UP_TO_NEXT_MAJOR_VERSION: context.encodeString("upToNextMajorVersion"); break;
			case UP_TO_NEXT_MINOR_VERSION: context.encodeString("upToNextMinorVersion"); break;
			default: throw new UnsupportedOperationException();
		}
	}
}
