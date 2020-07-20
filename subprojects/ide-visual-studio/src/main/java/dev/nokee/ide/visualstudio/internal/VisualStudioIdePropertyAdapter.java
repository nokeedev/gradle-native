package dev.nokee.ide.visualstudio.internal;

import dev.nokee.utils.Cast;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.util.GradleVersion;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public abstract class VisualStudioIdePropertyAdapter {
	@Inject
	protected abstract ProviderFactory getProviders();

	public Provider<String> getAction() {
		return getXcodeProperty("Action");
	}

	public Provider<String> getConfiguration() {
		return getXcodeProperty("Configuration");
	}

	public Provider<String> getPlatformName() {
		return getXcodeProperty("PlatformName");
	}

	public Provider<String> getProjectName() {
		return getXcodeProperty("ProjectName");
	}

	public Provider<String> getOutputDirectory() {
		return getXcodeProperty("OutDir");
	}

	@SneakyThrows
	private Provider<String> getXcodeProperty(String name) {
		if (GradleVersion.current().compareTo(GradleVersion.version("6.5")) >= 0) {
			Provider<String> result = getProviders().gradleProperty(prefixName(name));
			val method = Provider.class.getMethod("forUseAtConfigurationTime");
			return Cast.uncheckedCast("using reflection to support newer Gradle", method.invoke(result));
		}
		return getProviders().gradleProperty(prefixName(name));
	}

	public static List<String> getAdapterCommandLine(String action) {
		return Arrays.asList(
			toGradleProperty("Action", action),
			toGradleProperty("OutDir"),
			toGradleProperty("PlatformName"),
			toGradleProperty("Configuration"),
			toGradleProperty("ProjectName")
		);
	}

	private static String toGradleProperty(String source) {
		return "-P" + prefixName(source) + "=$(" + source + ")";
	}

	private static String toGradleProperty(String name, String value) {
		return "-P" + prefixName(name) + "=" + value;
	}

	private static String prefixName(String source) {
		return "dev.nokee.internal.visualStudio.bridge." + source;
	}
}
