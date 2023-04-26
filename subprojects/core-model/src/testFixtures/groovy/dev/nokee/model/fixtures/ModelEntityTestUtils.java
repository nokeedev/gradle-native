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
package dev.nokee.model.fixtures;

import dev.nokee.model.internal.core.Component;
import dev.nokee.model.internal.core.DefaultComponentRegistry;
import dev.nokee.model.internal.core.ModelComponentRegistry;
import dev.nokee.model.internal.core.ModelNode;
import lombok.EqualsAndHashCode;

public final class ModelEntityTestUtils {
	public static ModelNode newEntity() {
		return new ModelNode();
	}

	public static ModelNode newEntity(ModelComponentRegistry components) {
		return new ModelNode(components);
	}

	public static ModelComponentRegistry newComponentRegistry() {
		return new DefaultComponentRegistry();
	}

	private static long nextId = 0;

	public static Component.Id aComponentId() {
		return new TestComponentId(nextId++);
	}

	@EqualsAndHashCode
	private static final class TestComponentId implements Component.Id {
		private final long id;

		private TestComponentId(long id) {
			this.id = id;
		}
	}
}
