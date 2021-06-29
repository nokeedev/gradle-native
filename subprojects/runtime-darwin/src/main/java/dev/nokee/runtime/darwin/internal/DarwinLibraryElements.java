package dev.nokee.runtime.darwin.internal;

import org.gradle.api.attributes.LibraryElements;

import java.util.Objects;

/** @see org.gradle.api.attributes.LibraryElements */
public final class DarwinLibraryElements {
	private DarwinLibraryElements() {}

	/**
	 * Represents a macOS framework bundle for {@link org.gradle.api.attributes.Category#LIBRARY library category}.
	 * A framework bundle usually provide native headers and shared library.
	 */
	public static final String FRAMEWORK_BUNDLE = "framework-bundle";

	public static boolean isFrameworkBundle(LibraryElements self) {
		return Objects.equals(self.getName(), FRAMEWORK_BUNDLE);
	}
}
