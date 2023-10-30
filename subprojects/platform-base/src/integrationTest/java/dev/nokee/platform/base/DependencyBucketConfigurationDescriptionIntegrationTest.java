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

import dev.nokee.internal.testing.ConfigurationMatchers;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.GradleTestExtension;
import dev.nokee.model.internal.DefaultModelObjectIdentifier;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ConfigurationComponent;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static org.hamcrest.MatcherAssert.assertThat;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class DependencyBucketConfigurationDescriptionIntegrationTest {
	NamedDomainObjectProvider<Configuration> configuration;
	ModelNode owner;
	ModelNode subject;

	@BeforeEach
	void createSubject(Project project) {
		owner = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder().build());
		subject = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponentTag(IsDependencyBucket.class)
			.withComponent(new DisplayNameComponent("xune dependencies"))
			.withComponent(new ConfigurationComponent(configuration = project.getConfigurations().register("xune")))
			.build());
	}

	@Test
	void configuresConfigurationProjectionDescriptionWithOwnerIdentifier() {
		owner.addComponent(new IdentifierComponent(new DefaultModelObjectIdentifier(ElementName.of("zimo"))));
		subject.addComponent(new ParentComponent(owner));
		assertThat(configuration.get(), ConfigurationMatchers.description("Xune dependencies for artifact ':zimo'."));
	}

	@Test
	void configuresConfigurationProjectionDescriptionWithoutOwnerIdentifier() {
		subject.addComponent(new ParentComponent(owner));
		assertThat(configuration.get(), ConfigurationMatchers.description("Xune dependencies."));
	}
}
