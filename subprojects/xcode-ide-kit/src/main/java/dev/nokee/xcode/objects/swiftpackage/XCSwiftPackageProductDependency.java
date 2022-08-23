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
package dev.nokee.xcode.objects.swiftpackage;

import dev.nokee.xcode.objects.PBXContainerItem;

import java.util.Objects;

public final class XCSwiftPackageProductDependency extends PBXContainerItem {
	private final String productName;
	private final XCRemoteSwiftPackageReference packageReference;

	public XCSwiftPackageProductDependency(String productName, XCRemoteSwiftPackageReference packageReference) {
		this.productName = productName;
		this.packageReference = packageReference;
	}

	public String getProductName() {
		return productName;
	}

	public XCRemoteSwiftPackageReference getPackageReference() {
		return packageReference;
	}

	public static Builder builder() {
		return new Builder();
	}

	public static final class Builder {
		private String productName;
		private XCRemoteSwiftPackageReference packageReference;

		public Builder productName(String productName) {
			this.productName = productName;
			return this;
		}

		public Builder packageReference(XCRemoteSwiftPackageReference packageReference) {
			this.packageReference = packageReference;
			return this;
		}

		public XCSwiftPackageProductDependency build() {
			return new XCSwiftPackageProductDependency(Objects.requireNonNull(productName), Objects.requireNonNull(packageReference));
		}
	}
}
