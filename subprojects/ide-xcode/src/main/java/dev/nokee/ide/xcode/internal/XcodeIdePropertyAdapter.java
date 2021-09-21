/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.ide.xcode.internal;

import dev.nokee.utils.Cast;
import lombok.SneakyThrows;
import lombok.val;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.util.GradleVersion;

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

	public Provider<String> getGradleIdeProjectName() {
		return getXcodeProperty("GRADLE_IDE_PROJECT_NAME");
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

	public static String adapt(String source, String value) {
		return "-P" + prefixName(source) + "=\"" + value + "\"";
	}
}
