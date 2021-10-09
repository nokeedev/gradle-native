/*
 * Copyright 2020 the original author or authors.
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

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.HasCompileOnlyDependencyBucket;
import dev.nokee.platform.base.HasImplementationDependencyBucket;
import dev.nokee.platform.base.HasRuntimeOnlyDependencyBucket;

/**
 * Allows the implementation dependencies of a native component to be specified.
 *
 * @since 0.4
 */
public interface NativeComponentDependencies extends ComponentDependencies, HasImplementationDependencyBucket, HasCompileOnlyDependencyBucket, HasLinkOnlyDependencyBucket, HasRuntimeOnlyDependencyBucket {
}
