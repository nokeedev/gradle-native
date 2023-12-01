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

package dev.nokee.platform.nativebase;

import dev.nokee.platform.base.Component;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.VariantOf;
import dev.nokee.testing.base.TestableComponent;
import org.gradle.api.Action;

// TODO: we should tie T to a special type while would be either a Component or Variant or event TestSuiteComponent
public interface NativeComponentOf<T> extends Component, TestableComponent
	, VariantAwareComponent<VariantOf<T>>, HasDevelopmentVariant<VariantOf<T>>
	, TargetMachineAwareComponent, TargetBuildTypeAwareComponent, TargetLinkageAwareComponent
{
	void configure(Action<? super T> configureAction);
}
