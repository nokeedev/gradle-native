package dev.nokee.ide.visualstudio.internal;

import org.gradle.api.Project;
import org.gradle.util.GUtil;

import java.util.Arrays;
import java.util.List;

public class VisualStudioIdePropertyAdapter {
    private final Project project;

    public VisualStudioIdePropertyAdapter(Project project) {
        this.project = project;
    }

    public String getConfiguration() {
        return getXcodeProperty("Configuration");
    }

    public String getPlatformName() {
    	return getXcodeProperty("PlatformName");
	}

    public String getBuiltProductsDir() {
        return getXcodeProperty("BUILT_PRODUCTS_DIR");
    }

    public String getProjectName() {
    	return getXcodeProperty("ProjectName");
	}

	public String getTargetName() {
    	return getXcodeProperty("TARGET_NAME");
	}

	public String getIntermediateDirectory() {
    	return getXcodeProperty("OutDir");
	}

    private String getXcodeProperty(String name) {
        return String.valueOf(GUtil.elvis(project.findProperty(prefixName(name)), ""));
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
