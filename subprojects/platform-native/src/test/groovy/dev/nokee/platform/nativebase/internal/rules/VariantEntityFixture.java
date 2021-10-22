/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.platform.nativebase.internal.rules;

import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.internal.ComponentIdentifier;
import dev.nokee.platform.base.internal.VariantIdentifier;
import dev.nokee.platform.base.internal.variants.KnownVariant;
import dev.nokee.platform.base.internal.variants.KnownVariantFactory;
import dev.nokee.platform.base.internal.variants.VariantConfigurer;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import org.gradle.api.Project;

import java.util.Arrays;

public interface VariantEntityFixture extends NokeeEntitiesFixture {
	Project getProject();

	default VariantConfigurer getVariantConfigurer() {
		return new VariantConfigurer(getEventPublisher());
	}

	default VariantRepository getVariantRepository() {
		return new VariantRepository(getEventPublisher(), getEntityRealizer(), getProject().getProviders());
	}

	default KnownVariantFactory getKnownVariantFactory() {
		return new KnownVariantFactory(this::getVariantRepository, this::getVariantConfigurer);
	}

	static VariantIdentifier<Variant> onlyVariantOfMainComponent() {
		return onlyVariantOfMainComponent(ProjectIdentifier.of("root"));
	}

	static VariantIdentifier<Variant> onlyVariantOfMainComponent(ProjectIdentifier projectOwner) {
		VariantIdentifier.Builder<Variant> builder = VariantIdentifier.builder().withType(Variant.class).withComponentIdentifier(ComponentIdentifier.ofMain(projectOwner));
		return builder.build();
	}

	static VariantIdentifier<Variant> aVariantOfMainComponent(String... ambiguousDimensionName) {
		return aVariantOfMainComponent(ProjectIdentifier.of("root"), ambiguousDimensionName);
	}

	static VariantIdentifier<Variant> aVariantOfMainComponent(ProjectIdentifier projectOwner, String... ambiguousDimensionName) {
		VariantIdentifier.Builder<Variant> builder = VariantIdentifier.builder().withType(Variant.class).withComponentIdentifier(ComponentIdentifier.ofMain(projectOwner));
		for (String it : ambiguousDimensionName) {
			builder.withVariantDimension(it, Arrays.asList(it, "dummy"));
		}
		return builder.build();
	}

	default <S extends Variant> KnownVariant<S> known(VariantIdentifier<S> identifier) {
		return getKnownVariantFactory().create(identifier);
	}
}
