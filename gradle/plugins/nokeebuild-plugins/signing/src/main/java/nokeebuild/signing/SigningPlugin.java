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
package nokeebuild.signing;

import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.publish.PublishingExtension;
import org.gradle.api.publish.plugins.PublishingPlugin;
import org.gradle.plugins.signing.SigningExtension;

import javax.inject.Inject;

abstract class SigningPlugin implements Plugin<Project> {
	private final ProviderFactory providers;

	@Inject
	public SigningPlugin(ProviderFactory providers) {
		this.providers = providers;
	}

	@Override
	public void apply(Project project) {
		project.getPluginManager().apply("signing");

		project.getPlugins().withType(PublishingPlugin.class, new Once<>(__ -> {
			signing(project, extension -> {
				extension.sign(publishing(project).map(PublishingExtension::getPublications).get());
				ifNotPresent(providers.gradleProperty("signing.secretKeyRingFile"), () -> {
					final String signingKeyId = providers.gradleProperty("signing.keyId").getOrNull();
					final String signingKey = providers.gradleProperty("signing.key").getOrNull();
					final String signingPassword = providers.gradleProperty("signing.password").getOrNull();
					extension.useInMemoryPgpKeys(signingKeyId, signingKey, signingPassword);
				});
			});
		}));
	}

	private static void signing(Project project, Action<? super SigningExtension> action) {
		action.execute((SigningExtension) project.getExtensions().getByName("signing"));
	}

	private static Provider<PublishingExtension> publishing(Project project) {
		return project.getProviders().provider(() -> (PublishingExtension) project.getExtensions().getByName("publishing"));
	}

	private static void ifNotPresent(Provider<?> self, Runnable action) {
		final Object value = self.getOrNull();
		if (value == null) {
			action.run();
		}
	}
}
