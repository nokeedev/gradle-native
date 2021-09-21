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
package dev.nokee.ide.visualstudio.internal;

import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;

import static dev.nokee.utils.ProviderUtils.forUseAtConfigurationTime;

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

	public Provider<String> getGradleIdeProjectName() {
		return getXcodeProperty("GRADLE_IDE_PROJECT_NAME");
	}

	private Provider<String> getXcodeProperty(String name) {
		return forUseAtConfigurationTime(getProviders().gradleProperty(prefixName(name)));
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

	public static String adapt(String source, String value) {
		return "-P" + prefixName(source) + "=\"" + value + "\"";
	}
}
