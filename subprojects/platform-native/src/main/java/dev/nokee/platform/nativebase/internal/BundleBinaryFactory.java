package dev.nokee.platform.nativebase.internal;

import dev.nokee.language.base.internal.GeneratedSourceSet;
import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.nativebase.BundleBinary;
import dev.nokee.runtime.nativebase.internal.DefaultTargetMachine;
import org.gradle.api.DomainObjectSet;

public interface BundleBinaryFactory {
	BundleBinary create(NamingScheme names, DefaultTargetMachine targetMachine, DomainObjectSet<GeneratedSourceSet> objectSourceSets);
}
