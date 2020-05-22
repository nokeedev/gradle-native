package dev.nokee.platform.c;

import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;

public interface CApplicationExtension extends DependencyAwareComponent<NativeComponentDependencies>, TargetMachineAwareComponent {
}
