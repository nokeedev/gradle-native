package dev.nokee.platform.ios;

import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import dev.nokee.platform.swift.HasSwiftSources;

@Deprecated // Use SwiftIosApplication instead.
public interface SwiftIosApplicationExtension extends DependencyAwareComponent<NativeComponentDependencies>, VariantAwareComponent<IosApplication>, BinaryAwareComponent, SourceAwareComponent<SwiftIosApplicationSources>, HasSwiftSources, HasIosResources {}
