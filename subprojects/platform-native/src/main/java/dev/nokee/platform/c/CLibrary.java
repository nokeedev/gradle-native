package dev.nokee.platform.c;

import dev.nokee.language.c.CSourceSet;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.*;
import dev.nokee.platform.nativebase.*;
import dev.nokee.platform.nativebase.internal.DefaultNativeLibraryComponent;

/**
 * Configuration for a library written in C, defining the dependencies that make up the library plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the C Library Plugin.</p>
 *
 * @since 0.5
 */
public interface CLibrary extends CLibraryExtension, DependencyAwareComponent<NativeLibraryComponentDependencies>, VariantAwareComponent<NativeLibrary>, BinaryAwareComponent, TargetMachineAwareComponent, TargetLinkageAwareComponent, TargetBuildTypeAwareComponent, SourceAwareComponent<CLibrarySources>, HasPrivateHeaders, HasPublicHeaders, HasCSources, BaseNameAwareComponent {
	default NativeLibraryComponentDependencies getDependencies() {
		return ModelNodes.of(this).get(DefaultNativeLibraryComponent.class).getDependencies();
	}

	// For Groovy DSL
	default CSourceSet getcSources() {
		return getCSources();
	}
}
