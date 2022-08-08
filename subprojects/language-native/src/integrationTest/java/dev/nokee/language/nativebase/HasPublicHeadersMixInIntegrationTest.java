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
package dev.nokee.language.nativebase;

import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.GradleTestExtension;
import dev.nokee.language.nativebase.internal.HasPublicHeadersMixIn;
import dev.nokee.language.nativebase.internal.NativeHeaderLanguageBasePlugin;
import dev.nokee.language.nativebase.internal.PublicHeadersComponent;
import dev.nokee.language.nativebase.internal.PublicHeadersPropertyComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareComponent;
import dev.nokee.platform.base.internal.extensionaware.ExtensionAwareMixIn;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import lombok.val;
import org.gradle.api.Project;
import org.gradle.api.file.ConfigurableFileCollection;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.File;

import static dev.nokee.internal.testing.FileSystemMatchers.aFile;
import static dev.nokee.internal.testing.FileSystemMatchers.withAbsolutePath;
import static dev.nokee.internal.testing.GradleNamedMatchers.named;
import static dev.nokee.internal.testing.ProjectMatchers.buildDependencies;
import static dev.nokee.model.internal.state.ModelStates.discover;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static dev.nokee.platform.base.internal.DomainObjectEntities.entityOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.isA;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PluginRequirement.Require(type = NativeHeaderLanguageBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class HasPublicHeadersMixInIntegrationTest {
	ModelNode entity;

	@BeforeEach
	void createSubject(Project project) {
		entity = project.getExtensions().getByType(ModelRegistry.class).instantiate(ModelRegistration.builder()
			.mergeFrom(entityOf(HasPublicHeadersMixIn.class))
			.build());
	}

	@Test
	void hasPublicHeadersTag() {
		assertTrue(entity.hasComponent(typeOf(HasPublicHeadersMixIn.Tag.class)));
	}

	@Test
	void hasPublicHeadersPropertyWhenDiscovered() {
		assertFalse(entity.has(PublicHeadersPropertyComponent.class));
		assertTrue(discover(entity).has(PublicHeadersPropertyComponent.class));
	}

	@Test
	void hasPublicHeadersWhenEntityFinalized() {
		assertFalse(entity.has(PublicHeadersComponent.class));
		assertFalse(discover(entity).has(PublicHeadersComponent.class));
		assertTrue(ModelStates.finalize(entity).has(PublicHeadersComponent.class));
	}

	@Test
	void finalizedComponentContainsPropertyValue() {
		ModelProperties.add(discover(entity).get(PublicHeadersPropertyComponent.class).get(), new File("foo/bar"));
		assertThat(ModelStates.finalize(entity).get(PublicHeadersComponent.class).get(), hasItem(aFile(withAbsolutePath(endsWith("/foo/bar")))));
	}

	@Test
	void finalizedComponentContainsImplicitTaskDependencies(Project project) {
		val generatorTask = project.getTasks().register("generator");
		ModelProperties.add(discover(entity).get(PublicHeadersPropertyComponent.class).get(), generatorTask.map(__ -> new File("foo/bar")));
		assertThat(ModelStates.finalize(entity).get(PublicHeadersComponent.class).get(), buildDependencies(hasItem(named("generator"))));
	}

	@Test
	@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
	void mountPropertyAsExtension() {
		discover(entity).addComponent(tag(ExtensionAwareMixIn.Tag.class));
		assertThat(entity.get(ExtensionAwareComponent.class).get().findByName("publicHeaders"),
			isA(ConfigurableFileCollection.class));
	}

	@Nested
	class HasPublicHeadersProjectionTest implements HasPublicHeadersTester {
		HasPublicHeadersMixIn subject;

		@BeforeEach
		void realizeProjection() {
			subject = ModelNodeUtils.get(ModelStates.realize(entity), HasPublicHeadersMixIn.class);
		}

		@Override
		public HasPublicHeadersMixIn subject() {
			return subject;
		}
	}
}
