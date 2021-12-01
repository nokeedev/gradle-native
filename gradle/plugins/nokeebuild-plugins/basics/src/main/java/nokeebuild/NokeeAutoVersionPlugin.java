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
package nokeebuild;

import nokeebuild.util.NokeeVersion;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;

import javax.inject.Inject;

abstract /*final*/ class NokeeAutoVersionPlugin implements Plugin<Project> {
	@Inject
	public NokeeAutoVersionPlugin() {}

	@Override
	public void apply(Project project) {
		if (!isRootProject(project)) {
			throw new UnsupportedOperationException();
		}
		project.getPluginManager().apply("org.shipkit.shipkit-auto-version");

		project.allprojects(proj -> project.setVersion(toStringObjectProvider(NokeeVersion.forProject(project))));

		System.out.println("Version overridden to " + project.getVersion());
	}

	private static boolean isRootProject(Project project) {
		return project.getParent() == null;
	}

	private static Object toStringObjectProvider(Provider<?> provider) {
		return new Object() {
			@Override
			public String toString() {
				return provider.get().toString();
			}
		};
	}
}
