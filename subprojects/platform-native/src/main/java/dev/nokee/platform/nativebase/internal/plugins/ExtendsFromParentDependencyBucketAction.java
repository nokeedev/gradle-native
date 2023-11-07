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
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.base.internal.dependencies.DependencyBucketInternal;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.ModelBackedNativeLibraryComponentDependencies;
import org.gradle.api.Action;

public final class ExtendsFromParentDependencyBucketAction<TargetType> implements Action<TargetType> {
	@Override
	public void execute(TargetType target) {
		if (target instanceof DependencyAwareComponent && target instanceof VariantAwareComponent) {
			ifNativeComponentDependencies((DependencyAwareComponent<?>) target, parentDependencies -> {
				((VariantAwareComponent<?>) target).getVariants().configureEach(variant -> {
					if (variant instanceof DependencyAwareComponent) {
						ifNativeComponentDependencies((DependencyAwareComponent<?>) variant, dependencies -> {
							((DependencyBucketInternal) dependencies.getImplementation()).extendsFrom(parentDependencies.getImplementation());
							((DependencyBucketInternal) dependencies.getCompileOnly()).extendsFrom(parentDependencies.getCompileOnly());
							((DependencyBucketInternal) dependencies.getLinkOnly()).extendsFrom(parentDependencies.getLinkOnly());
							((DependencyBucketInternal) dependencies.getRuntimeOnly()).extendsFrom(parentDependencies.getRuntimeOnly());
						});
					}
				});
			});
		}
	}

	private void ifNativeComponentDependencies(DependencyAwareComponent<?> target, Action<? super NativeComponentDependencies> action) {
		if (target.getDependencies() instanceof DefaultNativeLibraryComponentDependencies) {
			action.execute((NativeComponentDependencies) target.getDependencies());
		} else if (target.getDependencies() instanceof DefaultNativeComponentDependencies) {
			action.execute((NativeComponentDependencies) target.getDependencies());
		} else if (target.getDependencies() instanceof DefaultNativeApplicationComponentDependencies) {
			action.execute((NativeComponentDependencies) target.getDependencies());
		} else if (target.getDependencies() instanceof ModelBackedNativeApplicationComponentDependencies) {
			action.execute((NativeComponentDependencies) target.getDependencies());
		} else if (target.getDependencies() instanceof ModelBackedNativeLibraryComponentDependencies) {
			action.execute((NativeComponentDependencies) target.getDependencies());
		}
	}
}
