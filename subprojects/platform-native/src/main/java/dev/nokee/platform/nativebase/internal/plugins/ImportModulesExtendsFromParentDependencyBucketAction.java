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

import dev.nokee.language.base.LanguageSourceSet;
import dev.nokee.language.swift.internal.plugins.HasImportModules;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.View;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeApplicationComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeComponentDependencies;
import dev.nokee.platform.nativebase.internal.dependencies.DefaultNativeLibraryComponentDependencies;
import org.gradle.api.Action;

public final class ImportModulesExtendsFromParentDependencyBucketAction<TargetType> implements Action<TargetType> {
	@Override
	public void execute(TargetType target) {
		if (target instanceof DependencyAwareComponent && target instanceof SourceAwareComponent && ((SourceAwareComponent<?>) target).getSources() instanceof View) {
			ifNativeComponentDependencies((DependencyAwareComponent<?>) target, parentDependencies -> {
				@SuppressWarnings("unchecked")
				final View<LanguageSourceSet> sources = (View<LanguageSourceSet>) ((SourceAwareComponent<?>) target).getSources();
				sources.configureEach(sourceSet -> {
					if (sourceSet instanceof HasImportModules) {
						((HasImportModules) sourceSet).getImportModules().extendsFrom(parentDependencies.getImplementation(), parentDependencies.getCompileOnly());
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
