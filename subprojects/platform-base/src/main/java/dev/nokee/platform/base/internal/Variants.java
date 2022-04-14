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

import dev.nokee.model.internal.core.ModelComponent;
import dev.nokee.model.internal.core.ModelNode;
import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.ComponentVariants;

import java.util.Iterator;
import java.util.Map;

public final class Variants implements ComponentVariants, Iterable<ModelNode>, ModelComponent {
	private final Map<BuildVariant, ModelNode> variants;

	public Variants(Map<BuildVariant, ModelNode> variants) {
		this.variants = variants;
	}

	@Override
	public Iterator<ModelNode> iterator() {
		return variants.values().iterator();
	}
}
