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
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Scanner;

@SuppressWarnings("UnstableApiUsage")
final class BaseVersionProvider {
	public static Provider<String> forProject(Project project) {
		return project.getProviders().of(Source.class, spec -> {
			spec.getParameters().getVersionFile().fileValue(new File(project.getRootDir(), "version.txt"));
		});
	}

	static abstract class Source implements ValueSource<String, Source.Parameters> {
		interface Parameters extends ValueSourceParameters {
			RegularFileProperty getVersionFile();
		}

		@Inject
		public Source() {}

		@Nullable
		@Override
		public String obtain() {
			try {
				return toString(new FileInputStream(getParameters().getVersionFile().get().getAsFile()));
			} catch (FileNotFoundException e) {
				return null;
			}
		}

		@Nullable
		private static String toString(InputStream inStream) {
			Scanner s = new Scanner(inStream).useDelimiter("\\A");
			if (s.hasNext()) {
				return s.next().trim();
			}
			return null;
		}
	}
}
