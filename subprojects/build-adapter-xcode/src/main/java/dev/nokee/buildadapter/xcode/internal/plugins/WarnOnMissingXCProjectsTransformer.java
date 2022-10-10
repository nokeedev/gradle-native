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

import com.google.common.collect.Iterables;
import dev.nokee.xcode.XCProjectReference;
import org.gradle.api.Transformer;
import org.gradle.api.logging.Logger;
import org.gradle.api.logging.Logging;

import java.nio.file.Path;
import java.util.function.Consumer;

public final class WarnOnMissingXCProjectsTransformer implements Transformer<Iterable<XCProjectReference>, Iterable<XCProjectReference>> {
	private static final Logger LOGGER = Logging.getLogger(WarnOnMissingXCProjectsTransformer.class);
	private final Path basePath;
	private final Consumer<? super String> logger;

	public WarnOnMissingXCProjectsTransformer(Path basePath) {
		this(basePath, LOGGER::warn);
	}

	public WarnOnMissingXCProjectsTransformer(Path basePath, Consumer<? super String> logger) {
		this.basePath = basePath;
		this.logger = logger;
	}

	@Override
	public Iterable<XCProjectReference> transform(Iterable<XCProjectReference> allProjects) {
		if (Iterables.isEmpty(allProjects)) {
			logger.accept(String.format("The plugin 'dev.nokee.xcode-build-adapter' has no effect because no Xcode workspace or project were found in '%s'. See https://nokee.fyi/using-xcode-build-adapter for more details.", basePath));
		}
		return allProjects;
	}
}
