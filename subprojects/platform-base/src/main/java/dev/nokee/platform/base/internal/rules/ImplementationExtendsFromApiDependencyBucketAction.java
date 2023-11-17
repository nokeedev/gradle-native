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

package dev.nokee.platform.base.internal.rules;

import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.LibraryComponentDependencies;
import dev.nokee.platform.base.internal.mixins.ApiDependencyBucketMixIn;
import dev.nokee.platform.base.internal.mixins.ImplementationDependencyBucketMixIn;

import java.util.function.BiConsumer;

public final class ImplementationExtendsFromApiDependencyBucketAction implements BiConsumer<ModelObjectIdentifier, DependencyAwareComponent<?>> {
	@Override
	public void accept(ModelObjectIdentifier objects, DependencyAwareComponent<?> target) {
		final ComponentDependencies targetDependencies = target.getDependencies();
		if (targetDependencies instanceof LibraryComponentDependencies && targetDependencies instanceof ApiDependencyBucketMixIn && targetDependencies instanceof ImplementationDependencyBucketMixIn) {
			((ImplementationDependencyBucketMixIn) targetDependencies).getImplementation().extendsFrom(((ApiDependencyBucketMixIn) targetDependencies).getApi());
		}
	}
}
