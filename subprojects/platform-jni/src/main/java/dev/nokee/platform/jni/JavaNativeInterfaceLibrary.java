package dev.nokee.platform.jni;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.*;
import dev.nokee.platform.jni.internal.JniLibraryComponentInternal;
import dev.nokee.platform.nativebase.TargetMachineAwareComponent;
import dev.nokee.runtime.nativebase.TargetMachine;
import org.gradle.api.provider.SetProperty;

/**
 * Configuration for a Java Native Interface (JNI) library, defining the dependencies that make up the library plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the JNI Library Plugin.</p>
 *
 * @since 0.1
 */
public interface JavaNativeInterfaceLibrary extends JniLibraryExtension, DependencyAwareComponent<JavaNativeInterfaceLibraryComponentDependencies>, VariantAwareComponent<JniLibrary>, BinaryAwareComponent, TargetMachineAwareComponent, SourceAwareComponent<JavaNativeInterfaceLibrarySources>, BaseNameAwareComponent {
	/**
	 * Returns the dependencies of this component.
	 *
	 * @return a {@link JavaNativeInterfaceLibraryComponentDependencies}, never null.
	 * @since 0.1
	 */
	default JavaNativeInterfaceLibraryComponentDependencies getDependencies() {
		return ModelNodes.of(this).get(JniLibraryComponentInternal.class).getDependencies();
	}

	@Override
	default SetProperty<TargetMachine> getTargetMachines() {
		return ModelNodes.of(this).get(JniLibraryComponentInternal.class).getTargetMachines();
	}
}
