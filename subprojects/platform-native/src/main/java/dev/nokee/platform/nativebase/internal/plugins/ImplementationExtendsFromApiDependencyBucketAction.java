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

package dev.nokee.platform.nativebase.internal.plugins;

import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.LibraryComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencyBucketSpec;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeLibraryComponentDependencies;
import org.gradle.api.Action;

public final class ImplementationExtendsFromApiDependencyBucketAction<TargetType> implements Action<TargetType> {
	@Override
	public void execute(TargetType target) {
		if (target instanceof DependencyAwareComponent) {
			ifNativeLibraryComponentDependencies((DependencyAwareComponent<?>) target, dependencies -> {
				((DeclarableDependencyBucketSpec) dependencies.getImplementation()).extendsFrom(dependencies.getApi());
			});
		}
	}

	private void ifNativeLibraryComponentDependencies(DependencyAwareComponent<?> target, Action<? super LibraryComponentDependencies> action) {
		if (target.getDependencies() instanceof DefaultNativeLibraryComponentDependencies) {
			action.execute((LibraryComponentDependencies) target.getDependencies());
		} else if (target.getDependencies() instanceof ModelBackedNativeLibraryComponentDependencies) {
			action.execute((LibraryComponentDependencies) target.getDependencies());
		}
	}
}
