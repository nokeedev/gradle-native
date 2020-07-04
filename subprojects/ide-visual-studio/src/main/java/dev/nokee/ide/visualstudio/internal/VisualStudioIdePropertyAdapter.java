package dev.nokee.ide.visualstudio.internal;

import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

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

    private Provider<String> getXcodeProperty(String name) {
        return getProviders().gradleProperty(prefixName(name));
    }

    public static List<String> getAdapterCommandLine() {
        return Arrays.asList(
            toGradleProperty("OutDir"),
            toGradleProperty("PlatformName"),
            toGradleProperty("Configuration"),
			toGradleProperty("ProjectName")
        );
    }

    private static String toGradleProperty(String source) {
        return "-P" + prefixName(source) + "=$(" + source + ")";
    }

    private static String prefixName(String source) {
        return "dev.nokee.internal.visualStudio.bridge." + source;
    }
}
