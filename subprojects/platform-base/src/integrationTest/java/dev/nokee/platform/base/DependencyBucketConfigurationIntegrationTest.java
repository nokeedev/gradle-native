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
import dev.nokee.model.internal.DefaultDomainObjectIdentifier;
import dev.nokee.model.internal.core.DisplayName;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.IdentifierComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.core.ParentComponent;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.FullyQualifiedNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.artifacts.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static dev.nokee.internal.testing.ConfigurationMatchers.consumable;
import static dev.nokee.internal.testing.ConfigurationMatchers.declarable;
import static dev.nokee.internal.testing.ConfigurationMatchers.description;
import static dev.nokee.internal.testing.ConfigurationMatchers.resolvable;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.model.internal.core.ModelNodeUtils.canBeViewedAs;
import static dev.nokee.model.internal.core.ModelNodeUtils.get;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(GradleTestExtension.class)
@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
class DependencyBucketConfigurationIntegrationTest {
	ModelNode subject;

	@BeforeEach
	void createSubject(Project project) {
		subject = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder().withComponent(tag(IsDependencyBucket.class)).withComponent(new FullyQualifiedNameComponent("cufeDewaCuba")).build());
	}

	@Test
	void addConfigurationProjection() {
		assertTrue(canBeViewedAs(subject, ModelType.of(Configuration.class)));
	}

	@Test
	void usesEntityFullyQualifiedNameAsConfigurationName() {
		assertThat(get(subject, ModelType.of(Configuration.class)), named("cufeDewaCuba"));
	}

	@Test
	void configuresAsConsumableIfConsumableTag() {
		subject.addComponent(tag(ConsumableDependencyBucketSpec.Tag.class));
		assertThat(get(subject, ModelType.of(Configuration.class)), consumable());
	}

	@Test
	void configuresAsDeclarableIfDeclarableTag() {
		subject.addComponent(tag(DeclarableDependencyBucketSpec.Tag.class));
		assertThat(get(subject, ModelType.of(Configuration.class)), declarable());
	}

	@Test
	void configuresAsResolvableIfResolvableTag() {
		subject.addComponent(tag(ResolvableDependencyBucketSpec.Tag.class));
		assertThat(get(subject, ModelType.of(Configuration.class)), resolvable());
	}

	@Test
	void configuresDescriptionFromDisplayNameAndParentIdentifier(Project project) {
		val parentEntity = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder().withComponent(new IdentifierComponent(new DefaultDomainObjectIdentifier(ElementName.of("dewa"), null, DisplayName.of("project"), ModelPath.path("cufe.dewa")))).build());
		subject.addComponent(new DisplayNameComponent("link elements"));
		subject.addComponent(new ParentComponent(parentEntity));
		assertThat(get(subject, ModelType.of(Configuration.class)), description("Link elements for project ':cufe:dewa'."));
	}

	@Test
	void configuresDescriptionFromDisplayName__parentIdentifierIsAbsent(Project project) {
		val parentEntity = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder().build());
		subject.addComponent(new DisplayNameComponent("runtime elements"));
		subject.addComponent(new ParentComponent(parentEntity));
		assertThat(get(subject, ModelType.of(Configuration.class)), description("Runtime elements.")); // TODO: Should include path in description
	}

	@Test
	@Disabled("missing feature ModelComponentReference<...> as optional input")
	void configuresDescriptionFromDisplayName__noParentEntity() {
		subject.addComponent(new DisplayNameComponent("API elements"));
		assertThat(get(subject, ModelType.of(Configuration.class)), description("API elements ':cufe:dewa'."));
	}
}
