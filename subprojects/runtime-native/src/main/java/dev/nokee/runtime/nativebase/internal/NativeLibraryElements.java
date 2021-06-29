package dev.nokee.runtime.nativebase.internal;

import org.gradle.api.attributes.LibraryElements;

import java.util.Objects;

public final class NativeLibraryElements {
	private NativeLibraryElements() {}

	public static final String IMPORT_LIB = "import-lib";

	public static boolean isImportLibrary(LibraryElements self) {
		return Objects.equals(self.getName(), IMPORT_LIB);
	}

	public static boolean isLinkArchive(LibraryElements self) {
		return Objects.equals(self.getName(), LibraryElements.LINK_ARCHIVE);
	}

	public static boolean isDynamicLibrary(LibraryElements self) {
		return Objects.equals(self.getName(), LibraryElements.DYNAMIC_LIB);
	}
}
