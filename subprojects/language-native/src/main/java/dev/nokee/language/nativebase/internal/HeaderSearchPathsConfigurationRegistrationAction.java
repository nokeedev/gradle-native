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

import dev.nokee.language.base.internal.IsLanguageSourceSet;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import java.nio.file.Path;
import java.util.Set;

import static dev.nokee.language.nativebase.internal.FrameworkAwareIncomingArtifacts.frameworks;
import static dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentity.resolvable;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.TransformerUtils.toSetTransformer;
import static dev.nokee.utils.TransformerUtils.transformEach;

public final class HeaderSearchPathsConfigurationRegistrationAction extends ModelActionWithInputs.ModelAction3<ModelProjection, IdentifierComponent, IsLanguageSourceSet> {
	private final ModelRegistry registry;
	private final ResolvableDependencyBucketRegistrationFactory resolvableFactory;
	private final ObjectFactory objects;

	HeaderSearchPathsConfigurationRegistrationAction(ModelRegistry registry, ResolvableDependencyBucketRegistrationFactory resolvableFactory, ObjectFactory objects) {
		super(ModelComponentReference.ofProjection(HasConfigurableHeadersMixIn.class), ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(IsLanguageSourceSet.class));
		this.registry = registry;
		this.resolvableFactory = resolvableFactory;
		this.objects = objects;
	}

	@Override
	protected void execute(ModelNode entity, ModelProjection knownObject, IdentifierComponent identifier, IsLanguageSourceSet ignored) {
		val headerSearchPaths = registry.register(ModelRegistration.builder().mergeFrom(resolvableFactory.create(DependencyBucketIdentifier.of(resolvable("headerSearchPaths"), identifier.get()))).withComponent(HeaderSearchPathsDependencyBucketTag.tag()).build());
		headerSearchPaths.configure(Configuration.class, forCPlusPlusApiUsage());
		val incomingArtifacts = FrameworkAwareIncomingArtifacts.from(incomingArtifactsOf(headerSearchPaths));
		entity.addComponent(new HeaderSearchPathsConfigurationComponent(ModelNodes.of(headerSearchPaths)));
		entity.addComponent(new DependentFrameworkSearchPaths(incomingArtifacts.getAs(frameworks()).map(parentFiles())));
		entity.addComponent(new DependentHeaderSearchPaths(incomingArtifacts.getAs(frameworks().negate())));
	}

	private Action<Configuration> forCPlusPlusApiUsage() {
		return configureAttributes(builder -> builder.usage(objects.named(Usage.class, Usage.C_PLUS_PLUS_API)));
	}

	private static Transformer<Set<Path>, Iterable<? extends Path>> parentFiles() {
		return transformEach(Path::getParent).andThen(toSetTransformer(Path.class));
	}

	private Provider<ResolvableDependencies> incomingArtifactsOf(ModelElement element) {
		return element.as(Configuration.class).map(Configuration::getIncoming);
	}
}
