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
package nokeebuild.util;

import org.gradle.api.Project;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.services.BuildService;
import org.gradle.api.services.BuildServiceParameters;
import org.gradle.util.VersionNumber;

import javax.inject.Inject;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.Objects;

import static java.time.temporal.ChronoField.*;

@SuppressWarnings("UnstableApiUsage")
public final class NokeeVersion {
	private static final DateTimeFormatter BUILD_TIME_FORMATTER = new DateTimeFormatterBuilder()
		.appendValue(YEAR, 4)
		.appendValue(MONTH_OF_YEAR, 2)
		.appendValue(DAY_OF_MONTH, 2)
		.appendValue(HOUR_OF_DAY, 2)
		.appendValue(MINUTE_OF_HOUR, 2)
		.toFormatter();

	public static Provider<NokeeVersion> forProject(Project project) {
		return project.getGradle().getSharedServices().registerIfAbsent("version", VersionService.class, spec -> {
			spec.parameters(param -> {
				param.getBaseVersion().set(BaseVersionProvider.forProject(project).forUseAtConfigurationTime());
				param.getBuildTime().set(BUILD_TIME_FORMATTER.format(ZonedDateTime.now()));
				param.getQualifier().set(GitHashProvider.forProject(project).forUseAtConfigurationTime());
				param.getAutoVersion().set(Objects.toString(project.getVersion(), null));
				param.getVersionScheme().set(VersionSchemeProvider.forProject(project).forUseAtConfigurationTime());
			});
		}).map(VersionService::get);
	}

	private final VersionNumber delegate;

	private NokeeVersion(VersionNumber delegate) {
		this.delegate = delegate;
	}

	@Override
	public String toString() {
		return delegate.toString();
	}

	static abstract /*final*/ class VersionService implements BuildService<VersionService.Parameters> {
		interface Parameters extends BuildServiceParameters, NokeeVersionParameters {
			Property<String> getBaseVersion();
			Property<String> getQualifier();
			Property<String> getBuildTime();
			Property<String> getAutoVersion();
			Property<VersionScheme> getVersionScheme();
		}

		private final Object lock = new Object();
		private NokeeVersion version = null;

		@Inject
		public VersionService() {}

		public NokeeVersion get() {
			if (version == null) {
				synchronized (lock) {
					if (version == null) {
						version = new NokeeVersion(getParameters().getVersionScheme().get().format(getParameters()));
					}
				}
			}
			return version;
		}
	}
}
