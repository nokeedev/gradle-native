/*
 * Copyright 2020-2021 the original author or authors.
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
package dev.nokee.runtime.nativebase;

/**
 * A factory for creating {@link TargetLinkage} instances.
 *
 * @since 0.5
 */
public interface TargetLinkageFactory extends dev.nokee.platform.nativebase.TargetLinkageFactory {
	/**
	 * Creates a shared linkage for building shared libraries.
	 *
	 * @return a {@link TargetLinkage} instance representing the shared linkage, never null.
	 */
	TargetLinkage getShared();

	/**
	 * Creates a static linkage for building static libraries.
	 *
	 * @return a {@link TargetLinkage} instance representing the static linkage, never null.
	 */
	TargetLinkage getStatic();
}
