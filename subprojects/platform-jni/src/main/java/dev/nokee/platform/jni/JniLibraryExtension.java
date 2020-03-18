package dev.nokee.platform.jni;

import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.nativebase.TargetMachine;
import dev.nokee.platform.nativebase.TargetMachineFactory;
import org.gradle.api.Action;
import org.gradle.api.provider.SetProperty;

/**
 * Configuration for a Java Native Interface (JNI) library, defining the dependencies that make up the library plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the JNI Library Plugin.</p>
 *
 * @since 0.1
 */
public interface JniLibraryExtension {
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
	 */
	VariantView<? extends JniLibrary> getVariants();
}
