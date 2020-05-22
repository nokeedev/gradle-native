package dev.nokee.platform.swift;

import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.nativebase.NativeLibraryDependencies;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;

public interface SwiftLibraryExtension extends DependencyAwareComponent<NativeLibraryDependencies>, TargetMachineAwareComponent {
}
