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
package dev.nokee.model;

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.model.internal.core.DisplayName;
import dev.nokee.model.internal.core.DisplayNameComponent;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.plugins.ModelBasePlugin;
import dev.nokee.model.internal.registry.ModelLookup;
import lombok.val;
import org.junit.jupiter.api.Test;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

class ProjectDisplayNameIntegrationTest {
	@Test
	void createDisplayNameOnRootProject() {
		val project = ProjectTestUtils.rootProject();
		project.getPluginManager().apply(ModelBasePlugin.class);
		assertThat(project.getExtensions().getByType(ModelLookup.class).get(ModelPath.root()).get(DisplayNameComponent.class).get(),
			equalTo(DisplayName.of("root project 'test'")));
	}

	@Test
	void createDisplayNameOnChildProject() {
		val project = ProjectTestUtils.createChildProject(ProjectTestUtils.rootProject(), "foo");
		project.getPluginManager().apply(ModelBasePlugin.class);
		assertThat(project.getExtensions().getByType(ModelLookup.class).get(ModelPath.root()).get(DisplayNameComponent.class).get(),
			equalTo(DisplayName.of("project ':foo'")));
	}
}
