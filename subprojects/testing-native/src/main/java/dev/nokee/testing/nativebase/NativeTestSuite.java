package dev.nokee.testing.nativebase;

import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.testing.base.TestSuiteComponent;

public interface NativeTestSuite extends TestSuiteComponent, DependencyAwareComponent<NativeComponentDependencies> {
}
