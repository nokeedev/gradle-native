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

package dev.nokee.model.internal.decorators;

import dev.nokee.model.internal.names.ElementName;

public final class NestedObjectNamer implements DomainObjectNamer {
	private final DomainObjectNamer delegate;

	public NestedObjectNamer(DomainObjectNamer delegate) {
		this.delegate = delegate;
	}

	@Override
	public ElementName determineName(Decorator.MethodMetadata details) {
		NestedObject nestedObject = (NestedObject) details.getAnnotations().filter(it -> it.annotationType().equals(NestedObject.class)).findFirst().orElse(null);
		if (nestedObject == null || nestedObject.value().length() == 0) {
			return delegate.determineName(details);
		} else {
			if (details.getAnnotations().anyMatch(it -> it.annotationType().equals(MainModelObject.class))) {
				return ElementName.ofMain(nestedObject.value());
			} else {
				return ElementName.of(nestedObject.value());
			}
		}
	}
}
