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
package dev.nokee.model.internal.ancestors;

import dev.nokee.model.internal.core.ModelNode;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public final class AncestorRef {
	private final ModelNode entityRef;

	private AncestorRef(ModelNode entityRef) {
		this.entityRef = entityRef;
	}

	public ModelNode get() {
		return entityRef;
	}

	@Override
	public String toString() {
		return entityRef.toString();
	}

	public static AncestorRef of(ModelNode entityRef) {
		return new AncestorRef(entityRef);
	}
}
