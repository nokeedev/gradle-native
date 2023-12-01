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

package dev.nokee.platform.nativebase.internal;

import dev.nokee.platform.base.VariantOf;
import dev.nokee.platform.base.internal.mixins.VariantAwareComponentMixIn;
import dev.nokee.platform.nativebase.NativeComponentOf;
import dev.nokee.testing.base.internal.TestableComponentSpec;
import org.gradle.api.Action;

// Internal implementation of NativeComponentOf<Component/Variant>
//   Use ModelMap#configureEach(CApplication) instead of DomainObjectCollection.withType(...).configureEach { ... }
//   Else you would have to use something like DomainObjectCollection.configureEach(withType(CApplication) {...})
// Contract: must implements/extends from T
public interface INativeComponentSpec<T /*extends ModelElement*/> extends NativeComponentOf<T>/*, T*/
	, VariantAwareComponentMixIn<VariantOf<T>>
	, NativeComponentSpec, TargetedNativeComponentSpec, TestableComponentSpec
{
	@Override
	@SuppressWarnings("unchecked")
	default void configure(Action<? super T> configureAction) {
		configureAction.execute((T) this);
	}
}
