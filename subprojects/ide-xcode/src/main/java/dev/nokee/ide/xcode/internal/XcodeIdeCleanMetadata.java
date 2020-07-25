package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.base.internal.BaseIdeCleanMetadata;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;

public class XcodeIdeCleanMetadata extends BaseIdeCleanMetadata {
	public XcodeIdeCleanMetadata(Provider<? extends Task> cleanTask) {
		super(cleanTask);
	}
}
