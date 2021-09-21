package dev.nokeebuild.licensing;

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
