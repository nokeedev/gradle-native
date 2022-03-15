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

import dev.nokee.model.internal.core.ModelEntityId;
import dev.nokee.model.internal.core.ModelNode;
import lombok.EqualsAndHashCode;

/**
 * A relative name represent a non-unique name of a domain object relative to another domain object owner.
 * Although the name somewhat represent the domain object's ownership hierarchy, users should not use it as such.
 * The relative name can be seen as a {@link FullyQualifiedName} where the base domain object is not the root of the model.
 */
@EqualsAndHashCode
public final class RelativeName {
	private final BaseRef baseRef;
	private final String name;

	private RelativeName(BaseRef baseRef, String name) {
		this.baseRef = baseRef;
		this.name = name;
	}

	BaseRef baseRef() {
		return baseRef;
	}

	public static RelativeName of(ModelEntityId baseRef, String name) {
		return new RelativeName(new BaseRef(baseRef), name);
	}

	public static RelativeName of(ModelNode baseRef, String name) {
		return new RelativeName(new BaseRef(baseRef.getId()), name);
	}

	@Override
	public String toString() {
		return name;
	}

	@EqualsAndHashCode
	public static final class BaseRef {
		private final ModelEntityId entityRef;

		private BaseRef(ModelEntityId entityRef) {
			this.entityRef = entityRef;
		}

		@Override
		public String toString() {
			return "BaseRef{" +
				"entityRef=" + entityRef +
				'}';
		}

		public static BaseRef of(ModelNode entityRef) {
			return new BaseRef(entityRef.getId());
		}
	}
}
