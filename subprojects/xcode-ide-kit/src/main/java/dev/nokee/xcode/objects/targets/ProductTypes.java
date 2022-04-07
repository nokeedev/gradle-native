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

import java.util.Objects;

public final class ProductTypes {
	// NOTE: These constants cannot live in ProductType, as referencing subclass in static
	// initializer may cause deadlock during classloading.
	public static final ProductType APPLICATION = ProductType.of("com.apple.product-type.application", "app");
	public static final ProductType FRAMEWORK = ProductType.of("com.apple.product-type.framework", "framework");
	public static final ProductType STATIC_FRAMEWORK = ProductType.of("com.apple.product-type.framework.static", "framework");
	public static final ProductType XC_FRAMEWORK = ProductType.of("com.apple.product-type.xcframework", "xcframework");
	public static final ProductType DYNAMIC_LIBRARY = ProductType.of("com.apple.product-type.library.dynamic", "dylib");
	public static final ProductType STATIC_LIBRARY = ProductType.of("com.apple.product-type.library.static", "a");
	public static final ProductType BUNDLE = ProductType.of("com.apple.product-type.bundle", "bundle");
	public static final ProductType UNIT_TEST_BUNDLE = ProductType.of("com.apple.product-type.bundle.unit-test", "xctest");
	public static final ProductType UI_TEST_BUNDLE = ProductType.of("com.apple.product-type.bundle.ui-testing", "xctest");
	public static final ProductType APP_EXTENSION = ProductType.of("com.apple.product-type.app-extension", "appex");
	public static final ProductType COMMAND_LINE_TOOL = ProductType.of("com.apple.product-type.tool", null);
	public static final ProductType WATCH_APP = ProductType.of("com.apple.product-type.application.watchapp", "app");
	public static final ProductType WATCH2_APP = ProductType.of("com.apple.product-type.application.watchapp2", "app");
	public static final ProductType WATCH2_APP_CONTAINER = ProductType.of("com.apple.product-type.application.watchapp2-container", "app");
	public static final ProductType WATCH_EXTENSION = ProductType.of("com.apple.product-type.watchkit-extension", "appex");
	public static final ProductType WATCH2_EXTENSION = ProductType.of("com.apple.product-type.watchkit2-extension", "appex");
	public static final ProductType TV_EXTENSION = ProductType.of("com.apple.product-type.tv-app-extension", "appex");
	public static final ProductType MESSAGES_APPLICATION = ProductType.of("com.apple.product-type.application.messages", "app");
	public static final ProductType MESSAGES_EXTENSION = ProductType.of("com.apple.product-type.app-extension.messages", "appex");
	public static final ProductType STICKER_PACK = ProductType.of("com.apple.product-type.app-extension.messages-sticker-pack", "appex");
	public static final ProductType XPC_SERVICE = ProductType.of("com.apple.product-type.xpc-service", "xpc");
	public static final ProductType OC_UNIT_TEST_BUNDLE = ProductType.of("com.apple.product-type.bundle.ocunit-test", "octest");
	public static final ProductType XCODE_EXTENSION = ProductType.of("com.apple.product-type.xcode-extension", "appex");
	public static final ProductType INSTRUMENTS_PACKAGE = ProductType.of("com.apple.product-type.instruments-package", "instrpkg");
	public static final ProductType INTENTS_SERVICE_EXTENSION = ProductType.of("com.apple.product-type.app-extension.intents-service", "appex");
	public static final ProductType ON_DEMAND_INSTALL_CAPABLE_APPLICATION = ProductType.of("com.apple.product-type.application.on-demand-install-capable", "app");
	public static final ProductType METAL_LIBRARY = ProductType.of("com.apple.product-type.metal-library", "metallib");
	public static final ProductType DRIVER_EXTENSION = ProductType.of("com.apple.product-type.driver-extension", "dext");
	public static final ProductType SYSTEM_EXTENSION = ProductType.of("com.apple.product-type.system-extension", "systemextension");

	private static final ProductType[] KNOWN_VALUES = new ProductType[] {
		APPLICATION, FRAMEWORK, STATIC_FRAMEWORK, XC_FRAMEWORK, DYNAMIC_LIBRARY, STATIC_LIBRARY, BUNDLE,
		UNIT_TEST_BUNDLE, UI_TEST_BUNDLE, APP_EXTENSION, COMMAND_LINE_TOOL, WATCH_APP, WATCH2_APP,
		WATCH2_APP_CONTAINER, WATCH_EXTENSION, WATCH2_EXTENSION, TV_EXTENSION, MESSAGES_APPLICATION,
		MESSAGES_EXTENSION, STICKER_PACK, XPC_SERVICE, OC_UNIT_TEST_BUNDLE, XCODE_EXTENSION, INSTRUMENTS_PACKAGE,
		INTENTS_SERVICE_EXTENSION, ON_DEMAND_INSTALL_CAPABLE_APPLICATION, METAL_LIBRARY, DRIVER_EXTENSION,
		SYSTEM_EXTENSION
	};

	private ProductTypes() {}

	/**
	 * Creates a product type of the specified identifier.
	 *
	 * @param identifier  an identifier for the product type to create, must not be null
	 * @return the {@link ProductType} instance for the specified identifier, never null.
	 */
	public static ProductType valueOf(String identifier) {
		Objects.requireNonNull(identifier);
		for (ProductType knownValue : KNOWN_VALUES) {
			if (knownValue.getIdentifier().equals(identifier)) {
				return knownValue;
			}
		}
		throw new IllegalArgumentException(String.format("identifier '%s' is not known", identifier));
	}

	/**
	 * Returns all known product types.
	 *
	 * @return an array of known {@link ProductType} instances, never null.
	 */
	public static ProductType[] values() {
		return KNOWN_VALUES;
	}
}
