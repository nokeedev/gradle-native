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
package dev.nokee.model.internal.plugins;

import dev.nokee.model.internal.DomainObjectEventPublisher;
import dev.nokee.model.internal.DomainObjectEventPublisherImpl;
import dev.nokee.model.internal.RealizableDomainObjectRealizer;
import dev.nokee.model.internal.RealizableDomainObjectRealizerImpl;
import dev.nokee.model.internal.core.DirectInstantiationModelEntityFactory;
import dev.nokee.model.internal.core.ModelEntityFactory;
import dev.nokee.model.internal.core.ModelPropertyRegistrationFactory;
import dev.nokee.model.internal.registry.DefaultModelRegistry;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.tasks.ModelReportTask;
import dev.nokee.utils.TaskUtils;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

public class ModelBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		val eventPublisher = new DomainObjectEventPublisherImpl();
		project.getExtensions().add(DomainObjectEventPublisher.class, "__NOKEE_eventPublisher", eventPublisher);

		val realization = new RealizableDomainObjectRealizerImpl(eventPublisher);
		project.getExtensions().add(RealizableDomainObjectRealizer.class, "__NOKEE_realization", realization);

		val modelRegistry = new DefaultModelRegistry(project.getObjects()::newInstance);
		project.getExtensions().add(ModelRegistry.class, "__NOKEE_modelRegistry", modelRegistry);
		project.getExtensions().add(ModelLookup.class, "__NOKEE_modelLookup", modelRegistry);
		project.getExtensions().add(ModelConfigurer.class, "__NOKEE_modelConfigurer", modelRegistry);
		project.getExtensions().add(ModelPropertyRegistrationFactory.class, "__NOKEE_modelPropertyRegistrationFactory", new ModelPropertyRegistrationFactory(modelRegistry, project.getObjects()));

		project.getExtensions().add(ModelEntityFactory.class, "__NOKEE_modelEntityFactory", new DirectInstantiationModelEntityFactory());

		project.getTasks().register("nokeeModel", ModelReportTask.class, TaskUtils.configureDescription("Displays the configuration model of %s.", project));
	}
}
