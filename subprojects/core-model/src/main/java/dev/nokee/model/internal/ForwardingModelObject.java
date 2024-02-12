/*
 * Copyright 2024 the original author or authors.
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

import dev.nokee.model.internal.type.ModelType;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectProvider;

public abstract class ForwardingModelObject<ObjectType> implements ModelObject<ObjectType> {
	protected abstract ModelObject<ObjectType> delegate();

	@Override
	public ModelObjectIdentifier getIdentifier() {
		return delegate().getIdentifier();
	}

	@Override
	public ModelType<?> getType() {
		return delegate().getType();
	}

	@Override
	public NamedDomainObjectProvider<ObjectType> asProvider() {
		return delegate().asProvider();
	}

	@Override
	public ObjectType get() {
		return delegate().get();
	}

	@Override
	public ModelObject<ObjectType> configure(Action<? super ObjectType> configureAction) {
		return delegate().configure(configureAction);
	}

	@Override
	public ModelObject<ObjectType> whenFinalized(Action<? super ObjectType> finalizeAction) {
		return delegate().whenFinalized(finalizeAction);
	}

	@Override
	public String getName() {
		return delegate().getName();
	}
}
