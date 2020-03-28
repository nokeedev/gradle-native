package dev.nokee.platform.nativebase.internal;

import org.gradle.api.attributes.Attribute;

public interface ArtifactTypes {
	Attribute<String> ARTIFACT_TYPES_ATTRIBUTE = Attribute.of("artifactType", String.class);

	/**
	 * @see org.gradle.api.artifacts.type.ArtifactTypeDefinition#DIRECTORY_TYPE
	 */
	String DIRECTORY_TYPE = "directory";

	/**
	 * Represents a macOS framework bundle.
	 */
	String FRAMEWORK_TYPE = "framework";

	static boolean isFramework(String name) {
		return name.equals(FRAMEWORK_TYPE);
	}

	static boolean isStaticLinkable(String name) {
		return name.equals("a") || name.equals("lib") || name.equals("so") || name.equals("dylib");
	}

	static boolean isSharedLibrary(String name) {
		return name.equals("so") || name.equals("dylib") || name.equals("dll");
	}

	static boolean isDirectory(String name) {
		return name.equals(DIRECTORY_TYPE);
	}
}
