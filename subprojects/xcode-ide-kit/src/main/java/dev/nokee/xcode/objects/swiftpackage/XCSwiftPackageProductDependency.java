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
import dev.nokee.xcode.project.KeyedCoders;
import dev.nokee.xcode.project.DefaultKeyedObject;
import dev.nokee.xcode.project.CodeableXCSwiftPackageProductDependency;

import static java.util.Objects.requireNonNull;

public interface XCSwiftPackageProductDependency extends PBXContainerItem {
	String getProductName();

	XCRemoteSwiftPackageReference getPackageReference();

	static Builder builder() {
		return new Builder();
	}

	final class Builder {
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
			final DefaultKeyedObject.Builder builder = new DefaultKeyedObject.Builder();
			builder.put(KeyedCoders.ISA, "XCSwiftPackageProductDependency");
			builder.put(CodeableXCSwiftPackageProductDependency.CodingKeys.productName, requireNonNull(productName, "'productName' must not be null"));
			builder.put(CodeableXCSwiftPackageProductDependency.CodingKeys.packageReference, requireNonNull(packageReference, "'packageReference' must not be null"));

			return new CodeableXCSwiftPackageProductDependency(builder.build());
		}
	}
}
