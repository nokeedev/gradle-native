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

	/**
	 * Returns a static library product, also known as static library.
	 *
	 * @return a {@link XcodeIdeProductType} instance representing a static library, never null.
	 * @see XcodeIdeProductTypes#STATIC_LIBRARY
	 */
	default XcodeIdeProductType getStaticLibrary() {
		return STATIC_LIBRARY;
	}

	/**
	 * Returns a dynamic library product type, also known as a shared library.
	 *
	 * @return a {@link XcodeIdeProductType} instance representing a dynamic library, never null.
	 * @see XcodeIdeProductTypes#DYNAMIC_LIBRARY
	 */
	default XcodeIdeProductType getDynamicLibrary() {
		return DYNAMIC_LIBRARY;
	}

	/**
	 * Returns a tool product type, also known as a command line executable.
	 *
	 * @return a {@link XcodeIdeProductType} instance representing a tool, never null.
	 * @see XcodeIdeProductTypes#TOOL
	 */
	default XcodeIdeProductType getTool() {
		return TOOL;
	}

	/**
	 * Returns an application product type, also known as an application bundle.
	 *
	 * @return a {@link XcodeIdeProductType} instance representing an application, never null.
	 * @see XcodeIdeProductTypes#APPLICATION
	 */
	default XcodeIdeProductType getApplication() {
		return APPLICATION;
	}

	/**
	 * Creates a product type of the specified identifier.
	 *
	 * @param identifier a identifier for the product type to create.
	 * @return a {@link XcodeIdeProductType} instance, never null.
	 */
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
