package dev.nokee.platform.nativebase;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.*;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;

public interface NativeLibraryExtension extends DependencyAwareComponent<NativeLibraryComponentDependencies>, VariantAwareComponent<NativeLibrary>, BinaryAwareComponent, TargetMachineAwareComponent, TargetLinkageAwareComponent, TargetBuildTypeAwareComponent, SourceAwareComponent<NativeLibrarySources>, BaseNameAwareComponent {
	/**
	 * {@inheritDoc}
	 */
	default NativeLibraryComponentDependencies getDependencies() {
		return ModelNodes.of(this).get(DefaultNativeLibraryComponent.class).getDependencies();
	}
}
