/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.platform.nativebase.internal.linking;

import dev.nokee.language.nativebase.internal.FrameworkAwareIncomingArtifacts;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelActionWithInputs;
import dev.nokee.model.internal.core.ModelComponentReference;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelProjection;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.tags.ModelComponentTag;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.internal.IsBinary;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketRegistrationFactory;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ResolvableDependencies;
import org.gradle.api.attributes.Usage;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.provider.Provider;

import static dev.nokee.language.nativebase.internal.FrameworkAwareIncomingArtifacts.frameworks;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.utils.ConfigurationUtils.configureAttributes;

final class LinkLibrariesConfigurationRegistrationRule extends ModelActionWithInputs.ModelAction3<IdentifierComponent, ModelComponentTag<IsBinary>, ModelProjection> {
	private final ModelRegistry registry;
	private final ResolvableDependencyBucketRegistrationFactory resolvableFactory;
	private final ObjectFactory objects;

	public LinkLibrariesConfigurationRegistrationRule(ModelRegistry registry, ResolvableDependencyBucketRegistrationFactory resolvableFactory, ObjectFactory objects) {
		super(ModelComponentReference.of(IdentifierComponent.class), ModelTags.referenceOf(IsBinary.class), ModelComponentReference.ofProjection(HasLinkLibrariesDependencyBucket.class));
		this.registry = registry;
		this.resolvableFactory = resolvableFactory;
		this.objects = objects;
	}

	@Override
	protected void execute(ModelNode entity, IdentifierComponent identifier, ModelComponentTag<IsBinary> ignored, ModelProjection projection) {
		val linkLibraries = registry.register(ModelRegistration.builder().mergeFrom(resolvableFactory.create(DependencyBucketIdentifier.of("linkLibraries", identifier.get()))).withComponent(tag(LinkLibrariesDependencyBucketTag.class)).build());
		linkLibraries.configure(Configuration.class, forNativeLinkUsage());
		val incomingArtifacts = FrameworkAwareIncomingArtifacts.from(incomingArtifactsOf(linkLibraries));
		entity.addComponent(new DependentFrameworks(incomingArtifacts.getAs(frameworks())));
		entity.addComponent(new DependentLinkLibraries(incomingArtifacts.getAs(frameworks().negate())));
		entity.addComponent(new LinkLibrariesConfiguration(linkLibraries.as(Configuration.class)));
	}

	private Action<Configuration> forNativeLinkUsage() {
		return configureAttributes(builder -> builder.usage(objects.named(Usage.class, Usage.NATIVE_LINK)));
	}

	private static Provider<ResolvableDependencies> incomingArtifactsOf(ModelElement element) {
		return element.as(Configuration.class).map(Configuration::getIncoming);
	}
}
