package dev.nokee.platform.jni.internal;

import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.jni.JniLibrary;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.model.ObjectFactory;
import org.gradle.nativeplatform.SharedLibraryBinarySpec;

import javax.inject.Inject;

public abstract class JniLibraryInternal implements JniLibrary {
    private final DomainObjectSet<? super LanguageSourceSetInternal> sources;
    private final Configuration nativeImplementationDependencies;
    private final Configuration jvmImplementationDependencies;
    private final DomainObjectSet<SharedLibraryBinarySpec> binaries;

    @Inject
    public JniLibraryInternal(ObjectFactory objectFactory, Configuration nativeImplementationDependencies, Configuration jvmImplementationDependencies) {
        binaries = objectFactory.domainObjectSet(SharedLibraryBinarySpec.class);
        sources = objectFactory.domainObjectSet(LanguageSourceSetInternal.class);
        this.nativeImplementationDependencies = nativeImplementationDependencies;
        this.jvmImplementationDependencies = jvmImplementationDependencies;
    }

    public DomainObjectSet<SharedLibraryBinarySpec> getBinaries() {
        return binaries;
    }

    public DomainObjectSet<? super LanguageSourceSetInternal> getSources() {
        return sources;
    }

    public Configuration getNativeImplementationDependencies() {
        return nativeImplementationDependencies;
    }

    public Configuration getJvmImplementationDependencies() {
        return jvmImplementationDependencies;
    }
}
