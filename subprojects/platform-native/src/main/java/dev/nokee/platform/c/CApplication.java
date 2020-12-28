package dev.nokee.platform.c;

import dev.nokee.language.c.CSourceSet;
import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.nativebase.*;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;

/**
 * Configuration for an application written in C, defining the dependencies that make up the application plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the C Application Plugin.</p>
 *
 * @since 0.5
 */
public interface CApplication extends CApplicationExtension, DependencyAwareComponent<NativeApplicationComponentDependencies>, VariantAwareComponent<NativeApplication>, BinaryAwareComponent, TargetMachineAwareComponent, TargetBuildTypeAwareComponent, SourceAwareComponent<CApplicationSources>, HasPrivateHeaders, HasCSources {
	default NativeApplicationComponentDependencies getDependencies() {
		return ModelNodes.of(this).get(DefaultNativeApplicationComponent.class).getDependencies();
	}

	// For Groovy DSL
	default CSourceSet getcSources() {
		return getCSources();
	}
}
