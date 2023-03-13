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
import dev.nokee.xcode.objects.swiftpackage.XCSwiftPackageProductDependency;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.Objects;

import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

@EqualsAndHashCode
public final class CodeableXCSwiftPackageProductDependency implements XCSwiftPackageProductDependency, Codeable {
	public enum CodingKeys implements CodingKey {
		productName,
		packageReference,
		;


		@Override
		public String getName() {
			return name();
		}
	}

	private final KeyedObject delegate;

	public CodeableXCSwiftPackageProductDependency(KeyedObject delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getProductName() {
		return delegate.tryDecode(CodingKeys.productName);
	}

	@Override
	public XCRemoteSwiftPackageReference getPackageReference() {
		return delegate.tryDecode(CodingKeys.packageReference);
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
		return Objects.hash(getProductName(), getPackageReference().getRepositoryUrl());
	}

	@Override
	public <T> T tryDecode(CodingKey key) {
		return delegate.tryDecode(key);
	}

	public static CodeableXCSwiftPackageProductDependency newInstance(KeyedObject delegate) {
		return new CodeableXCSwiftPackageProductDependency(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
