package dev.nokee.runtime.darwin.internal;

/**
 * Represents Darwin, macOS/iOS artifact types.
 *
 * @see dev.nokee.runtime.nativebase.internal.NativeArtifactTypes
 * @see org.gradle.api.artifacts.type.ArtifactTypeDefinition
 */
public final class DarwinArtifactTypes {
	private DarwinArtifactTypes() {}

	/**
	 * Represents a macOS framework bundle, directory-like, file, i.e. {@literal JavaNativeFoundation.framework}.
	 */
	public static final String FRAMEWORK_TYPE = "framework";

	/**
	 * Represents a directory tree containing native headers or a macOS framework bundle.
	 * @see dev.nokee.runtime.nativebase.internal.NativeArtifactTypes#NATIVE_HEADERS_DIRECTORY
	 * @see #FRAMEWORK_TYPE
	 */
	public static final String NATIVE_HEADERS_DIRECTORY_OR_FRAMEWORK_TYPE = "native-headers-directory-or-framework";

	/**
	 * Represents a linkable native library (e.g. import library, shared library, objects) or a macOS framework bundle.
	 * @see #FRAMEWORK_TYPE
	 */
	public static final String LINKABLE_ELEMENT_OR_FRAMEWORK_TYPE = "linkable-element-or-framework";
}
