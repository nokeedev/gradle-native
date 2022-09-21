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

import dev.nokee.buildadapter.xcode.internal.GradleProjectPathService;
import dev.nokee.buildadapter.xcode.internal.components.GradleProjectPathComponent;
import dev.nokee.buildadapter.xcode.internal.components.GradleProjectTag;
import dev.nokee.buildadapter.xcode.internal.components.XCProjectComponent;
import dev.nokee.buildadapter.xcode.internal.rules.XcodeProjectPathRule;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.samples.xcode.EmptyProject;
import dev.nokee.xcode.XCProjectReference;
import lombok.val;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.nio.file.Path;
import java.nio.file.Paths;

import static dev.nokee.model.internal.tags.ModelTags.tag;
import static org.gradle.util.Path.path;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

@ExtendWith(MockitoExtension.class)
class XcodeProjectPathRuleTests {
	@TempDir static Path testDirectory;
	@Mock GradleProjectPathService projectPathService;
	@InjectMocks XcodeProjectPathRule subject;
	static XCProjectReference reference;

	@BeforeAll
	static void createXcodeProject() {
		new EmptyProject().writeToProject(testDirectory);
		reference = XCProjectReference.of(testDirectory.resolve("Empty.xcodeproj"));
	}

	@Test
	void addGradleProjectPathComponentFromProjectPathService() {
		val entity = new ModelNode();
		entity.addComponent(tag(GradleProjectTag.class));
		entity.addComponent(new XCProjectComponent(reference));
		Mockito.when(projectPathService.toProjectPath(reference)).thenReturn(path(":foo:bar"));
		subject.execute(entity);

		assertThat(entity.get(GradleProjectPathComponent.class), equalTo(new GradleProjectPathComponent(path(":foo:bar"))));
	}
}
