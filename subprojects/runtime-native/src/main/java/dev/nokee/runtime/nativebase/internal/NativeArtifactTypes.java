package dev.nokee.runtime.nativebase.internal;

/**
 * Represent native artifact types.
 *
 * @see org.gradle.api.artifacts.type.ArtifactTypeDefinition
 */
public final class NativeArtifactTypes {
	private NativeArtifactTypes() {}

	/**
	 * Represents a directory tree containing native headers, often referred to as header search path or include root.
	 */
	public static final String NATIVE_HEADERS_DIRECTORY = "native-headers-directory";

	/**
	 * Represents a compressed directory tree containing native headers when published inside a remote artifact repository.
	 */
	public static final String NATIVE_HEADERS_ZIP = "native-headers-zip";

	/**
	 * Represents shared objects library file, mostly used on Unix-like system for shared library at link-time and runtime.
	 */
	public static final String SHARED_OBJECTS_LIBRARY = "so";

	/**
	 * Represents Mach-O dynamic library file, mostly used on macOS for shared library at link-time and runtime.
	 */
	public static final String MACH_OBJECT_DYNAMIC_LIBRARY = "dylib";

	/**
	 * Represents dynamic-link library file, mostly used on Windows for shared library at runtime.
	 */
	public static final String DYNAMIC_LINK_LIBRARY = "dll";

	/**
	 * Represents static library archive file, mostly used on Unix-like system for static library at link-time.
	 */
	public static final String STATIC_LIBRARY_ARCHIVE = "a";

	/**
	 * Represents static or import library file, mostly used on Windows for static library or shared library (linkable part) at link-time.
	 */
	public static final String STATIC_OR_IMPORT_LIBRARY = "lib";
}
