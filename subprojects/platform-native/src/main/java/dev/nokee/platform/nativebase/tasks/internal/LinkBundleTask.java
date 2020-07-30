package dev.nokee.platform.nativebase.tasks.internal;

import dev.nokee.platform.nativebase.tasks.LinkBundle;
import org.gradle.api.file.RegularFile;
import org.gradle.api.provider.Provider;
import org.gradle.nativeplatform.tasks.LinkMachOBundle;

public abstract class LinkBundleTask extends LinkMachOBundle implements LinkBundle, ObjectFilesToBinaryTask {
	@Override
	public Provider<RegularFile> getBinaryFile() {
		return getLinkedFile();
	}
}
