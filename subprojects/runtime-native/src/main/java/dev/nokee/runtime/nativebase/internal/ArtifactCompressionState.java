/*
 * Copyright 2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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
