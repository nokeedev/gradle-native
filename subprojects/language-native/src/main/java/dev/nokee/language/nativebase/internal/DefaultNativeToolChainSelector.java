/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.language.nativebase.internal;

import dev.nokee.platform.base.BuildVariant;
import dev.nokee.platform.base.internal.BuildVariantInternal;
import dev.nokee.runtime.nativebase.TargetMachine;
import org.gradle.api.Task;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.swift.tasks.SwiftCompile;
import org.gradle.model.internal.registry.ModelRegistry;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.tasks.CreateStaticLibrary;
import org.gradle.nativeplatform.toolchain.NativeToolChain;

public final class DefaultNativeToolChainSelector implements NativeToolChainSelector {
	private final ToolChainSelectorInternal selector;
	private final ProviderFactory providers;

	public DefaultNativeToolChainSelector(ModelRegistry modelRegistry, ProviderFactory providers) {
		this.selector = new ToolChainSelectorInternal(modelRegistry);
		this.providers = providers;
	}

	public Provider<NativeToolChain> select(Task task) {
		return targetPlatformProperty(task).map(it -> {
			if (task instanceof SwiftCompile) {
				return selector.selectSwift((NativePlatformInternal) it);
			} else {
				return selector.select((NativePlatformInternal) it);
			}
		});
	}

	public Provider<NativeToolChain> select(Task task, BuildVariant buildVariant) {
		if (task instanceof SwiftCompile) {
			return providers.provider(() -> selector.selectSwift(((BuildVariantInternal) buildVariant).getAxisValue(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS)));
		} else {
			return providers.provider(() -> selector.select(((BuildVariantInternal) buildVariant).getAxisValue(TargetMachine.TARGET_MACHINE_COORDINATE_AXIS)));
		}
	}

	private static Property<NativePlatform> targetPlatformProperty(Task task) {
		if (task instanceof AbstractNativeCompileTask) {
			return ((AbstractNativeCompileTask) task).getTargetPlatform();
		} else if (task instanceof SwiftCompile) {
			return ((SwiftCompile) task).getTargetPlatform();
		} else if (task instanceof AbstractLinkTask) {
			return ((AbstractLinkTask) task).getTargetPlatform();
		} else if (task instanceof CreateStaticLibrary) {
			return ((CreateStaticLibrary) task).getTargetPlatform();
		} else {
			throw new IllegalArgumentException();
		}
	}
}
