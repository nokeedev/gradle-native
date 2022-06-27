/*
 * Copyright 2022 the original author or authors.
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

import com.google.common.collect.Iterators;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.HasName;
import dev.nokee.model.internal.core.DisplayName;
import dev.nokee.model.internal.core.ModelPath;
import dev.nokee.model.internal.names.ElementName;

import javax.annotation.Nullable;
import java.util.Iterator;

public final class DefaultDomainObjectIdentifier implements DomainObjectIdentifier, HasName {
	private final ElementName elementName;
	@Nullable
	private final DomainObjectIdentifier parentIdentifier;
	private final DisplayName displayName;
	private final ModelPath path;

	public DefaultDomainObjectIdentifier(ElementName elementName, @Nullable DomainObjectIdentifier parentIdentifier, DisplayName displayName, ModelPath path) {
		this.elementName = elementName;
		this.parentIdentifier = parentIdentifier;
		this.displayName = displayName;
		this.path = path;
	}

	@Override
	public Object getName() {
		return elementName;
	}

	@Override
	public Iterator<Object> iterator() {
		if (parentIdentifier == null) {
			return Iterators.forArray(this);
		} else {
			return Iterators.concat(parentIdentifier.iterator(), Iterators.forArray(this));
		}
	}

	@Override
	public String toString() {
		return displayName + " ':" + path.toString().replace(".", ":") + "'";
	}
}
