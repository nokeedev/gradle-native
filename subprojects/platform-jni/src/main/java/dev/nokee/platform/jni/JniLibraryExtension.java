package dev.nokee.platform.jni;

import dev.nokee.platform.base.*;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.platform.nativebase.TargetMachineFactory;
import dev.nokee.runtime.nativebase.TargetMachine;
import org.gradle.api.Action;
import org.gradle.api.provider.SetProperty;

/**
 * Configuration for a Java Native Interface (JNI) library, defining the dependencies that make up the library plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the JNI Library Plugin.</p>
 *
 * @since 0.1
 */
public interface JniLibraryExtension extends DependencyAwareComponent<JniLibraryDependencies>, VariantAwareComponent<JniLibrary>, BinaryAwareComponent, TargetMachineAwareComponent {
	/**
	 * Returns the dependencies of this component.
	 *
	 * @return a {@link JniLibraryDependencies}, never null.
	 * @since 0.1
	 */
	JniLibraryDependencies getDependencies();

	/**
	 * Configure the dependencies of this component.
	 *
	 * @param action configuration action for {@link JniLibraryDependencies}.
	 * @since 0.1
	 */
	void dependencies(Action<? super JniLibraryDependencies> action);

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

	/**
	 * Configure the variants of this component.
	 *
	 * @return a {@link VariantView} for configuring each {@link JniLibrary}, never null.
	 * @since 0.2
	 */
	VariantView<JniLibrary> getVariants();

	/**
	 * Configure the binaries of this component.
	 * The view contains an aggregation of all the binaries for each realized variants.
	 *
	 * @return a {@link BinaryView} for configuring each binary, never null.
	 * @since 0.3
	 */
	BinaryView<Binary> getBinaries();

	/**
	 * Returns a factory to create target machines when configuring {@link #getTargetMachines()}.
	 *
	 * @return a {@link TargetMachineFactory} for creating {@link TargetMachine} instance, never null.
	 * @since 0.4
	 */
	TargetMachineFactory getMachines();
}
