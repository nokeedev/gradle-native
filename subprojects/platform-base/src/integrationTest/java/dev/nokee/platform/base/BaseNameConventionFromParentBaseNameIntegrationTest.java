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
package dev.nokee.platform.base;

import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.GradleTestExtension;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.BaseNameComponent;
import dev.nokee.platform.base.internal.BaseNamePropertyComponent;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.nokee.internal.testing.GradleProviderMatchers.providerOf;
import static dev.nokee.model.internal.core.ModelProperties.clear;
import static dev.nokee.model.internal.core.ModelProperties.valueOf;
import static dev.nokee.model.internal.core.ModelPropertyRegistrationFactory.property;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static org.hamcrest.MatcherAssert.assertThat;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class BaseNameConventionFromParentBaseNameIntegrationTest {
	ModelNode subject;
	ModelNode baseName;

	@BeforeEach
	void createSubject(Project project) {
		subject = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder().build());

		baseName = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(new ParentComponent(subject))
			.mergeFrom(property(String.class))
			.build());
		clear(baseName); // ensure only convention is applied

		subject.addComponent(new BaseNamePropertyComponent(baseName));
	}

	@Test
	void prefersParentBaseNameOverElementName(Project project) {
		// given: subject has element name
		subject.addComponent(new ElementNameComponent("sehu"));

		// and: has parent with baseName property
		val parent = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(new BaseNameComponent("kels"))
			.build());
		subject.addComponent(new ParentComponent(parent));

		assertThat(valueOf(baseName), providerOf("kels"));
	}

	@Test
	void searchAllParentsForBaseNameProperty(Project project) {
		// given: has parent with baseName property
		val parent = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(new BaseNameComponent("diqa"))
			.build());

		// and: intermediate node
		val intermediate = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(new ParentComponent(parent))
			.build());
		subject.addComponent(new ParentComponent(intermediate));

		assertThat(valueOf(baseName), providerOf("diqa"));
	}
}
