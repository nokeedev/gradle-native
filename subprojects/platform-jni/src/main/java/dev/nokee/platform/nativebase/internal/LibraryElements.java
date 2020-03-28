package dev.nokee.platform.nativebase.internal;

import org.gradle.api.Named;
import org.gradle.api.attributes.Attribute;

public interface LibraryElements extends Named {
	/**
	 * @see org.gradle.api.attributes.LibraryElements#LIBRARY_ELEMENTS_ATTRIBUTE
	 */
	Attribute<LibraryElements> LIBRARY_ELEMENTS_ATTRIBUTE = Attribute.of(org.gradle.api.attributes.LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE.getName(), LibraryElements.class);

	/**
	 * Header files for C++
	 */
	String HEADERS_CPLUSPLUS = org.gradle.api.attributes.LibraryElements.HEADERS_CPLUSPLUS;

	/**
	 * Framework bundles
	 */
	String FRAMEWORK_BUNDLE = "framework-bundle";

	String LINK_ARCHIVE = org.gradle.api.attributes.LibraryElements.LINK_ARCHIVE;
	String OBJECTS = org.gradle.api.attributes.LibraryElements.OBJECTS;
	String DYNAMIC_LIB = org.gradle.api.attributes.LibraryElements.DYNAMIC_LIB;
	String IMPORT_LIB = "import-lib";

	default boolean isFrameworkBundle() {
		return getName().equals(FRAMEWORK_BUNDLE);
	}

	default boolean isSharedLibrary() {
		return getName().equals(DYNAMIC_LIB);
	}
}
