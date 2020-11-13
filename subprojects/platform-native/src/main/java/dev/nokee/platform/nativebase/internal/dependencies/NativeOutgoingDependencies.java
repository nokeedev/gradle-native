package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.Binary;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

public interface NativeOutgoingDependencies {
	ConfigurableFileCollection getExportedHeaders();
	RegularFileProperty getExportedSwiftModule();
	Property<Binary> getExportedBinary();
}
