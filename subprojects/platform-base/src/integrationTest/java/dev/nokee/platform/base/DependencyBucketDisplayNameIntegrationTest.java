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
import dev.nokee.internal.testing.junit.jupiter.GradleProject;
import dev.nokee.internal.testing.junit.jupiter.GradleTestExtension;
import dev.nokee.model.internal.core.DisplayName;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.names.ElementNameComponent;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.model.internal.tags.ModelTags;
import dev.nokee.platform.base.internal.IsDependencyBucket;
import dev.nokee.platform.base.internal.dependencies.ConsumableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.spotify.hamcrest.optional.OptionalMatchers.optionalWithValue;
import static dev.nokee.model.internal.core.ModelRegistration.builder;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(GradleTestExtension.class)
@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
class DependencyBucketDisplayNameIntegrationTest {
	@Test
	void doesNotOverwriteDisplayName(Project project) {
		val entity = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(IsDependencyBucket.class))
			.withComponent(new ElementNameComponent("puje"))
			.withComponent(new DisplayNameComponent("my custom display name")).build());
		ModelStates.register(entity);
		assertThat(entity.find(DisplayNameComponent.class).map(DisplayNameComponent::get),
			optionalWithValue(equalTo(DisplayName.of("my custom display name"))));
	}

	@Test
	void usesDefaultDisplayNameIfAbsent__singleWordElementName(Project project) {
		val entity = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(IsDependencyBucket.class))
			.withComponent(new ElementNameComponent("rubo"))
			.build());
		ModelStates.register(entity);
		assertThat(entity.find(DisplayNameComponent.class).map(DisplayNameComponent::get),
			optionalWithValue(equalTo(DisplayName.of("rubo"))));
	}

	@Test
	void usesDefaultDisplayNameIfAbsent__twoWordsCamelCaseElementName(Project project) {
		val entity = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(IsDependencyBucket.class))
			.withComponent(new ElementNameComponent("ruboXisa"))
			.build());
		ModelStates.register(entity);
		assertThat(entity.find(DisplayNameComponent.class).map(DisplayNameComponent::get),
			optionalWithValue(equalTo(DisplayName.of("rubo xisa"))));
	}

	@Test
	void usesDefaultDisplayNameIfAbsent__threeWordsCamelCaseElementName(Project project) {
		val entity = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(IsDependencyBucket.class))
			.withComponent(new ElementNameComponent("ruboXisaReli"))
			.build());
		ModelStates.register(entity);
		assertThat(entity.find(DisplayNameComponent.class).map(DisplayNameComponent::get),
			optionalWithValue(equalTo(DisplayName.of("rubo xisa reli"))));
	}

	@Test
	void usesDefaultDisplayNameIfAbsent__declarableBucketAppendsDependencies(Project project) {
		val entity = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(IsDependencyBucket.class))
			.withComponent(tag(DeclarableDependencyBucketSpec.Tag.class))
			.withComponent(new ElementNameComponent("heme"))
			.build());
		ModelStates.register(entity);
		assertThat(entity.find(DisplayNameComponent.class).map(DisplayNameComponent::get),
			optionalWithValue(equalTo(DisplayName.of("heme dependencies"))));
	}

	@Test
	void usesDefaultDisplayNameIfAbsent__consumableBucketDoesNotAppendsDependencies(Project project) {
		val entity = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(IsDependencyBucket.class))
			.withComponent(tag(ConsumableDependencyBucketSpec.Tag.class))
			.withComponent(new ElementNameComponent("xago"))
			.build());
		ModelStates.register(entity);
		assertThat(entity.find(DisplayNameComponent.class).map(DisplayNameComponent::get),
			optionalWithValue(equalTo(DisplayName.of("xago"))));
	}

	@Test
	void usesDefaultDisplayNameIfAbsent__resolvableBucketDoesNotAppendsDependencies(Project project) {
		val entity = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(IsDependencyBucket.class))
			.withComponent(tag(ResolvableDependencyBucketSpec.Tag.class))
			.withComponent(new ElementNameComponent("huhi"))
			.build());
		ModelStates.register(entity);
		assertThat(entity.find(DisplayNameComponent.class).map(DisplayNameComponent::get),
			optionalWithValue(equalTo(DisplayName.of("huhi"))));
	}

	@Test
	void usesDefaultDisplayNameIfAbsent__capitalizedApiKeyword(Project project) {
		val entity = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(IsDependencyBucket.class))
			.withComponent(new ElementNameComponent("apiElements"))
			.build());
		ModelStates.register(entity);
		assertThat(entity.find(DisplayNameComponent.class).map(DisplayNameComponent::get),
			optionalWithValue(equalTo(DisplayName.of("API elements"))));
	}

	@Test
	void usesDefaultDisplayNameIfAbsent__capitalizedJvmKeyword(Project project) {
		val entity = project.getExtensions().getByType(ModelRegistry.class).instantiate(builder()
			.withComponent(tag(IsDependencyBucket.class))
			.withComponent(new ElementNameComponent("jvmImplementation"))
			.build());
		ModelStates.register(entity);
		assertThat(entity.find(DisplayNameComponent.class).map(DisplayNameComponent::get),
			optionalWithValue(equalTo(DisplayName.of("JVM implementation"))));
	}
}
