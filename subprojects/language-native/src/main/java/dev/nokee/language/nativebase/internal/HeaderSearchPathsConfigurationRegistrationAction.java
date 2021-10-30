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

import com.google.auto.factory.AutoFactory;
import com.google.auto.factory.Provided;
import dev.nokee.language.base.internal.LanguageSourceSetIdentifier;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.attributes.Usage;
import org.gradle.api.file.FileCollection;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import java.util.Set;

import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.resolvable;
import static dev.nokee.runtime.nativebase.internal.ArtifactCompressionState.ARTIFACT_COMPRESSION_STATE_ATTRIBUTE;
import static dev.nokee.runtime.nativebase.internal.ArtifactCompressionState.UNCOMPRESSED;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.FileCollectionUtils.elementsOf;

@AutoFactory
public final class HeaderSearchPathsConfigurationRegistrationAction extends ModelActionWithInputs.ModelAction2<LanguageSourceSetIdentifier, ModelState.IsAtLeastRegistered> {
	private final LanguageSourceSetIdentifier identifier;
	private final ModelRegistry registry;
	private final ResolvableDependencyBucketRegistrationFactory resolvableFactory;
	private final ObjectFactory objects;

	HeaderSearchPathsConfigurationRegistrationAction(LanguageSourceSetIdentifier identifier, @Provided ModelRegistry registry, @Provided ResolvableDependencyBucketRegistrationFactory resolvableFactory, @Provided ObjectFactory objects) {
		this.identifier = identifier;
		this.registry = registry;
		this.resolvableFactory = resolvableFactory;
		this.objects = objects;
	}

	@Override
	protected void execute(ModelNode entity, LanguageSourceSetIdentifier identifier, ModelState.IsAtLeastRegistered isAtLeastRegistered) {
		if (identifier.equals(this.identifier)) {
			val headerSearchPaths = registry.register(resolvableFactory.create(DependencyBucketIdentifier.of(resolvable("headerSearchPaths"), identifier)));
			headerSearchPaths.configure(Configuration.class, forCPlusPlusApiUsage());
			// TODO: add DependentFramework...
			entity.addComponent(new DependentHeaderSearchPaths(incomingArtifactsOf(headerSearchPaths)));
		}
	}

	private Action<Configuration> forCPlusPlusApiUsage() {
		return configureAttributes(builder -> builder.usage(objects.named(Usage.class, Usage.C_PLUS_PLUS_API)));
	}

	private Provider<Set<FileSystemLocation>> incomingArtifactsOf(ModelElement element) {
		return element.as(Configuration.class).flatMap(elementsOf(this::uncompressedIncomingArtifacts));
	}

	private FileCollection uncompressedIncomingArtifacts(Configuration configuration) {
		return configuration.getIncoming()
			.artifactView(configureAttributes(it -> it.attribute(ARTIFACT_COMPRESSION_STATE_ATTRIBUTE, UNCOMPRESSED)))
			.getFiles();
	}
}
