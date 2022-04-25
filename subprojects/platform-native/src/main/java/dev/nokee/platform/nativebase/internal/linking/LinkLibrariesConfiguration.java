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
package dev.nokee.platform.nativebase.internal.linking;

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.nativebase.internal.Configurable;
import dev.nokee.platform.nativebase.internal.LinkedEntity;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;

public final class LinkLibrariesConfiguration implements Configurable<Configuration>, LinkedEntity, ModelComponent {
	private final DomainObjectProvider<Configuration> delegate;

	public LinkLibrariesConfiguration(DomainObjectProvider<Configuration> delegate) {
		this.delegate = delegate;
	}

	public void configure(Action<? super Configuration> action) {
		delegate.configure(action);
	}

	@Override
	public ModelNode get() {
		return ModelNodes.of(delegate);
	}
}
