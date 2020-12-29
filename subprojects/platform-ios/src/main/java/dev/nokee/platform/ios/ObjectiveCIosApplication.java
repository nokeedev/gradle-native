package dev.nokee.platform.ios;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.*;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.nativebase.HasPrivateHeaders;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.objectivec.HasObjectiveCSources;

public interface ObjectiveCIosApplication extends ObjectiveCIosApplicationExtension, DependencyAwareComponent<NativeComponentDependencies>, VariantAwareComponent<IosApplication>, BinaryAwareComponent, SourceAwareComponent<ObjectiveCIosApplicationSources>, HasObjectiveCSources, HasPrivateHeaders, HasIosResources, BaseNameAwareComponent {
	default NativeComponentDependencies getDependencies() {
		return ModelNodes.of(this).get(DefaultIosApplicationComponent.class).getDependencies();
	}
}
