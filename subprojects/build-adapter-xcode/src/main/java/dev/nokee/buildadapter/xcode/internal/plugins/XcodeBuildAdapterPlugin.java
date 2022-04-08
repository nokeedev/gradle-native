package dev.nokee.buildadapter.xcode.internal.plugins;

import dev.nokee.utils.ProviderUtils;
import dev.nokee.xcode.XCProjectReference;
import dev.nokee.xcode.XCWorkspace;
import dev.nokee.xcode.XCWorkspaceReference;
import lombok.val;
import org.apache.commons.io.FilenameUtils;
import org.gradle.api.Action;
import org.gradle.api.Plugin;
import org.gradle.api.Project;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.nio.file.Path;

class XcodeBuildAdapterPlugin implements Plugin<Settings> {
	private static final Logger LOGGER = Logging.getLogger(XcodeBuildAdapterPlugin.class);
	private final ProviderFactory providers;

	@Inject
	public XcodeBuildAdapterPlugin(ProviderFactory providers) {
		this.providers = providers;
	}

	@Override
	public void apply(Settings settings) {
		val allWorkspaceLocations = ProviderUtils.forUseAtConfigurationTime(providers.of(AllXCWorkspaceLocationsValueSource.class, it -> it.parameters(p -> p.getSearchDirectory().set(settings.getSettingsDir()))));
		val selectedWorkspaceLocation = allWorkspaceLocations.map(new SelectXCWorkspaceLocationTransformation());

		val workspace = ProviderUtils.forUseAtConfigurationTime(providers.of(XCWorkspaceDataValueSource.class, it -> it.parameters(p -> p.getWorkspace().set(selectedWorkspaceLocation)))).getOrNull();
		if (workspace == null) {
			settings.getGradle().rootProject(rootProject -> {
				val allProjectLocations = ProviderUtils.forUseAtConfigurationTime(providers.of(AllXCProjectLocationsValueSource.class, it -> it.parameters(p -> p.getSearchDirectory().set(settings.getSettingsDir()))));
				val selectedProjectLocation = allProjectLocations.map(new SelectXCProjectLocationTransformation());

				val project = selectedProjectLocation.getOrNull();
				if (project == null) {
					LOGGER.warn(String.format("The plugin 'dev.nokee.xcode-build-adapter' has no effect on project '%s' because no Xcode workspace or project were found in '%s'. See https://nokee.fyi/using-xcode-build-adapter for more details.", settings.getGradle(), settings.getSettingsDir()));
				} else {
					LOGGER.quiet("Taking this project " + project.getLocation());
					forXcodeProject(project).execute(rootProject);
				}
			});
		} else {
			for (XCProjectReference project : workspace.getProjectLocations()) {
				val relativePath = settings.getSettingsDir().toPath().relativize(project.getLocation());
				val projectPath = asProjectPath(relativePath);
				LOGGER.info(String.format("Mapping Xcode project '%s' to Gradle project '%s'.", relativePath, projectPath));
				settings.include(projectPath);

				settings.getGradle().rootProject(rootProject -> {
					rootProject.project(projectPath, forXcodeProject(project));
				});
			}
		}
	}

	private String asProjectPath(Path relativePath) {
		return FilenameUtils.removeExtension(relativePath.toString()).replace('/', ':');
	}

	private static Action<Project> forXcodeProject(XCProjectReference reference) {
		return project -> { /* do nothing */ };
	}

	public static abstract class XCWorkspaceDataValueSource implements ValueSource<XCWorkspace, XCWorkspaceDataValueSource.Parameters> {
		interface Parameters extends ValueSourceParameters {
			Property<XCWorkspaceReference> getWorkspace();
		}

		@Nullable
		@Override
		public XCWorkspace obtain() {
			if (getParameters().getWorkspace().isPresent()) {
				return getParameters().getWorkspace().get().load();
			} else {
				return null;
			}
		}
	}
}
