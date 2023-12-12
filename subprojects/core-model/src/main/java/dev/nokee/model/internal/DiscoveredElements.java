/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.model.internal;

import dev.nokee.internal.Factory;
import org.gradle.api.Action;

import java.util.function.Consumer;

public class DiscoveredElements {
	public <RegistrableType> ModelObject<RegistrableType> discover(ModelObjectIdentifier identifier, Class<RegistrableType> type, Factory<ModelObject<RegistrableType>> factory) {
		return factory.create();
	}

	public <T> void onRealized(Action<? super T> action, Consumer<? super Action<? super T>> next) {
		next.accept(action);
	}

	public <T> void onKnown(Action<? super KnownModelObject<T>> action, Consumer<? super Action<? super KnownModelObject<T>>> next) {
		next.accept(action);
	}

	public <T> void onFinalized(Action<? super T> action, Consumer<? super Action<? super T>> next) {
		next.accept(action);
	}
}
