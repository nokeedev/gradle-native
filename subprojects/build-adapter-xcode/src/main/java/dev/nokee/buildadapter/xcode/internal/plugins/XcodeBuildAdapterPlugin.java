package dev.nokee.buildadapter.xcode.internal.plugins;

import dev.nokee.utils.ProviderUtils;
import dev.nokee.xcode.XCWorkspace;
import dev.nokee.xcode.XCWorkspaceReference;
import lombok.val;
import org.gradle.api.Plugin;
import org.gradle.api.initialization.Settings;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

import javax.annotation.Nullable;
import javax.inject.Inject;

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
			LOGGER.warn(String.format("The plugin 'dev.nokee.xcode-build-adapter' has no effect on project '%s' because no Xcode workspace were found in '%s'. See https://nokee.fyi/using-xcode-build-adapter for more details.", settings.getGradle(), settings.getSettingsDir()));
			return;
		}
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
