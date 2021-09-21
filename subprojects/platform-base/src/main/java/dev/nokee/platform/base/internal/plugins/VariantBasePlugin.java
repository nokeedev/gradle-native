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
package dev.nokee.platform.base.internal.plugins;

import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.platform.base.internal.variants.KnownVariantFactory;
import dev.nokee.platform.base.internal.variants.VariantConfigurer;
import dev.nokee.platform.base.internal.variants.VariantRepository;
import dev.nokee.platform.base.internal.variants.VariantViewFactory;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class VariantBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);

		val eventPublisher = project.getExtensions().getByType(DomainObjectEventPublisher.class);
		val realization = project.getExtensions().getByType(RealizableDomainObjectRealizer.class);

		val variantRepository = new VariantRepository(eventPublisher, realization, project.getProviders());
		project.getExtensions().add(VariantRepository.class, "__NOKEE_variantRepository", variantRepository);

		val variantConfigurer = new VariantConfigurer(eventPublisher);
		project.getExtensions().add(VariantConfigurer.class, "__NOKEE_variantConfigurer", variantConfigurer);

		val knownVariantFactory = new KnownVariantFactory(() -> variantRepository, () -> variantConfigurer);
		project.getExtensions().add(KnownVariantFactory.class, "__NOKEE_knownVariantFactory", knownVariantFactory);

		val variantViewFactory = new VariantViewFactory(variantRepository, variantConfigurer, knownVariantFactory);
		project.getExtensions().add(VariantViewFactory.class, "__NOKEE_variantViewFactory", variantViewFactory);
	}
}
