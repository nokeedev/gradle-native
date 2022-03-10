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
package nokeedocs;

import net.nokeedev.jbake.JBakeExtension;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.file.CopySpec;

import static net.nokeedev.jbake.JBakeExtension.jbake;
import static dev.gradleplugins.dockit.manual.UserManualExtension.userManual;

class NokeeUserManualPlugin implements Plugin<Project> {
	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("dev.gradleplugins.documentation.user-manual");

		userManual(project, manual -> {
			manual.getManualDirectory().set(project.getLayout().getProjectDirectory().dir("src/docs/manual"));
		});

		project.getPluginManager().withPlugin("net.nokeedev.jbake-site", ignored -> {
			userManual(project, new RegisterJBakeExtension<>(project));
			userManual(project, new JBakeAssets<>(new AllCompiledDots<>(project)));
			userManual(project, new JBakeAssets<>(new AllPngs<>()));
			userManual(project, new JBakeAssets<>(new AllGifs<>()));
			userManual(project, new JBakeContent<>(new ManualContent()));

			jbake(project, new Action<JBakeExtension>() {
				@Override
				public void execute(JBakeExtension extension) {
					extension.getContent().from(extension.sync("manualContent", toManual(spec -> {
						spec.from(userManual(project).flatMap(it -> jbake(it).getContent().getElements()));
					})));
					extension.getContent().from("src/docs/index.adoc");
					extension.getContent().from("src/docs/release-notes.adoc");
					extension.getAssets().from(extension.sync("manualAssets", toManual(spec -> spec.from(userManual(project).flatMap(it -> jbake(it).getAssets().getElements())))));
					extension.getTemplates().from(userManual(project).flatMap(it -> jbake(it).getTemplates().getElements()));
					extension.getConfigurations().putAll(userManual(project).flatMap(it -> jbake(it).getConfigurations()));
				}

				private Action<CopySpec> toManual(Action<? super CopySpec> action) {
					return spec -> spec.into("manual", action);
				}
			});
		});
	}
}
