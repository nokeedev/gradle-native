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
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;

import static dev.nokee.xcode.project.RecodeableKeyedObject.of;

@EqualsAndHashCode
public final class CodeableVersionRequirementRange implements XCRemoteSwiftPackageReference.VersionRequirement.Range, Codeable {
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

	private final KeyedObject delegate;

	public CodeableVersionRequirementRange(String minimumVersion, String maximumVersion) {
		this(new DefaultKeyedObject.Builder().put(CodingKeys.kind, Kind.RANGE).put(CodingKeys.minimumVersion, minimumVersion).put(CodingKeys.maximumVersion, maximumVersion).build());
	}

	public CodeableVersionRequirementRange(KeyedObject delegate) {
		this.delegate = delegate;
	}

	@Override
	public Kind getKind() {
		return delegate.tryDecode(CodingKeys.kind);
	}

	public String getMinimumVersion() {
		return delegate.tryDecode(CodingKeys.minimumVersion);
	}

	public String getMaximumVersion() {
		return delegate.tryDecode(CodingKeys.maximumVersion);
	}

	@Override
	public String isa() {
		return delegate.isa();
	}

	@Nullable
	@Override
	public String globalId() {
		return delegate.globalId();
	}

	@Override
	public long age() {
		return delegate.age();
	}

	@Override
	public void encode(Codeable.EncodeContext context) {
		delegate.encode(context);
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		return delegate.tryDecode(key);
	}

	@Override
	public String toString() {
		return "require range version from '" + getMinimumVersion() + "' to '" + getMaximumVersion() + "'";
	}

	public static CodeableVersionRequirementRange newInstance(KeyedObject delegate) {
		return new CodeableVersionRequirementRange(new RecodeableKeyedObject(delegate, of(CodingKeys.values())));
	}
}
