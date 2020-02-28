package dev.nokee.platform.jni.fixtures.elements;

import dev.gradleplugins.test.fixtures.sources.SourceElement;
import dev.gradleplugins.test.fixtures.sources.SourceFile;

import java.util.ArrayList;
import java.util.List;

public abstract class JniLibraryElement extends SourceElement {
    public abstract SourceElement getJvmSources();

    public abstract SourceElement getNativeSources();

    @Override
    public List<SourceFile> getFiles() {
        List<SourceFile> result = new ArrayList<>();
        result.addAll(getJvmSources().getFiles());
        result.addAll(getNativeSources().getFiles());
        return result;
    }
}
