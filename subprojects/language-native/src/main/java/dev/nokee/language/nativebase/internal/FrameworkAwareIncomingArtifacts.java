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
package dev.nokee.language.nativebase.internal;

import com.google.common.collect.Streams;
import dev.nokee.runtime.darwin.internal.DarwinLibraryElements;
import dev.nokee.utils.SpecUtils;
import lombok.val;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.ArtifactCollection;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.artifacts.result.ResolvedArtifactResult;
import org.gradle.api.attributes.Attribute;
import org.gradle.api.attributes.LibraryElements;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.nio.file.Path;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static dev.nokee.runtime.nativebase.internal.ArtifactCompressionState.ARTIFACT_COMPRESSION_STATE_ATTRIBUTE;
import static dev.nokee.runtime.nativebase.internal.ArtifactCompressionState.UNCOMPRESSED;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;

public final class FrameworkAwareIncomingArtifacts {
	private static final Logger LOGGER = Logger.getLogger(HeaderSearchPathsConfigurationRegistrationAction.class.getCanonicalName());
	private final Provider<ResolvableDependencies> incomingArtifacts;

	private FrameworkAwareIncomingArtifacts(Provider<ResolvableDependencies> incomingArtifacts) {
		this.incomingArtifacts = incomingArtifacts;
	}

	public static FrameworkAwareIncomingArtifacts from(Provider<ResolvableDependencies> incomingArtifacts) {
		return new FrameworkAwareIncomingArtifacts(incomingArtifacts);
	}

	public Provider<Set<Path>> getAs(Spec<? super ResolvedArtifactResult> spec) {
		val artifacts = incomingArtifacts.map(asUncompressedArtifacts());
		return artifacts.flatMap(it -> it.getArtifactFiles().getElements()).map(filterArtifacts(artifacts, spec));
	}

	private static Transformer<Set<Path>, Object> filterArtifacts(Provider<ArtifactCollection> artifacts, Spec<? super ResolvedArtifactResult> spec) {
		return ignored -> Streams.stream(artifacts.get()).filter(spec::isSatisfiedBy).map(toPath()).collect(Collectors.toSet());
	}

	private static Function<ResolvedArtifactResult, Path> toPath() {
		return it -> it.getFile().toPath();
	}

	private static Transformer<ArtifactCollection, ResolvableDependencies> asUncompressedArtifacts() {
		return incoming -> incoming.artifactView(configureAttributes(it -> it.attribute(ARTIFACT_COMPRESSION_STATE_ATTRIBUTE, UNCOMPRESSED)))
			.getArtifacts();
	}

	public static SpecUtils.Spec<ResolvedArtifactResult> frameworks() {
		return result -> {
			Optional<Attribute<?>> attribute = result.getVariant().getAttributes().keySet().stream().filter(it -> it.getName().equals(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE.getName())).findFirst();
			if (attribute.isPresent()) {
				String v = result.getVariant().getAttributes().getAttribute(attribute.get()).toString();
				if (v.equals(DarwinLibraryElements.FRAMEWORK_BUNDLE)) {
					return true;
				}
				return false;
			}
			LOGGER.finest(() -> "No library elements on dependency\n" + result.getVariant().getAttributes().keySet().stream().map(Attribute::getName).collect(Collectors.joining(", ")));
			return false;
		};
	}
}
