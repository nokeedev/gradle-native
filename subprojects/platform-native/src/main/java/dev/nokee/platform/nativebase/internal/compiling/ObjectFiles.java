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
package dev.nokee.platform.nativebase.internal.compiling;

import dev.nokee.model.internal.core.ModelComponent;
import org.gradle.api.provider.Provider;

import java.nio.file.Path;
import java.util.Set;
import java.util.concurrent.Callable;

public final class ObjectFiles implements Callable<Object>, ModelComponent {
	private final Provider<Set<Path>> delegate;

	public ObjectFiles(Provider<Set<Path>> delegate) {
		this.delegate = delegate;
	}

	public Set<Path> get() {
		return delegate.get();
	}

	@Override
	public Object call() throws Exception {
		return delegate;
	}
}
