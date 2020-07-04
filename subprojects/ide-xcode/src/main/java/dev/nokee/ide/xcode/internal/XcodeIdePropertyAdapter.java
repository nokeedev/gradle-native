package dev.nokee.ide.xcode.internal;

import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

public abstract class XcodeIdePropertyAdapter {
	@Inject
	protected abstract ProviderFactory getProviders();

	public Provider<String> getAction() {
		return getXcodeProperty("ACTION");
	}

	public Provider<String> getProductName() {
		return getXcodeProperty("PRODUCT_NAME");
	}

	public Provider<String> getConfiguration() {
		return getXcodeProperty("CONFIGURATION");
	}

	public Provider<String> getBuiltProductsDir() {
		return getXcodeProperty("BUILT_PRODUCTS_DIR");
	}

	public Provider<String> getProjectName() {
		return getXcodeProperty("PROJECT_NAME");
	}

	public Provider<String> getTargetName() {
		return getXcodeProperty("TARGET_NAME");
	}

	private Provider<String> getXcodeProperty(String name) {
		return getProviders().gradleProperty(prefixName(name));
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
