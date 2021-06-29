package dev.nokee.runtime.nativebase.internal;

/**
 * Represent native artifact types.
 *
 * @see org.gradle.api.artifacts.type.ArtifactTypeDefinition
 */
public final class NativeArtifactTypes {
	private NativeArtifactTypes() {}

	/**
	 * Represent a directory tree containing native headers, often referred to as header search path or include root.
	 */
	public static final String NATIVE_HEADERS_DIRECTORY = "native-headers-directory";
}
