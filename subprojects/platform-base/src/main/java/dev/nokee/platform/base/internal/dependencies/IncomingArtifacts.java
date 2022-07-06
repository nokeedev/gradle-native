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
package dev.nokee.platform.base.internal.dependencies;

import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.artifacts.ArtifactView;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.file.FileCollection;

final class IncomingArtifacts {
	private final NamedDomainObjectProvider<Configuration> delegate;

	public IncomingArtifacts(NamedDomainObjectProvider<Configuration> delegate) {
		this.delegate = delegate;
	}

	public FileCollection get() {
		return delegate.get().getIncoming().getFiles();
	}

	public FileCollection getAsLenient() {
		return delegate.get().getIncoming().artifactView(this::asLenient).getFiles();
	}

	private void asLenient(ArtifactView.ViewConfiguration view) {
		view.setLenient(true);
	}
}
