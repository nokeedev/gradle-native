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
package dev.nokee.gradle.internal;

import dev.nokee.gradle.GradleProjectVersion;
import org.gradle.api.Project;

import javax.inject.Inject;

class GradleProjectVersionImpl implements GradleProjectVersion {
	private final Project project;

	@Inject
	public GradleProjectVersionImpl(Project project) {
		this.project = project;
	}

	@Override
	public String get() {
		return project.getVersion().toString();
	}
}
