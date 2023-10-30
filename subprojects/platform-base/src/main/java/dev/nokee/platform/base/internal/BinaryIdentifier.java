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
package dev.nokee.platform.base.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.HasName;
import dev.nokee.model.internal.names.ElementName;
import lombok.EqualsAndHashCode;

import java.util.Iterator;

import static com.google.common.base.Preconditions.checkArgument;
import static java.util.Objects.requireNonNull;

@EqualsAndHashCode
public final class BinaryIdentifier implements DomainObjectIdentifier, HasName {
	private final ElementName name;
	private final DomainObjectIdentifier ownerIdentifier;

	public BinaryIdentifier(ElementName name, DomainObjectIdentifier ownerIdentifier) {
		requireNonNull(name);
		checkArgument(ownerIdentifier != null, "Cannot construct a task identifier because the owner identifier is null.");
		this.name = name;
		this.ownerIdentifier = ownerIdentifier;
	}

	public ElementName getName() {
		return name;
	}

	public DomainObjectIdentifier getOwnerIdentifier() {
		return ownerIdentifier;
	}


	public static BinaryIdentifier of(DomainObjectIdentifier ownerIdentifier, String name) {
		return new BinaryIdentifier(ElementName.of(name), ownerIdentifier);
	}

	public static BinaryIdentifier of(DomainObjectIdentifier ownerIdentifier, ElementName name) {
		return new BinaryIdentifier(name, ownerIdentifier);
	}

	@Override
	public Iterator<Object> iterator() {
		return ImmutableList.builder().addAll(ownerIdentifier).add(this).build().iterator();
	}
}
