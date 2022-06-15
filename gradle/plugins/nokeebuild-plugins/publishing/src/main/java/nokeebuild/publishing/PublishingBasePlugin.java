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

import nokeebuild.licensing.LicenseExtension;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.maven.MavenPom;
import org.gradle.api.publish.maven.MavenPublication;
import org.gradle.api.publish.plugins.PublishingPlugin;

import javax.inject.Inject;
import java.util.Objects;
import java.util.function.BiConsumer;

abstract class PublishingBasePlugin implements Plugin<Project> {
	private final ProviderFactory providers;

	@Inject
	public PublishingBasePlugin(ProviderFactory providers) {
		this.providers = providers;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply(PublishingPlugin.class);

		publishing(project, forEachMavenPom((publication, pom) -> {
			pom.getName().set(providers.provider(() -> publication.getGroupId() + ":" + publication.getArtifactId()));
			pom.getDescription().set(providers.provider(project::getDescription));

			pom.developers(developers -> {
				developers.developer(developer -> {
					developer.getName().set("Daniel Lacasse");
					developer.getId().set("lacasseio");
				});
			});
		}));

		project.getPluginManager().withPlugin("java-gradle-plugin", __ -> {
			publishing(project, forEachMavenPom((publication, pom) -> {
				pom.licenses(licenses -> {
					licenses.license(licenseSpec -> {
						licenseSpec.getName().set(license(project).flatMap(LicenseExtension::getDisplayName));
						licenseSpec.getUrl().set(license(project).flatMap(LicenseExtension::getLicenseUrl).map(Objects::toString));
					});
				});
			}));
		});

		// Because... don't get me started...
		project.getPluginManager().withPlugin("java-gradle-plugin", __ -> {
			project.afterEvaluate(proj -> {
				publishing(proj, forEachMavenPom((publication, pom) -> {
					pom.getName().set(providers.provider(() -> publication.getGroupId() + ":" + publication.getArtifactId()));
					pom.getDescription().set(providers.provider(project::getDescription));
				}));
			});
		});
	}

	public static Action<PublishingExtension> forEachMavenPom(BiConsumer<? super MavenPublication, ? super MavenPom> action) {
		return publishing -> {
			publishing.publications(publications -> {
				publications.withType(MavenPublication.class).configureEach(publication -> {
					publication.pom(pom -> action.accept(publication, pom));
				});
			});
		};
	}

	public static void publishing(Project project, Action<? super PublishingExtension> action) {
		action.execute((PublishingExtension) project.getExtensions().getByName("publishing"));
	}

	private static Provider<LicenseExtension> license(Project project) {
		return project.provider(() -> (LicenseExtension) project.getExtensions().getByName("license"));
	}
}
