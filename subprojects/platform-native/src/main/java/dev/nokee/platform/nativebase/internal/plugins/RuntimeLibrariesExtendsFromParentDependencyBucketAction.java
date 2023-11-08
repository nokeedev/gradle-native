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

import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObjectIdentifiers;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.View;
import dev.nokee.platform.base.internal.dependencies.ResolvableDependencyBucketSpec;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.HasRuntimeLibrariesDependencyBucket;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectCollection;

public final class RuntimeLibrariesExtendsFromParentDependencyBucketAction<TargetType> implements Action<TargetType> {
	private final NamedDomainObjectCollection<DependencyBucket> buckets;

	public RuntimeLibrariesExtendsFromParentDependencyBucketAction(NamedDomainObjectCollection<DependencyBucket> buckets) {
		this.buckets = buckets;
	}

	@Override
	public void execute(TargetType target) {
		if (target instanceof DependencyAwareComponent && target instanceof BinaryAwareComponent) {
			ifNativeComponentDependencies((DependencyAwareComponent<?>) target, parentDependencies -> {
				final View<Binary> binaries = ((BinaryAwareComponent) target).getBinaries();
				binaries.configureEach(binary -> {
					if (binary instanceof HasRuntimeLibrariesDependencyBucket) {
						ModelElementSupport.safeAsModelElement(binary).ifPresent(element -> {
							buckets.withType(ResolvableDependencyBucketSpec.class).getByName(ModelObjectIdentifiers.asFullyQualifiedName(element.getIdentifier().child("runtimeLibraries")).toString()).extendsFrom(parentDependencies.getImplementation(), parentDependencies.getRuntimeOnly());
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
		}
	}
}
