package dev.nokee.platform.cpp;

import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;

public interface CppLibraryExtension extends DependencyAwareComponent<NativeLibraryDependencies>, TargetMachineAwareComponent {
}
