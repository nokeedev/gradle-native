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

public final class KnownModelObjectTypeOf<T> implements TypeFilteringAction.ActionTypeOf<KnownModelObject<T>> {
	private final Class<T> type;

	public KnownModelObjectTypeOf(Class<T> type) {
		this.type = type;
	}

	@Override
	public boolean isInstance(Object obj) {
		if (obj instanceof KnownModelObject) {
			return ((KnownModelObject<?>) obj).getType().isSubtypeOf(type);
		}
		return false;
	}

	@Override
	@SuppressWarnings("unchecked")
	public KnownModelObject<T> cast(Object obj) {
		return (KnownModelObject<T>) obj;
	}

	@Override
	public boolean isSatisfiedBy(ModelObjectIdentity<?> element) {
		return element.getType().isSubtypeOf(type);
	}

	@Override
	public String toString() {
		return "type of " + type.getSimpleName();
	}
}
