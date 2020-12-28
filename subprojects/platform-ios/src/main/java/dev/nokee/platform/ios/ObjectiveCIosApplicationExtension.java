package dev.nokee.platform.ios;

import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.nativebase.HasPrivateHeaders;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.objectivec.HasObjectiveCSources;

public interface ObjectiveCIosApplicationExtension extends DependencyAwareComponent<NativeComponentDependencies>, VariantAwareComponent<IosApplication>, BinaryAwareComponent, SourceAwareComponent<ObjectiveCIosApplicationSources>, HasObjectiveCSources, HasPrivateHeaders, HasIosResources {}
