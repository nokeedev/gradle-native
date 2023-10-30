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

import dev.nokee.model.HasName;
import dev.nokee.model.internal.names.ElementName;
import lombok.EqualsAndHashCode;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@EqualsAndHashCode
public final class DefaultModelObjectIdentifier implements ModelObjectIdentifier, HasName {
	@Nullable private final ModelObjectIdentifier parentIdentifier;
	private final ElementName name;

	public DefaultModelObjectIdentifier(ElementName name) {
		this(name, null);
	}

	public DefaultModelObjectIdentifier(ElementName name, @Nullable ModelObjectIdentifier parentIdentifier) {
		this.name = name;
		this.parentIdentifier = parentIdentifier;
	}

	@Nullable
	@Override
	public ModelObjectIdentifier getParent() {
		return parentIdentifier;
	}

	@Override
	public ElementName getName() {
		return name;
	}

	@Override
	public Iterator<Object> iterator() {
		final List<Object> result = new ArrayList<>();
		if (parentIdentifier != null) {
			parentIdentifier.forEach(result::add);
		}
		result.add(this);
		return result.iterator();
	}

	@Override
	public String toString() {
		throw new UnsupportedOperationException();
	}
}
