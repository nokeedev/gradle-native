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

import dev.nokee.xcode.objects.targets.ProductType;
import dev.nokee.xcode.objects.targets.ProductTypes;
import dev.nokee.xcode.project.ValueDecoder;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class ProductTypeDecoder implements ValueDecoder<ProductType, String> {
	@Override
	public ProductType decode(String identifier, Context context) {
		for (ProductType knownValue : ProductTypes.values()) {
			if (knownValue.getIdentifier().equals(identifier)) {
				return knownValue;
			}
		}
		return ProductType.of(identifier, null);
	}

	@Override
	public CoderType<?> getDecodeType() {
		return CoderType.of(ProductType.class);
	}
}
