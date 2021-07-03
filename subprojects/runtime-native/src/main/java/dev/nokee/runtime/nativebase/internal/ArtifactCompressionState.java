package dev.nokee.runtime.nativebase.internal;

import org.gradle.api.artifacts.type.ArtifactTypeContainer;
import org.gradle.api.artifacts.type.ArtifactTypeDefinition;
import org.gradle.api.attributes.Attribute;

/**
 * Represents compression state for native artifact.
 * Uncompressed artifact are typically unusable as-is for building native application.
 */
public enum ArtifactCompressionState {
	COMPRESSED, UNCOMPRESSED;

	public static final Attribute<ArtifactCompressionState> ARTIFACT_COMPRESSION_STATE_ATTRIBUTE = Attribute.of("dev.nokee.internal.artifactCompressionState", ArtifactCompressionState.class);

	static void configureArtifactsCompressionState(ArtifactTypeContainer artifactTypes) {
		artifactTypes.maybeCreate(ArtifactTypeDefinition.ZIP_TYPE).getAttributes().attribute(ARTIFACT_COMPRESSION_STATE_ATTRIBUTE, COMPRESSED);
	}
}
