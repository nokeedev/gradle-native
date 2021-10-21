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

import dev.nokee.model.internal.core.*;
import dev.nokee.model.internal.registry.ModelConfigurer;
import dev.nokee.model.internal.registry.ModelLookup;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelState;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.internal.IsComponent;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.testing.base.internal.DefaultTestSuiteContainer;
import dev.nokee.testing.base.internal.IsTestComponent;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.Project;

import static dev.nokee.model.internal.BaseNamedDomainObjectContainer.namedContainer;
import static dev.nokee.model.internal.type.ModelType.of;

public class TestingBasePlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(ComponentModelBasePlugin.class);

		val modeRegistry = project.getExtensions().getByType(ModelRegistry.class);
		val components = modeRegistry.register(testSuites()).as(DefaultTestSuiteContainer.class).get();
		project.getExtensions().add(TestSuiteContainer.class, "testSuites", components);

		val propertyFactory = project.getExtensions().getByType(ModelPropertyRegistrationFactory.class);
		project.getExtensions().getByType(ModelConfigurer.class).configure(ModelActionWithInputs.of(ModelComponentReference.of(ModelPath.class), ModelComponentReference.of(ModelState.IsAtLeastCreated.class), ModelComponentReference.of(IsTestComponent.class), ModelComponentReference.ofAny(ModelComponentType.projectionOf(TestSuiteComponent.class)), (e, p, ignored1, ignored2, projection) -> {
			if (ModelPath.root().isDirectDescendant(p)) {
				modeRegistry.register(propertyFactory.create(ModelPath.path("testSuites").child(p.getName()), e));
			}
		}));

		project.afterEvaluate(proj -> {
			// Force realize all test suite... until we solve the differing problem.
			project.getExtensions().getByType(ModelLookup.class).query(ModelSpecs.of(ModelNodes.withType(of(TestSuiteComponent.class)))).forEach(ModelStates::realize);
		});
	}

	private static NodeRegistration testSuites() {
		return namedContainer("testSuites", of(DefaultTestSuiteContainer.class));
	}
}
