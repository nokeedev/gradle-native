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
import dev.nokee.platform.base.internal.binaries.BinaryConfigurer;
import dev.nokee.platform.base.internal.binaries.BinaryRepository;
import dev.nokee.platform.base.internal.binaries.BinaryViewFactory;
import dev.nokee.platform.base.internal.binaries.KnownBinaryFactory;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class BinaryBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ModelBasePlugin.class);

		val eventPublisher = project.getExtensions().getByType(DomainObjectEventPublisher.class);
		val realization = project.getExtensions().getByType(RealizableDomainObjectRealizer.class);

		val binaryRepository = new BinaryRepository(eventPublisher, realization, project.getProviders());
		project.getExtensions().add(BinaryRepository.class, "__NOKEE_binaryRepository", binaryRepository);

		val binaryConfigurer = new BinaryConfigurer(eventPublisher);
		project.getExtensions().add(BinaryConfigurer.class, "__NOKEE_binaryConfigurer", binaryConfigurer);

		val knownBinaryFactory = new KnownBinaryFactory(() -> binaryRepository, () -> project.getExtensions().getByType(BinaryConfigurer.class));
		project.getExtensions().add(KnownBinaryFactory.class, "__NOKEE_knownBinaryFactory", knownBinaryFactory);

		val binaryViewFactory = new BinaryViewFactory(binaryRepository, binaryConfigurer);
		project.getExtensions().add(BinaryViewFactory.class, "__NOKEE_binaryViewFactory", binaryViewFactory);
	}
}
