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
package dev.nokee.language.nativebase.internal;

import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.model.internal.core.LinkedEntity;
import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelElement;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.model.internal.core.ModelNodes;
import org.gradle.api.Action;

public final class NativeCompileTask implements ModelComponent, LinkedEntity {
	private final ModelElement delegate;

	NativeCompileTask(ModelElement delegate) {
		this.delegate = delegate;
	}

	public <T extends SourceCompile> void configure(Class<T> type, Action<? super T> action) {
		delegate.configure(type, action);
	}

	@Override
	public ModelNode get() {
		return ModelNodes.of(delegate);
	}
}
