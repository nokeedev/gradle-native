package dev.nokee.platform.cpp;

import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;

public interface CppApplicationExtension extends DependencyAwareComponent<NativeComponentDependencies>, TargetMachineAwareComponent {
}
