package dev.nokee.ide.xcode.internal;

import org.gradle.api.Project;
import org.gradle.util.GUtil;

import java.util.Arrays;
import java.util.List;

public class XcodeIdePropertyAdapter {
    private final Project project;

    public XcodeIdePropertyAdapter(Project project) {
        this.project = project;
    }

    public String getAction() {
        return getXcodeProperty("ACTION");
    }

    public String getProductName() {
        return getXcodeProperty("PRODUCT_NAME");
    }

    public String getConfiguration() {
        return getXcodeProperty("CONFIGURATION");
    }

    public String getBuiltProductsDir() {
        return getXcodeProperty("BUILT_PRODUCTS_DIR");
    }

    public String getProjectName() {
    	return getXcodeProperty("PROJECT_NAME");
	}

	public String getTargetName() {
    	return getXcodeProperty("TARGET_NAME");
	}

    private String getXcodeProperty(String name) {
        return String.valueOf(GUtil.elvis(project.findProperty(prefixName(name)), ""));
    }

    public static List<String> getAdapterCommandLine() {
        return Arrays.asList(
            toGradleProperty("ACTION"),
            toGradleProperty("PRODUCT_NAME"),
            toGradleProperty("CONFIGURATION"),
            toGradleProperty("BUILT_PRODUCTS_DIR"),
			toGradleProperty("PROJECT_NAME"),
			toGradleProperty("TARGET_NAME")
        );
    }

    private static String toGradleProperty(String source) {
        return "-P" + prefixName(source) + "=\"${" + source + "}\"";
    }

    private static String prefixName(String source) {
        return "dev.nokee.internal.xcode.bridge." + source;
    }
}
