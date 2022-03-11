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
package nokeebuild.licensing;

import org.gradle.api.Action;
import org.gradle.api.plugins.AppliedPlugin;
import org.gradle.plugins.ide.idea.model.IdeaModel;
import org.gradle.plugins.ide.idea.model.IdeaProject;
import org.jetbrains.gradle.ext.CopyrightConfiguration;
import org.jetbrains.gradle.ext.CopyrightProfile;
import org.jetbrains.gradle.ext.ProjectSettings;

final class ConfigureIdeaCopyrightProfileAction implements Action<AppliedPlugin> {
	private final GradleExtensions extensions;

	public ConfigureIdeaCopyrightProfileAction(GradleExtensions extensions) {
		this.extensions = extensions;
	}

	@Override
	public void execute(AppliedPlugin ignored) {
		extensions.configure(IdeaModel.class, this::idea);
	}

	private void idea(IdeaModel idea) {
		idea.project(this::project);
	}

	private void project(IdeaProject project) {
		configureSettings(project, this::settings);
	}

	private void settings(ProjectSettings settings) {
		configureCopyright(settings, this::copyright);
	}

	private void copyright(CopyrightConfiguration copyright) {
		extensions.ifPresent(LicenseExtension.class, extension -> {
			CopyrightProfile defaultProfile = copyright.getProfiles().create(extension.getShortName().get(), profile -> {
				profile.setNotice(extension.getCopyrightFileHeader().get());
				profile.setKeyword("Copyright");
			});
			copyright.setUseDefault(defaultProfile.getName());
		});
	}

	private static void configureSettings(IdeaProject project, Action<? super ProjectSettings> action) {
		GradleExtensions.from(project).configure("settings", action);
	}

	private static void configureCopyright(ProjectSettings settings, Action<? super CopyrightConfiguration> action) {
		GradleExtensions.from(settings).configure("copyright", action);
	}
}
