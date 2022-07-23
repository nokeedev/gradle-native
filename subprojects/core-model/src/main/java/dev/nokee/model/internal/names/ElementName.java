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
package dev.nokee.model.internal.names;

import java.util.Objects;

/**
 * An element name represent the name of a domain object regardless of its owners.
 * The name is non-unique across all domain objects of a project.
 * For example, the element name of a task that assemble the output of a component is {@literal assemble}.
 * However, it's fully qualified name may be {@literal assembleFoo} for the {@literal foo} component.
 * In some cases, the element name may be the same as the fully qualified name such as the assemble task of the {@literal main} component.
 * It's important to keep in mind that both name represent different things.
 */
public interface ElementName {
	static ElementName of(String name) {
		return new DefaultElementName(Objects.requireNonNull(name));
	}
}
