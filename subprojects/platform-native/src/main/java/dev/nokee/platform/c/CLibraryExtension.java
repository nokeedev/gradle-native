package dev.nokee.platform.c;

import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;

public interface CLibraryExtension extends DependencyAwareComponent<NativeLibraryDependencies>, TargetMachineAwareComponent {
}
