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

import lombok.val;

import java.io.File;
import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.function.Consumer;

final class CachingCocoaPods implements CocoaPods {
	private final CocoaPods delegate;
	private final File cacheFile;

	public CachingCocoaPods(CocoaPods delegate, File cacheFile) {
		this.delegate = delegate;
		this.cacheFile = cacheFile;
	}

	@Override
	public Podfile getPodfile() {
		return delegate.getPodfile();
	}

	@Override
	public boolean isEnabled() {
		return delegate.isEnabled();
	}

	@Override
	public boolean isOutOfDate() {
		if (delegate.isOutOfDate()) {
			return true;
		} else {
			val podfileContent = getPodfile().getContent();
			val previousPodfileContent = load().getContent();
			return !podfileContent.equals(previousPodfileContent);
		}
	}

	public Podfile load() {
		return Podfile.of(cacheFile);
	}

	private void clear() {
		try {
			if (Files.exists(cacheFile.toPath())) {
				Files.delete(cacheFile.toPath());
			}
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private void update() {
		try {
			Files.copy(getPodfile().getLocation(), cacheFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	@Override
	public void ifOutOfDate(Consumer<? super Object> consumer) {
		if (!isEnabled()) {
			clear();
		} else if (isOutOfDate()) {
			consumer.accept(null);
			update();
		}
	}
}
