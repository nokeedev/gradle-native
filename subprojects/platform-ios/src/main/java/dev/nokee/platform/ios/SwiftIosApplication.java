package dev.nokee.platform.ios;

import dev.nokee.model.internal.core.ModelNodes;
import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.ios.internal.DefaultIosApplicationComponent;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.swift.HasSwiftSources;

public interface SwiftIosApplication extends SwiftIosApplicationExtension, DependencyAwareComponent<NativeComponentDependencies>, VariantAwareComponent<IosApplication>, BinaryAwareComponent, SourceAwareComponent<SwiftIosApplicationSources>, HasSwiftSources, HasIosResources {
	default NativeComponentDependencies getDependencies() {
		return ModelNodes.of(this).get(DefaultIosApplicationComponent.class).getDependencies();
	}
}
