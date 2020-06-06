package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.Binary;
import org.gradle.api.file.DirectoryProperty;
import org.gradle.api.file.RegularFileProperty;
import org.gradle.api.provider.Property;

public interface NativeOutgoingDependencies {
	DirectoryProperty getExportedHeaders();
	RegularFileProperty getExportedSwiftModule();
	Property<Binary> getExportedBinary();
}
