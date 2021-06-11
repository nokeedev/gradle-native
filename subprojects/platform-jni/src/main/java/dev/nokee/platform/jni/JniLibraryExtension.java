package dev.nokee.platform.jni;

import dev.nokee.platform.base.*;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.runtime.nativebase.TargetMachine;
import dev.nokee.runtime.nativebase.TargetMachineFactory;
import org.gradle.api.provider.SetProperty;

/**
 * Configuration for a Java Native Interface (JNI) library, defining the dependencies that make up the library plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the JNI Library Plugin.</p>
 *
 * @since 0.1
 */
@Deprecated // Use JavaNativeInterfaceLibrary instead.
public interface JniLibraryExtension extends DependencyAwareComponent<JavaNativeInterfaceLibraryComponentDependencies>, VariantAwareComponent<JniLibrary>, BinaryAwareComponent, TargetMachineAwareComponent, SourceAwareComponent<JavaNativeInterfaceLibrarySources> {
	/**
	 * Returns the dependencies of this component.
	 *
	 * @return a {@link JavaNativeInterfaceLibraryComponentDependencies}, never null.
	 * @since 0.1
	 */
	JavaNativeInterfaceLibraryComponentDependencies getDependencies();

	/**
	 * Specifies the target machines this component should be built for.
	 * The "machines" extension property (see {@link TargetMachineFactory}) can be used to construct common operating system and architecture combinations.
	 *
	 * <p>For example:</p>
	 * <pre>
	 * targetMachines = [machines.linux.x86_64, machines.windows.x86_64]
	 * </pre>
	 *
	 * @return a property for configuring the {@link TargetMachine}, never null.
	 * @since 0.1
	 */
	SetProperty<TargetMachine> getTargetMachines();
}
