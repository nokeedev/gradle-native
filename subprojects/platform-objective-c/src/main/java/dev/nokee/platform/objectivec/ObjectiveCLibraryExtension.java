package dev.nokee.platform.objectivec;

import dev.nokee.platform.base.BinaryAwareComponent;
import dev.nokee.platform.base.DependencyAwareComponent;
import dev.nokee.platform.base.SourceAwareComponent;
import dev.nokee.platform.base.VariantAwareComponent;
import dev.nokee.platform.nativebase.*;

/**
 * Configuration for a library written in Objective-C, defining the dependencies that make up the library plus other settings.
 *
 * <p>An instance of this type is added as a project extension by the Objective-C Library Plugin.</p>
 *
 * @since 0.4
 */
@Deprecated // Use ObjectiveCLibrary instead
public interface ObjectiveCLibraryExtension extends DependencyAwareComponent<NativeLibraryComponentDependencies>, VariantAwareComponent<NativeLibrary>, BinaryAwareComponent, TargetMachineAwareComponent, TargetLinkageAwareComponent, TargetBuildTypeAwareComponent, SourceAwareComponent<ObjectiveCLibrarySources>, HasPrivateHeaders, HasPublicHeaders, HasObjectiveCSources {}
