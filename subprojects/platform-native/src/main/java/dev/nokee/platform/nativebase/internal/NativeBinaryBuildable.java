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
package dev.nokee.platform.nativebase.internal;

import com.google.common.base.Suppliers;
import dev.nokee.language.base.tasks.SourceCompile;
import dev.nokee.platform.nativebase.BundleBinary;
import dev.nokee.platform.nativebase.ExecutableBinary;
import dev.nokee.platform.nativebase.NativeBinary;
import dev.nokee.platform.nativebase.SharedLibraryBinary;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.tasks.CreateStaticLibrary;
import dev.nokee.platform.nativebase.tasks.LinkBundle;
import dev.nokee.platform.nativebase.tasks.LinkExecutable;
import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import dev.nokee.platform.nativebase.tasks.internal.CreateStaticLibraryTask;
import org.gradle.language.nativeplatform.tasks.AbstractNativeCompileTask;
import org.gradle.language.swift.tasks.SwiftCompile;
import org.gradle.nativeplatform.platform.NativePlatform;
import org.gradle.nativeplatform.platform.internal.NativePlatformInternal;
import org.gradle.nativeplatform.tasks.AbstractLinkTask;
import org.gradle.nativeplatform.tasks.LinkMachOBundle;
import org.gradle.nativeplatform.toolchain.NativeToolChain;
import org.gradle.nativeplatform.toolchain.internal.NativeToolChainInternal;
import org.gradle.nativeplatform.toolchain.internal.PlatformToolProvider;

import java.util.function.Supplier;

public final class NativeBinaryBuildable {
	private final NativeBinary delegate;
	private final Supplier<Boolean> value = Suppliers.memoize(this::isBuildable);

	public NativeBinaryBuildable(NativeBinary delegate) {
		this.delegate = delegate;
	}

	public boolean get() {
		return value.get();
	}

	private boolean isBuildable() {
		try {
			if (delegate instanceof BundleBinary) {
				return isBuildable(delegate) && isBuildable(((BundleBinary) delegate).getLinkTask().get());
			} else if (delegate instanceof StaticLibraryBinary) {
				return isBuildable(delegate) && isBuildable(((StaticLibraryBinary) delegate).getCreateTask().get());
			} else if (delegate instanceof SharedLibraryBinary) {
				return isBuildable(delegate) && isBuildable(((SharedLibraryBinary) delegate).getLinkTask().get());
			} else if (delegate instanceof ExecutableBinary) {
				return isBuildable(delegate) && isBuildable(((ExecutableBinary) delegate).getLinkTask().get());
			} else {
				throw new UnsupportedOperationException(String.format("Native binary type '%s' is not known.", delegate));
			}
		} catch (Throwable ex) { // because toolchain selection calls xcrun for macOS which doesn't exists on non-mac system
			return false;
		}
	}

	private static boolean isBuildable(CreateStaticLibrary createTask) {
		CreateStaticLibraryTask createTaskInternal = (CreateStaticLibraryTask)createTask;
		return isBuildable(createTaskInternal.getToolChain().get(), createTaskInternal.getTargetPlatform().get());
	}

	private static boolean isBuildable(LinkBundle linkTask) {
		LinkMachOBundle linkTaskInternal = (LinkMachOBundle)linkTask;
		return isBuildable(linkTaskInternal.getToolChain().get(), linkTaskInternal.getTargetPlatform().get());
	}

	private static boolean isBuildable(LinkSharedLibrary linkTask) {
		AbstractLinkTask linkTaskInternal = (AbstractLinkTask)linkTask;
		return isBuildable(linkTaskInternal.getToolChain().get(), linkTaskInternal.getTargetPlatform().get());
	}

	private static boolean isBuildable(LinkExecutable linkTask) {
		AbstractLinkTask linkTaskInternal = (AbstractLinkTask)linkTask;
		return isBuildable(linkTaskInternal.getToolChain().get(), linkTaskInternal.getTargetPlatform().get());
	}

	private static boolean isBuildable(NativeBinary binary) {
		return binary.getCompileTasks().get().stream().allMatch(NativeBinaryBuildable::isBuildable);
	}

	private static boolean isBuildable(SourceCompile task) {
		if (task instanceof AbstractNativeCompileTask) {
			return isBuildable((AbstractNativeCompileTask) task);
		} else if (task instanceof SwiftCompile) {
			return isBuildable((SwiftCompile) task);
		} else {
			return true; // assume buildable
		}
	}

	private static boolean isBuildable(AbstractNativeCompileTask compileTask) {
		return isBuildable(compileTask.getToolChain().get(), compileTask.getTargetPlatform().get());
	}

	private static boolean isBuildable(SwiftCompile compileTask) {
		return isBuildable(compileTask.getToolChain().get(), compileTask.getTargetPlatform().get());
	}

	private static boolean isBuildable(NativeToolChain toolchain, NativePlatform platform) {
		NativeToolChainInternal toolchainInternal = (NativeToolChainInternal)toolchain;
		NativePlatformInternal platformInternal = (NativePlatformInternal)platform;
		PlatformToolProvider toolProvider = toolchainInternal.select(platformInternal);
		return toolProvider.isAvailable();
	}
}
