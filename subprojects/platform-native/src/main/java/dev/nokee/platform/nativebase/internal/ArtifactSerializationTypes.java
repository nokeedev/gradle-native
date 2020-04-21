package dev.nokee.platform.nativebase.internal;

import org.gradle.api.attributes.Attribute;

public interface ArtifactSerializationTypes {
	Attribute<String> ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE = Attribute.of("dev.nokee.artifactSerializationType", String.class);

	String SERIALIZED = "serialized";

	String DESERIALIZED = "deserialized";
}
