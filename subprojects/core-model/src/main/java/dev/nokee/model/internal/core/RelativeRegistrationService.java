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
package dev.nokee.model.internal.core;

import dev.nokee.model.internal.registry.ModelRegistry;

public final class RelativeRegistrationService {
	private final ModelPath path;
	private final ModelRegistry modelRegistry;

	public RelativeRegistrationService(ModelPath path, ModelRegistry modelRegistry) {
		this.path = path;
		this.modelRegistry = modelRegistry;
	}

	public <T> ModelElement register(NodeRegistration<T> registration) {
		return modelRegistry.register(registration.scope(path));
	}
}
