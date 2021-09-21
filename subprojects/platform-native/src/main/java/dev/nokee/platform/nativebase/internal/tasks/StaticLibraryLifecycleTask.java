/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.platform.nativebase.internal.tasks;

import org.gradle.api.DefaultTask;
import org.gradle.api.tasks.Internal;

import static org.gradle.language.base.plugins.LifecycleBasePlugin.BUILD_GROUP;

public class StaticLibraryLifecycleTask extends DefaultTask {
	@Override
	@Internal
	public String getGroup() {
		return BUILD_GROUP;
	}

	@Override
	public void setGroup(String group) {
		// ignores
	}

	@Override
	@Internal
	public String getDescription() {
		// TODO: The description should be derived from the owner (missing concept, but coming soon)
		return "Assembles a static library binary containing the main objects.";
	}

	@Override
	public void setDescription(String description) {
		// ignores
	}
}
