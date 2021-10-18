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

import dev.nokee.model.internal.core.ModelNodeUtils;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.BinaryView;
import groovy.lang.Closure;
import org.gradle.api.Action;
import org.gradle.util.ConfigureUtil;

public interface ModelBackedBinaryAwareComponentMixIn extends BinaryAwareComponent {
	@Override
	@SuppressWarnings("unchecked")
	default BinaryView<Binary> getBinaries() {
		return ModelNodeUtils.get(ModelNodes.of(this), BaseComponent.class).getBinaries();
	}

	@Override
	default void binaries(Action<? super BinaryView<Binary>> action) {
		action.execute(getBinaries());
	}

	@Override
	default void binaries(@SuppressWarnings("rawtypes") Closure closure) {
		binaries(ConfigureUtil.configureUsing(closure));
	}
}
