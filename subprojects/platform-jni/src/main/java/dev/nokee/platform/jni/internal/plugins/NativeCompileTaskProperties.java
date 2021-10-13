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
package dev.nokee.platform.jni.internal.plugins;

import dev.nokee.platform.base.internal.util.PropertyUtils;
import org.gradle.api.Action;
import org.gradle.api.Task;
import org.gradle.api.Transformer;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.FileCollection;
import org.gradle.api.provider.Property;
import org.gradle.api.provider.Provider;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.swift.tasks.SwiftCompile;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.tasks.CreateStaticLibrary;

import java.util.function.BiConsumer;

import static dev.nokee.platform.base.internal.util.PropertyUtils.wrap;

public final class NativeCompileTaskProperties {
	private NativeCompileTaskProperties() {}

	//region Includes
	public static <SELF extends Task> Action<SELF> includeRoots(BiConsumer<? super SELF, ? super PropertyUtils.FileCollectionProperty> action) {
		return task -> action.accept(task, wrap(includeRootsProperty(task)));
	}

	public static <SELF extends Task> Transformer<FileCollection, SELF> includeRoots() {
		return NativeCompileTaskProperties::includeRootsProperty;
	}

	private static ConfigurableFileCollection includeRootsProperty(Task task) {
		if (task instanceof AbstractNativeCompileTask) {
			return ((AbstractNativeCompileTask) task).getIncludes();
		} else {
			throw new IllegalArgumentException();
		}
	}
	//endregion

	//region Target platform
	public static <SELF extends Task> Action<SELF> targetPlatform(BiConsumer<? super SELF, ? super PropertyUtils.Property<? extends NativePlatform>> action) {
		return task -> action.accept(task, wrap(targetPlatformProperty(task)));
	}

	public static <SELF extends Task> Transformer<Provider<NativePlatform>, SELF> targetPlatform() {
		return NativeCompileTaskProperties::targetPlatformProperty;
	}

	public static Property<NativePlatform> targetPlatformProperty(Task task) {
		if (task instanceof AbstractNativeCompileTask) {
			return ((AbstractNativeCompileTask) task).getTargetPlatform();
		} else if (task instanceof SwiftCompile) {
			return ((SwiftCompile) task).getTargetPlatform();
		} else if (task instanceof org.gradle.nativeplatform.tasks.AbstractLinkTask) {
			return ((org.gradle.nativeplatform.tasks.AbstractLinkTask) task).getTargetPlatform();
		} else if (task instanceof CreateStaticLibrary) {
			return ((CreateStaticLibrary) task).getTargetPlatform();
		} else {
			throw new IllegalArgumentException();
		}
	}
	//endregion
}
