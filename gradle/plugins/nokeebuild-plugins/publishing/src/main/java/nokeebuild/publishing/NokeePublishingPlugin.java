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
package nokeebuild.publishing;

import org.gradle.api.Plugin;
import org.gradle.api.Project;

import javax.inject.Inject;

import static nokeebuild.publishing.PublishingBasePlugin.forEachMavenPom;
import static nokeebuild.publishing.PublishingBasePlugin.publishing;

abstract class NokeePublishingPlugin implements Plugin<Project> {
	@Inject
	public NokeePublishingPlugin() {}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(PublishingBasePlugin.class);
		project.getPluginManager().apply("maven-publish");

		publishing(project, forEachMavenPom((publication, pom) -> {
			pom.getInceptionYear().set("2020");
			pom.getUrl().set("https://nokee.dev");
			pom.scm(scm -> {
				scm.getConnection().set("scm:git:git://github.com/nokeedev/gradle-native.git");
				scm.getDeveloperConnection().set("scm:git:ssh://github.com:nokeedev/gradle-native.git");
				scm.getUrl().set("http://github.com/nokeedev/gradle-native/tree/main");
			});
		}));
	}
}
