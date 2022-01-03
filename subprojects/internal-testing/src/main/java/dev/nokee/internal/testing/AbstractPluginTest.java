/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.internal.testing;

import dev.nokee.internal.testing.junit.jupiter.GradleProject;
import dev.nokee.internal.testing.junit.jupiter.GradleTestExtension;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(GradleTestExtension.class)
public class AbstractPluginTest {
	@GradleProject
	protected Project project;

	public Project project() {
		return project;
	}

	protected final void applyPlugin(Class<? extends Plugin<? extends Project>> pluginType) {
		project.getPluginManager().apply(pluginType);
	}

	public final void applyPlugin(String pluginId) {
		project.getPluginManager().apply(pluginId);
	}
}
