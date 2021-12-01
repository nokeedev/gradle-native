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
package dev.nokee.model.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.DomainObjectIdentifier;
import lombok.EqualsAndHashCode;

import java.util.Iterator;

@EqualsAndHashCode
public final class ModelPropertyIdentifier implements DomainObjectIdentifier {
	public static ModelPropertyIdentifier of(DomainObjectIdentifier ownerIdentifier, String propertyName) {
		return new ModelPropertyIdentifier(ownerIdentifier, ModelPropertyIdentity.of(propertyName));
	}

	private final DomainObjectIdentifier ownerIdentifier;
	private final ModelPropertyIdentity identity;

	public ModelPropertyIdentifier(DomainObjectIdentifier ownerIdentifier, ModelPropertyIdentity identity) {
		this.ownerIdentifier = ownerIdentifier;
		this.identity = identity;
	}

	public DomainObjectIdentifier getOwnerIdentifier() {
		return ownerIdentifier;
	}

	public ModelPropertyName getName() {
		return identity.getName();
	}

	@Override
	public Iterator<Object> iterator() {
		return ImmutableList.builder().addAll(ownerIdentifier).add(identity).build().iterator();
	}

	@Override
	public String toString() {
		return ownerIdentifier.toString() + " " + identity;
	}
}
