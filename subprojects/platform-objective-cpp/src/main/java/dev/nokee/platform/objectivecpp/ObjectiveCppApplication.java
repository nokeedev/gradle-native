package dev.nokee.platform.objectivecpp;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.*;
import dev.nokee.platform.nativebase.*;
import dev.nokee.platform.nativebase.internal.DefaultNativeApplicationComponent;

/**
 * Configuration for an application written in Objective-C++, defining the dependencies that make up the application plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the Objective-C++ Application Plugin.</p>
 *
 * @since 0.5
 */
public interface ObjectiveCppApplication extends ObjectiveCppApplicationExtension, DependencyAwareComponent<NativeApplicationComponentDependencies>, VariantAwareComponent<NativeApplication>, BinaryAwareComponent, TargetMachineAwareComponent, TargetBuildTypeAwareComponent, SourceAwareComponent<ObjectiveCppApplicationSources>, HasPrivateHeaders, HasObjectiveCppSources, BaseNameAwareComponent {
	/**
	 * {@inheritDoc}
	 */
	default NativeApplicationComponentDependencies getDependencies() {
		return ModelNodes.of(this).get(DefaultNativeApplicationComponent.class).getDependencies();
	}
}
