package dev.nokee.buildadapter.xcode.internal.plugins;

import dev.nokee.utils.ProviderUtils;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.gradle.api.tasks.InputDirectory;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
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
		val selectedWorkspaceLocation = allWorkspaceLocations.map(new SelectXCWorkspaceLocationTransformation()).map(Path::toFile);
		val workspaceContent = ProviderUtils.forUseAtConfigurationTime(providers.of(XCWorkspaceDataValueSource.class, it -> it.parameters(p -> p.getWorkspaceLocation().fileProvider(selectedWorkspaceLocation))));

		val workspace = workspaceContent.getOrNull();
		if (workspace == null) {
			LOGGER.warn(String.format("The plugin 'dev.nokee.xcode-build-adapter' has no effect on project '%s' because no Xcode workspace were found in '%s'. See https://nokee.fyi/using-xcode-build-adapter for more details.", settings.getGradle(), settings.getSettingsDir()));
			return;
		}
	}

	public static abstract class XCWorkspaceDataValueSource implements ValueSource<String, XCWorkspaceDataValueSource.Parameters> {
		interface Parameters extends ValueSourceParameters {
			@InputDirectory
			DirectoryProperty getWorkspaceLocation();
		}

		@Nullable
		@Override
		public String obtain() {
			if (getParameters().getWorkspaceLocation().isPresent()) {
				try {
					return new String(Files.readAllBytes(getParameters().getWorkspaceLocation().get().getAsFile().toPath().resolve("contents.xcworkspacedata")));
				} catch (IOException e) {
					throw new UncheckedIOException(e);
				}
			} else {
				return null;
			}
		}
	}
}
