package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.base.internal.BaseIdeProjectMetadata;
import org.gradle.api.provider.Provider;

public class XcodeIdeProjectMetadata extends BaseIdeProjectMetadata {
    public XcodeIdeProjectMetadata(Provider<DefaultXcodeIdeProject> xcodeProject) {
        super(xcodeProject);
    }
}
