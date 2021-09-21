/*
 * Copyright 2020 the original author or authors.
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

import dev.nokee.model.DomainObjectIdentifier;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.ToString;
import lombok.Value;

@ToString
@EqualsAndHashCode
public class DomainObjectCreated<T> implements DomainObjectEvent {
	@Getter private final DomainObjectIdentifier identifier;
	@Getter private final T object;

	public DomainObjectCreated(TypeAwareDomainObjectIdentifier<?> identifier, T object) {
		assert identifier.getType().isInstance(object);
		this.identifier = identifier;
		this.object = object;
	}

	public DomainObjectCreated(DomainObjectIdentifier identifier, T object) {
		this.identifier = identifier;
		this.object = object;
	}
}
