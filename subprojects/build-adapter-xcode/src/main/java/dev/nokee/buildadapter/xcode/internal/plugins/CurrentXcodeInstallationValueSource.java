/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.buildadapter.xcode.internal.plugins;

import dev.nokee.core.exec.LoggingEngine;
import dev.nokee.core.exec.ProcessBuilderEngine;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.nio.file.FileSystems;
import java.nio.file.Path;

@SuppressWarnings("UnstableApiUsage")
public abstract class CurrentXcodeInstallationValueSource implements ValueSource<XcodeInstallation, CurrentXcodeInstallationValueSource.Parameters> {
	private final XcodeDeveloperDirectoryLocator developerDirLocator;
	private final XcodeVersionFinder versionFinder;

	public interface Parameters extends ValueSourceParameters {}

	@Inject
	public CurrentXcodeInstallationValueSource() {
		this(new XcodeDeveloperDirectoryEnvironmentVariableLocator(FileSystems.getDefault(), () -> System.getenv("DEVELOPER_DIR"), new XcodeDeveloperDirectorySystemApplicationsLocator(FileSystems.getDefault(), new XcodeDeveloperDirectoryXcodeSelectLocator(LoggingEngine.wrap(new ProcessBuilderEngine())))), new XcodeVersionPropertyListFinder());
	}

	public CurrentXcodeInstallationValueSource(XcodeDeveloperDirectoryLocator developerDirLocator, XcodeVersionFinder versionFinder) {
		this.developerDirLocator = developerDirLocator;
		this.versionFinder = versionFinder;
	}

	@Nullable
	@Override
	public XcodeInstallation obtain() {
		final Path developerDir = developerDirLocator.locate();
		final String version = versionFinder.find(developerDir);
		return new DefaultXcodeInstallation(version, developerDir);
	}

}
