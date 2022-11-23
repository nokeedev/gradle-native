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

import com.google.common.collect.ImmutableSet;
import dev.nokee.xcode.objects.swiftpackage.XCRemoteSwiftPackageReference;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;

@EqualsAndHashCode
public final class CodeableXCRemoteSwiftPackageReference implements XCRemoteSwiftPackageReference, Codeable {
	public enum CodingKeys implements CodingKey {
		repositoryUrl,
		requirement,
		;

		@Override
		public String getName() {
			return name();
		}
	}
	private final KeyedObject delegate;

	public CodeableXCRemoteSwiftPackageReference(KeyedObject delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getRepositoryUrl() {
		return delegate.tryDecode(CodingKeys.repositoryUrl);
	}

	@Override
	public VersionRequirement getRequirement() {
		return delegate.tryDecode(CodingKeys.requirement);
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
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
	public void encode(EncodeContext context) {
		delegate.encode(context);
	}

	@Override
	public int stableHash() {
		return getRepositoryUrl().hashCode();
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		return delegate.tryDecode(key);
	}

	public static CodeableXCRemoteSwiftPackageReference newInstance(KeyedObject delegate) {
		return new CodeableXCRemoteSwiftPackageReference(new RecodeableKeyedObject(delegate, ImmutableSet.copyOf(CodingKeys.values())));
	}
}
