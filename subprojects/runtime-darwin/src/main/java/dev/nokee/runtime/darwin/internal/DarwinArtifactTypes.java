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
	 * Represents a macOS framework bundle, directory-like, archive when published inside a remote artifact repository.
	 */
	public static final String FRAMEWORK_ZIP = "framework-zip";

	public static boolean isFramework(String name) {
		return name.equals(FRAMEWORK_TYPE);
	}
}
