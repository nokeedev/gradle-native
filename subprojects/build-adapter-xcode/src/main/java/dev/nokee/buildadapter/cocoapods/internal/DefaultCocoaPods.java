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
package dev.nokee.buildadapter.cocoapods.internal;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.function.Consumer;

final class DefaultCocoaPods implements CocoaPods {
	private final File workingDirectory;

	private DefaultCocoaPods(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	private Path getPodfileLockFile() {
		return new File(workingDirectory, "Podfile.lock").toPath();
	}

	private Path getManifestLockFile() {
		return new File(workingDirectory, "Pods/manifest.lock").toPath();
	}

	private Path getPodfileFile() {
		return new File(workingDirectory, "Podfile").toPath();
	}

	public static DefaultCocoaPods inDirectory(File workingDirectory) {
		return new DefaultCocoaPods(workingDirectory);
	}

	@Override
	public boolean isOutOfDate() {
		if (Files.notExists(getPodfileLockFile())) {
			return true;
		} else if (Files.notExists(getManifestLockFile())) {
			return true;
		}

		try {
			byte[] podfileLockContent = Files.readAllBytes(getPodfileLockFile());
			byte[] manifestLockContent = Files.readAllBytes(getManifestLockFile());
			if (!Arrays.equals(podfileLockContent, manifestLockContent)) {
				return true;
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}

		return false;
	}

	@Override
	public Podfile getPodfile() {
		return Podfile.of(getPodfileFile().toFile());
	}

	@Override
	public boolean isEnabled() {
		return Files.exists(getPodfileFile());
	}

	@Override
	public void ifOutOfDate(Consumer<? super Object> consumer) {
		if (isOutOfDate()) {
			consumer.accept(null);
		}
	}
}
