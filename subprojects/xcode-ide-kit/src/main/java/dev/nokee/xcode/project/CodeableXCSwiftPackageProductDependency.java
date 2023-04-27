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

import java.util.Objects;

import static dev.nokee.xcode.project.RecodeableKeyedObject.ofIsaAnd;

public final class CodeableXCSwiftPackageProductDependency extends AbstractCodeable implements XCSwiftPackageProductDependency {
	public enum CodingKeys implements CodingKey {
		productName,
		packageReference,
		;


		@Override
		public String getName() {
			return name();
		}
	}

	public CodeableXCSwiftPackageProductDependency(KeyedObject delegate) {
		super(delegate);
	}

	@Override
	public String getProductName() {
		return getOrNull(CodingKeys.productName);
	}

	@Override
	public XCRemoteSwiftPackageReference getPackageReference() {
		return getOrNull(CodingKeys.packageReference);
	}

	@Override
	public String toString() {
		return String.format("%s isa=%s", super.toString(), this.getClass().getSimpleName());
	}

	@Override
	public int stableHash() {
		return Objects.hash(getProductName(), getPackageReference().getRepositoryUrl());
	}

	public static CodeableXCSwiftPackageProductDependency newInstance(KeyedObject delegate) {
		return new CodeableXCSwiftPackageProductDependency(new RecodeableKeyedObject(delegate, ofIsaAnd(CodingKeys.values())));
	}
}
