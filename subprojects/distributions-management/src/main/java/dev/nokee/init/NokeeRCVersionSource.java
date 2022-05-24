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
package dev.nokee.init;

import org.gradle.api.Action;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.ValueSource;
import org.gradle.api.provider.ValueSourceParameters;
import org.gradle.api.provider.ValueSourceSpec;

import javax.annotation.Nullable;
import javax.inject.Inject;
import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;

abstract class NokeeRCVersionSource implements ValueSource<NokeeVersion, NokeeRCVersionSource.Parameters> {
	interface Parameters extends ValueSourceParameters {
		RegularFileProperty getNokeeRCFile();
	}

	@Inject
	public NokeeRCVersionSource() {}

	@Nullable
	@Override
	public NokeeVersion obtain() {
		final Path rcFile = getParameters().getNokeeRCFile().get().getAsFile().toPath();
		if (Files.exists(rcFile)) {
			try {
				return NokeeVersion.version(new String(Files.readAllBytes(rcFile)));
			} catch (IOException e) {
				throw new UncheckedIOException(e);
			}
		}
		return null;
	}

	public static Action<ValueSourceSpec<Parameters>> rcFile(File baseDirectory) {
		return spec -> spec.parameters(parameters -> parameters.getNokeeRCFile().fileValue(new File(baseDirectory, ".nokeerc")));
	}
}
