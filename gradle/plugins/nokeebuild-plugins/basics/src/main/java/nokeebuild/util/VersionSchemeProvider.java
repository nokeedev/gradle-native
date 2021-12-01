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
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.gradle.util.VersionNumber;

import javax.annotation.Nullable;
import javax.inject.Inject;

@SuppressWarnings("UnstableApiUsage")
final class VersionSchemeProvider {
	public static Provider<VersionScheme> forProject(Project project) {
		return project.getProviders().of(Source.class, spec -> {});
	}

	static abstract class Source implements ValueSource<VersionScheme, ValueSourceParameters.None> {
		@Inject
		public Source() {}

		@Nullable
		@Override
		public VersionScheme obtain() {
			if (System.getProperties().containsKey("release")) {
				return Schemes.Release;
			} else if (System.getProperties().containsKey("milestone")) {
				return Schemes.Milestone;
			} else if (System.getProperties().containsKey("integration")) { // Support multiple version of the same commit
				return Schemes.Integration;
			} else {
				return Schemes.Snapshot;
			}
		}

		private enum Schemes implements VersionScheme {
			Release {
				@Override
				public VersionNumber format(NokeeVersionParameters version) {
					return VersionNumber.parse(version.getBaseVersion().get());
				}
			},
			Milestone {
				@Override
				public VersionNumber format(NokeeVersionParameters version) {
					return VersionNumber.parse(version.getAutoVersion().get());
				}
			},
			Integration {
				@Override
				public VersionNumber format(NokeeVersionParameters version) {
					return VersionNumber.parse(version.getAutoVersion().get() + "-" + version.getBuildTime().get() + "." + version.getQualifier().get());
				}
			},
			Snapshot {
				@Override
				public VersionNumber format(NokeeVersionParameters version) {
					return VersionNumber.parse(version.getBaseVersion().get() + "-SNAPSHOT");
				}
			}
		}
	}
}
