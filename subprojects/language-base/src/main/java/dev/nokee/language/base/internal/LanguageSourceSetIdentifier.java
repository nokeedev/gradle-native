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
package dev.nokee.language.base.internal;

import com.google.common.collect.ImmutableList;
import dev.nokee.model.DomainObjectIdentifier;
import lombok.EqualsAndHashCode;

import java.util.Iterator;
import java.util.Objects;

@EqualsAndHashCode
public final class LanguageSourceSetIdentifier implements DomainObjectIdentifier {
	private final LanguageSourceSetIdentity identity;
	private final DomainObjectIdentifier ownerIdentifier;

	private LanguageSourceSetIdentifier(LanguageSourceSetIdentity identity, DomainObjectIdentifier ownerIdentifier) {
		this.identity = Objects.requireNonNull(identity);
		this.ownerIdentifier = Objects.requireNonNull(ownerIdentifier);
	}

	public LanguageSourceSetName getName() {
		return identity.getName();
	}

	public DomainObjectIdentifier getOwnerIdentifier() {
		return ownerIdentifier;
	}

	public static LanguageSourceSetIdentifier of(DomainObjectIdentifier ownerIdentifier, String name) {
		return new LanguageSourceSetIdentifier(LanguageSourceSetIdentity.of(name), ownerIdentifier);
	}

	@Override
	public Iterator<Object> iterator() {
		return ImmutableList.builder().addAll(ownerIdentifier).add(identity).build().iterator();
	}
}
