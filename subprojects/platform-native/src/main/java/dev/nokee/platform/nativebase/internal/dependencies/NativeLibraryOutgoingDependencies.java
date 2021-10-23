/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.nativebase.internal.dependencies;

import com.google.common.collect.Iterables;
import dev.nokee.model.NamedDomainObjectRegistry;
import dev.nokee.model.internal.DomainObjectIdentifierInternal;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketIdentifier;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketName;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.runtime.nativebase.internal.NativeArtifactTypes;
import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

import static dev.nokee.utils.ConfigurationUtils.configureAsConsumable;

public class NativeLibraryOutgoingDependencies extends AbstractNativeLibraryOutgoingDependencies implements NativeOutgoingDependencies {
	private final ConfigurationUtils builder;
	private final NamedDomainObjectProvider<Configuration> apiElements;

	@Inject
	public NativeLibraryOutgoingDependencies(DomainObjectIdentifierInternal ownerIdentifier, BuildVariantInternal buildVariant, DefaultNativeLibraryComponentDependencies dependencies, ConfigurationContainer configurationContainer, ObjectFactory objects) {
		super(ownerIdentifier, buildVariant, dependencies, configurationContainer, objects);
		builder = objects.newInstance(ConfigurationUtils.class);

		val configurationRegistry = NamedDomainObjectRegistry.of(configurationContainer);
		val identifier = DependencyBucketIdentifier.of(DependencyBucketName.of("apiElements"),
			ConsumableDependencyBucket.class, ownerIdentifier);
		// TODO: Introduce compileOnlyApi which apiElements should extends from
		this.apiElements = configurationRegistry.registerIfAbsent(identifier.getConfigurationName());
		apiElements.configure(configureAsConsumable());
		apiElements.configure(builder.asOutgoingHeaderSearchPathFrom(dependencies.getApi().getAsConfiguration()).withVariant(buildVariant).withDescription(identifier.getDisplayName()));

		// See https://github.com/gradle/gradle/issues/15146 to learn more about splitting the implicit dependencies
		apiElements.get().getOutgoing().artifact(getExportedHeaders().getElements().flatMap(it -> ProviderUtils.fixed(Iterables.getOnlyElement(it))), it -> {
			it.builtBy(getExportedHeaders());
			it.setType(NativeArtifactTypes.NATIVE_HEADERS_DIRECTORY);
		});
	}

	public Configuration getApiElements() {
		return apiElements.get();
	}
}
