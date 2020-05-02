package dev.nokee.ide.xcode;

/**
 * Factory for accessing known Xcode IDE product types or to create custom product types.
 *
 * @since 0.3
 */
public interface XcodeIdeProductTypes {
	XcodeIdeProductType STATIC_LIBRARY = XcodeIdeProductType.of("com.apple.product-type.library.static");
	XcodeIdeProductType DYNAMIC_LIBRARY = XcodeIdeProductType.of("com.apple.product-type.library.dynamic");
	XcodeIdeProductType TOOL = XcodeIdeProductType.of("com.apple.product-type.tool");
	XcodeIdeProductType BUNDLE = XcodeIdeProductType.of("com.apple.product-type.bundle");
	XcodeIdeProductType FRAMEWORK = XcodeIdeProductType.of("com.apple.product-type.framework");
	XcodeIdeProductType STATIC_FRAMEWORK = XcodeIdeProductType.of("com.apple.product-type.framework.static");
	XcodeIdeProductType APPLICATION = XcodeIdeProductType.of("com.apple.product-type.application");
	XcodeIdeProductType WATCH_APPLICATION = XcodeIdeProductType.of("com.apple.product-type.application.watchapp2");
	XcodeIdeProductType UNIT_TEST = XcodeIdeProductType.of("com.apple.product-type.bundle.unit-test");
	XcodeIdeProductType UI_TEST = XcodeIdeProductType.of("com.apple.product-type.bundle.ui-testing");
	XcodeIdeProductType APP_EXTENSION = XcodeIdeProductType.of("com.apple.product-type.app-extension");

	default XcodeIdeProductType getStaticLibrary() {
		return STATIC_LIBRARY;
	}

	default XcodeIdeProductType getDynamicLibrary() {
		return DYNAMIC_LIBRARY;
	}

	default XcodeIdeProductType getTool() {
		return TOOL;
	}

	default XcodeIdeProductType getApplication() {
		return APPLICATION;
	}

	default XcodeIdeProductType of(String identifier) {
		return XcodeIdeProductType.of(identifier);
	}

	/**
	 * Returns all known product types.
	 * Support for each product may not be complete.
	 *
	 * @return an array of known {@link XcodeIdeProductType} instances, never null.
	 */
	static XcodeIdeProductType[] getKnownValues() {
		return new XcodeIdeProductType[] {STATIC_LIBRARY, DYNAMIC_LIBRARY, TOOL, BUNDLE, FRAMEWORK, STATIC_FRAMEWORK, APPLICATION, WATCH_APPLICATION, UNIT_TEST, UI_TEST, APP_EXTENSION};
	}
}
