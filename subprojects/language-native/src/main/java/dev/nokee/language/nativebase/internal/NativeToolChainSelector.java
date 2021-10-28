package dev.nokee.language.nativebase.internal;

import dev.nokee.platform.base.BuildVariant;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.nativeplatform.toolchain.NativeToolChain;


public interface NativeToolChainSelector {
	Provider<NativeToolChain> select(Task task);

	Provider<NativeToolChain> select(Task task, BuildVariant buildVariant);
}
