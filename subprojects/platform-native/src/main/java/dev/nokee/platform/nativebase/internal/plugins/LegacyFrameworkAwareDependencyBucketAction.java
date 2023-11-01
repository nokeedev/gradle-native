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
import dev.nokee.platform.nativebase.internal.dependencies.RequestFrameworkAction;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;

public final class LegacyFrameworkAwareDependencyBucketAction<TargetType> implements Action<TargetType> {
	private final ObjectFactory objects;

	public LegacyFrameworkAwareDependencyBucketAction(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public void execute(TargetType target) {
		configureDependencies(target);

		if (target instanceof VariantAwareComponent) {
			((VariantAwareComponent<?>) target).getVariants().configureEach(this::configureDependencies);
		}
	}

	private <T> void configureDependencies(T target) {
		if (target instanceof DependencyAwareComponent) {
			ifNativeComponentDependencies((DependencyAwareComponent<?>) target, dependencies -> {
				((DependencyBucketInternal) dependencies.getImplementation()).getDefaultDependencyAction().set(new RequestFrameworkAction(objects));
				((DependencyBucketInternal) dependencies.getCompileOnly()).getDefaultDependencyAction().set(new RequestFrameworkAction(objects));
				((DependencyBucketInternal) dependencies.getLinkOnly()).getDefaultDependencyAction().set(new RequestFrameworkAction(objects));
				((DependencyBucketInternal) dependencies.getRuntimeOnly()).getDefaultDependencyAction().set(new RequestFrameworkAction(objects));

				if (dependencies instanceof DefaultNativeLibraryComponentDependencies) {
					((DefaultNativeLibraryComponentDependencies) dependencies).getApi().getDefaultDependencyAction().set(new RequestFrameworkAction(objects));
				}
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
		}
	}
}
