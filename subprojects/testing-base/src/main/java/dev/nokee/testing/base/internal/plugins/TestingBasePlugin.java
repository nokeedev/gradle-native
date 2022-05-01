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
package dev.nokee.testing.base.internal.plugins;

import dev.nokee.model.internal.ModelPropertyIdentifier;
import dev.nokee.model.internal.ProjectIdentifier;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelNodeContext;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.core.ModelSpecs;
import dev.nokee.model.internal.core.NodeRegistrationFactories;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.type.TypeOf;
import dev.nokee.platform.base.internal.ViewAdapter;
import dev.nokee.platform.base.internal.elements.ComponentElementsPropertyRegistrationFactory;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.testing.base.internal.TestSuiteContainerAdapter;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.model.internal.core.ModelProjections.createdUsing;
import static dev.nokee.model.internal.core.ModelProjections.ofInstance;
import static dev.nokee.model.internal.type.ModelType.of;

public class TestingBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ComponentModelBasePlugin.class);

		val modeRegistry = project.getExtensions().getByType(ModelRegistry.class);

		val elementsPropertyFactory = new ComponentElementsPropertyRegistrationFactory();
		val testSuites = modeRegistry.register(ModelRegistration.builder()
			.withComponent(new IdentifierComponent(ModelPropertyIdentifier.of(ProjectIdentifier.of(project), "testSuites")))
			.mergeFrom(elementsPropertyFactory.newProperty()
				.baseRef(project.getExtensions().getByType(ModelLookup.class).get(ModelPath.root()))
				.elementType(of(TestSuiteComponent.class))
				.build())
			.withComponent(createdUsing(of(TestSuiteContainer.class), () -> new TestSuiteContainerAdapter(ModelNodeUtils.get(ModelNodeContext.getCurrentModelNode(), of(new TypeOf<ViewAdapter<TestSuiteComponent>>() {})), modeRegistry)))
			.withComponent(ofInstance(new NodeRegistrationFactories()))
			.build()
		);
		project.getExtensions().add(TestSuiteContainer.class, "testSuites", testSuites.as(TestSuiteContainer.class).get());

		project.afterEvaluate(proj -> {
			// Force realize all test suite... until we solve the differing problem.
			project.getExtensions().getByType(ModelLookup.class).query(ModelSpecs.of(ModelNodes.withType(of(TestSuiteComponent.class)))).forEach(ModelStates::realize);
		});
	}
}
