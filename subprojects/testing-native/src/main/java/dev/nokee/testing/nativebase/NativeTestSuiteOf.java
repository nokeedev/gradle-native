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

package dev.nokee.testing.nativebase;

import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.HasDevelopmentVariant;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.VariantOf;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.TargetBuildTypeAwareComponent;
import dev.nokee.platform.nativebase.TargetLinkageAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.testing.base.TestSuiteComponent;

public interface NativeTestSuiteOf<T extends NativeTestSuite> extends TestSuiteComponent
	, TargetMachineAwareComponent, TargetBuildTypeAwareComponent, TargetLinkageAwareComponent
	, DependencyAwareComponent<NativeComponentDependencies>
	, VariantAwareComponent<VariantOf<T>>, HasDevelopmentVariant<VariantOf<T>>
{
}
