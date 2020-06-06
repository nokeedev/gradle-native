package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.StaticLibraryBinary;
import dev.nokee.platform.nativebase.internal.dependencies.NativeIncomingDependencies;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import org.gradle.api.DomainObjectSet;

public abstract class StaticLibraryBinaryInternal extends BaseNativeBinary implements StaticLibraryBinary {
	public StaticLibraryBinaryInternal(NamingScheme names, DomainObjectSet<GeneratedSourceSet> objectSourceSets, DefaultTargetMachine targetMachine, NativeIncomingDependencies dependencies) {
		super(names, objectSourceSets, targetMachine, dependencies);
	}
}
