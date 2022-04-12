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
package dev.nokee.xcode.objects.targets;

import javax.annotation.Nullable;
import java.util.Optional;

public final class ProductType {
	private static final ProductType NONE_PRODUCT_TYPE = new ProductType("", null);
	private final String identifier;
	@Nullable private final String fileExtension;

	private ProductType(String identifier, @Nullable String fileExtension) {
		this.identifier = identifier;
		this.fileExtension = fileExtension;
	}

	/**
	 * Returns the product type identifier.
	 *
	 * @return the identifier for this product type, never null.
	 */
	public String getIdentifier() {
		return identifier;
	}

	/**
	 * Returns the product type file extension.
	 *
	 * @return the file extension for this product type
	 */
	public Optional<String> getFileExtension() {
		return Optional.ofNullable(fileExtension);
	}

	/**
	 * Creates a product type of the specified identifier.
	 *
	 * @param identifier  an identifier for the product type to create, must not be null
	 * @param fileExtension  file extension for the product type to create, may be null
	 * @return a {@link ProductType} instance, never null
	 */
	public static ProductType of(String identifier, @Nullable String fileExtension) {
		return new ProductType(identifier, fileExtension);
	}

	/**
	 * Returns product type representing no product.
	 *
	 * @return a product type representing no product, never null
	 */
	public static ProductType none() {
		return NONE_PRODUCT_TYPE;
	}

	@Override
	public String toString() {
		return identifier;
	}
}
