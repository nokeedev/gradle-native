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
package dev.nokee.language.swift.internal.plugins;

import dev.nokee.language.nativebase.internal.DependentFrameworkSearchPaths;
import dev.nokee.language.nativebase.internal.FrameworkAwareIncomingArtifacts;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.platform.base.internal.dependencies.DependencyBuckets;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
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
import static dev.nokee.platform.base.internal.DomainObjectEntities.newEntity;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;
import static dev.nokee.utils.TransformerUtils.toSetTransformer;
import static dev.nokee.utils.TransformerUtils.transformEach;

final class ImportModulesConfigurationRegistrationAction extends ModelActionWithInputs.ModelAction3<ModelProjection, IdentifierComponent, ModelState.IsAtLeastRegistered> {
	private final ModelRegistry registry;
	private final ObjectFactory objects;

	ImportModulesConfigurationRegistrationAction(ModelRegistry registry, ObjectFactory objects) {
		super(ModelComponentReference.ofProjection(SwiftSourceSetSpec.class), ModelComponentReference.of(IdentifierComponent.class), ModelComponentReference.of(ModelState.IsAtLeastRegistered.class));
		this.registry = registry;
		this.objects = objects;
	}

	@Override
	protected void execute(ModelNode entity, ModelProjection knownSourceSet, IdentifierComponent identifier, ModelState.IsAtLeastRegistered isAtLeastRegistered) {
		val importModules = registry.register(newEntity("importModules", ResolvableDependencyBucketSpec.class, it -> it.ownedBy(entity)));
		importModules.configure(Configuration.class, forSwiftApiUsage());
		val incomingArtifacts = FrameworkAwareIncomingArtifacts.from(incomingArtifactsOf(importModules));
		entity.addComponent(new DependentFrameworkSearchPaths(incomingArtifacts.getAs(frameworks()).map(parentFiles())));
		entity.addComponent(new DependentImportModules(incomingArtifacts.getAs(frameworks().negate())));
		entity.addComponent(new ImportModulesConfigurationComponent(ModelNodes.of(importModules)));
	}

	private Action<Configuration> forSwiftApiUsage() {
		return configureAttributes(builder -> builder.usage(objects.named(Usage.class, Usage.SWIFT_API)));
	}

	private static Transformer<Set<Path>, Iterable<? extends Path>> parentFiles() {
		return transformEach(Path::getParent).andThen(toSetTransformer(Path.class));
	}

	private Provider<ResolvableDependencies> incomingArtifactsOf(ModelElement element) {
		return element.as(Configuration.class).map(DependencyBuckets::finalize).map(Configuration::getIncoming);
	}
}
