package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.base.internal.BaseIdeCleanMetadata;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;

public class VisualStudioIdeCleanMetadata extends BaseIdeCleanMetadata {
	public VisualStudioIdeCleanMetadata(Provider<? extends Task> cleanTask) {
		super(cleanTask);
	}
}
