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

import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

public final class CodeableXCRemoteSwiftPackageReference extends AbstractCodeable implements XCRemoteSwiftPackageReference {
	public enum CodingKeys implements CodingKey {
		repositoryUrl,
		requirement,
		;

		@Override
		public String getName() {
			return name();
		}
	}
	public CodeableXCRemoteSwiftPackageReference(KeyedObject delegate) {
		super(delegate);
	}

	@Override
	public String getRepositoryUrl() {
		return getOrNull(CodingKeys.repositoryUrl);
	}

	@Override
	public VersionRequirement getRequirement() {
		return getOrNull(CodingKeys.requirement);
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
	}

	@Override
	public int stableHash() {
		return getRepositoryUrl().hashCode();
	}

	public static CodeableXCRemoteSwiftPackageReference newInstance(KeyedObject delegate) {
		return new CodeableXCRemoteSwiftPackageReference(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
