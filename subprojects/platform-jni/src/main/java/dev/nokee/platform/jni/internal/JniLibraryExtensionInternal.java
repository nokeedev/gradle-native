package dev.nokee.platform.jni.internal;

import dev.nokee.language.base.internal.LanguageSourceSetInternal;
import dev.nokee.platform.base.internal.BinaryInternal;
import dev.nokee.platform.jni.JniLibraryExtension;
import org.gradle.api.DomainObjectSet;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;

public abstract class JniLibraryExtensionInternal implements JniLibraryExtension {
    private final DomainObjectSet<? super LanguageSourceSetInternal> sources;
	private final ConfigurationContainer configurations;
	private final Configuration nativeImplementationDependencies;
    private final Configuration jvmImplementationDependencies;
    private final DomainObjectSet<? super BinaryInternal> binaries;
    private final DomainObjectSet<JniLibraryInternal> variants;

    @Inject
    public JniLibraryExtensionInternal(ObjectFactory objectFactory, ConfigurationContainer configurations, Configuration nativeImplementationDependencies, Configuration jvmImplementationDependencies) {
        binaries = objectFactory.domainObjectSet(BinaryInternal.class);
        sources = objectFactory.domainObjectSet(LanguageSourceSetInternal.class);
        variants = objectFactory.domainObjectSet(JniLibraryInternal.class);
		this.configurations = configurations;
		this.nativeImplementationDependencies = nativeImplementationDependencies;
        this.jvmImplementationDependencies = jvmImplementationDependencies;
    }

    @Inject
	protected abstract ObjectFactory getObjectFactory();

    public void registerVariant() {
		JniLibraryInternal library = getObjectFactory().newInstance(JniLibraryInternal.class, configurations, sources, nativeImplementationDependencies);
		library.registerSharedLibraryBinary();
		library.registerJniJarBinary();
		variants.add(library);
	}

    public DomainObjectSet<? super BinaryInternal> getBinaries() {
        return binaries;
    }

	public DomainObjectSet<JniLibraryInternal> getVariants() {
		return variants;
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
