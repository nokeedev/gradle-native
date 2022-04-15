/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.platform.nativebase.internal.plugins.testers;

import dev.nokee.internal.testing.util.ProjectTestUtils;
import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.ComponentContainer;
import org.gradle.api.Project;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collections;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.isA;

public abstract class MainComponentPluginTester {
	protected abstract Class<? extends Component> getComponentType();
	protected abstract String getQualifiedPluginId();

	private final Project project = ProjectTestUtils.rootProject();

	@BeforeEach
	void applyPlugin() {
		project.apply(Collections.singletonMap("plugin", getQualifiedPluginId()));
	}

	@Test
	void createMainComponent() {
		Component mainComponent = project.getExtensions().getByType(ComponentContainer.class).named("main").get();

		assertThat(mainComponent, isA(getComponentType()));
	}
}
