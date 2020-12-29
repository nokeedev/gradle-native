package dev.nokee.platform.objectivec;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.*;
import dev.nokee.platform.nativebase.*;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;

/**
 * Configuration for a library written in Objective-C, defining the dependencies that make up the library plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the Objective-C Library Plugin.</p>
 *
 * @since 0.5
 */
public interface ObjectiveCLibrary extends ObjectiveCLibraryExtension, DependencyAwareComponent<NativeLibraryComponentDependencies>, VariantAwareComponent<NativeLibrary>, BinaryAwareComponent, TargetMachineAwareComponent, TargetLinkageAwareComponent, TargetBuildTypeAwareComponent, SourceAwareComponent<ObjectiveCLibrarySources>, HasPrivateHeaders, HasPublicHeaders, HasObjectiveCSources, BaseNameAwareComponent {
	default NativeLibraryComponentDependencies getDependencies() {
		return ModelNodes.of(this).get(DefaultNativeLibraryComponent.class).getDependencies();
	}
}
