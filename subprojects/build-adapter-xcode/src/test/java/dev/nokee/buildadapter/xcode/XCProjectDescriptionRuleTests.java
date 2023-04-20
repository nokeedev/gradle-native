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
package dev.nokee.buildadapter.xcode;

import dev.nokee.buildadapter.xcode.internal.XCProjectReferenceToStringer;
import dev.nokee.buildadapter.xcode.internal.components.GradleProjectTag;
import dev.nokee.buildadapter.xcode.internal.components.XCProjectComponent;
import dev.nokee.buildadapter.xcode.internal.rules.XCProjectDescriptionRule;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.platform.base.internal.project.ProjectDescriptionComponent;
import dev.nokee.xcode.XCProjectReference;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import static dev.nokee.model.fixtures.ModelEntityTestUtils.newEntity;
import static dev.nokee.model.internal.tags.ModelTags.tag;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class XCProjectDescriptionRuleTests {
	@Mock XCProjectReferenceToStringer service;
	@InjectMocks XCProjectDescriptionRule subject;
	@Mock XCProjectReference projectReference;
	ModelNode projectEntity = newEntity();

	@BeforeEach
	void createEntities() {
		projectEntity.addComponent(tag(GradleProjectTag.class));
		projectEntity.addComponent(new XCProjectComponent(projectReference));
	}

	@Test
	void addsProjectDescription() {
		Mockito.when(service.toString(projectReference)).thenReturn("Xcode project 'my/Project.xcodeproj'");
		subject.execute(projectEntity);

		assertThat(projectEntity.get(ProjectDescriptionComponent.class).get(), equalTo("Xcode project 'my/Project.xcodeproj'"));
	}
}
