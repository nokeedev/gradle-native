/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.language.swift.internal.plugins;

import com.google.common.collect.ImmutableSet;
import org.gradle.api.file.FileSystemLocation;
import org.gradle.api.provider.Provider;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Callable;

final class DependentImportModules implements Callable<Object> {
	private final Provider<Set<FileSystemLocation>> delegate;

	DependentImportModules(Provider<Set<FileSystemLocation>> delegate) {
		this.delegate = delegate;
	}

	public Set<Path> get() {
		return delegate.get().stream().map(it -> it.getAsFile().toPath()).collect(ImmutableSet.toImmutableSet());
	}

	@Override
	public Object call() throws Exception {
		return delegate;
	}
}
