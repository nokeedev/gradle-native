package dev.nokee.platform.nativebase.tasks.internal;

import dev.nokee.platform.nativebase.tasks.LinkSharedLibrary;
import org.gradle.api.DefaultTask;
import org.gradle.api.provider.Property;
import org.gradle.nativeplatform.toolchain.NativeToolChain;

public abstract class LinkSharedLibraryTask extends DefaultTask implements LinkSharedLibrary {
	public abstract Property<NativeToolChain> getToolChain();
}
