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
package dev.nokee.language.objectivecpp;

import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.internal.testing.junit.jupiter.GradleTestExtension;
import dev.nokee.language.objectivecpp.internal.HasObjectiveCppSourcesMixIn;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourcesComponent;
import dev.nokee.language.objectivecpp.internal.ObjectiveCppSourcesPropertyComponent;
import dev.nokee.language.objectivecpp.internal.plugins.ObjectiveCppLanguageBasePlugin;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelProperties;
import dev.nokee.model.internal.core.ModelRegistration;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.model.internal.state.ModelStates;
import lombok.val;
import org.gradle.api.Project;
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
import static dev.nokee.model.internal.tags.ModelTags.typeOf;
import static dev.nokee.platform.base.internal.DomainObjectEntities.entityOf;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.endsWith;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@PluginRequirement.Require(type = ObjectiveCppLanguageBasePlugin.class)
@ExtendWith(GradleTestExtension.class)
class HasObjectiveCppSourcesMixInIntegrationTest {
	ModelNode entity;

	@BeforeEach
	void createSubject(Project project) {
		entity = project.getExtensions().getByType(ModelRegistry.class).instantiate(ModelRegistration.builder()
			.mergeFrom(entityOf(HasObjectiveCppSourcesMixIn.class))
			.build());
	}

	@Test
	void hasObjectiveCppSourcesTag() {
		assertTrue(entity.hasComponent(typeOf(HasObjectiveCppSourcesMixIn.Tag.class)));
	}

	@Test
	void hasObjectiveCppSourcesPropertyWhenDiscovered() {
		assertFalse(entity.has(ObjectiveCppSourcesPropertyComponent.class));
		assertTrue(discover(entity).has(ObjectiveCppSourcesPropertyComponent.class));
	}

	@Test
	void hasObjectiveCppSourcesWhenEntityFinalized() {
		assertFalse(entity.has(ObjectiveCppSourcesComponent.class));
		assertFalse(discover(entity).has(ObjectiveCppSourcesComponent.class));
		assertTrue(ModelStates.finalize(entity).has(ObjectiveCppSourcesComponent.class));
	}

	@Test
	void finalizedComponentContainsPropertyValue() {
		ModelProperties.add(discover(entity).get(ObjectiveCppSourcesPropertyComponent.class).get(), new File("foo/bar"));
		assertThat(ModelStates.finalize(entity).get(ObjectiveCppSourcesComponent.class).get(), hasItem(aFile(withAbsolutePath(endsWith("/foo/bar")))));
	}

	@Test
	void finalizedComponentContainsImplicitTaskDependencies(Project project) {
		val generatorTask = project.getTasks().register("generator");
		ModelProperties.add(discover(entity).get(ObjectiveCppSourcesPropertyComponent.class).get(), generatorTask.map(__ -> new File("foo/bar")));
		assertThat(ModelStates.finalize(entity).get(ObjectiveCppSourcesComponent.class).get(), buildDependencies(hasItem(named("generator"))));
	}

	@Nested
	class HasObjectiveCppSourcesProjectionTest implements HasObjectiveCppSourcesTester {
		HasObjectiveCppSourcesMixIn subject;

		@BeforeEach
		void realizeProjection() {
			subject = ModelNodeUtils.get(ModelStates.realize(entity), HasObjectiveCppSourcesMixIn.class);
		}

		@Override
		public HasObjectiveCppSourcesMixIn subject() {
			return subject;
		}
	}
}
