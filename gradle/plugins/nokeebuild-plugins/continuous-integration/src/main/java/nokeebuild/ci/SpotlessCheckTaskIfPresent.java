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

package nokeebuild.ci;

import org.gradle.api.Project;

import java.util.Optional;
import java.util.concurrent.Callable;

import static java.util.Collections.EMPTY_SET;

final class SpotlessCheckTaskIfPresent implements Callable<Object> {
	private final Project project;

	public SpotlessCheckTaskIfPresent(Project project) {
		this.project = project;
	}

	@Override
	public Object call() throws Exception {
		return Optional.<Object>ofNullable(project.getTasks().findByName("spotlessCheck")).orElse(EMPTY_SET);
	}
}
