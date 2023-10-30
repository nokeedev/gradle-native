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
package dev.nokee.platform.base.internal;

import dev.nokee.model.HasName;
import dev.nokee.model.internal.names.ElementName;
import lombok.EqualsAndHashCode;

import java.util.Objects;

@EqualsAndHashCode
public final class ComponentIdentity implements HasName {
	private static final ElementName MAIN_COMPONENT_NAME = ElementName.ofMain();

	public static ComponentIdentity ofMain() {
		return new ComponentIdentity(MAIN_COMPONENT_NAME);
	}

	public static ComponentIdentity of(String name) {
		return new ComponentIdentity(ElementName.of(name));
	}

	public static ComponentIdentity of(ElementName name) {
		return new ComponentIdentity(name);
	}

	private final ElementName name;

	private ComponentIdentity(ElementName name) {
		this.name = Objects.requireNonNull(name);
	}

	@Override
	public ElementName getName() {
		return name;
	}

	public boolean isMainComponent() {
		return name.equals(MAIN_COMPONENT_NAME);
	}

	@Override
	public String toString() {
		if (isMainComponent()) {
			return "";
		} else {
			return name.toString();
		}
	}
}
