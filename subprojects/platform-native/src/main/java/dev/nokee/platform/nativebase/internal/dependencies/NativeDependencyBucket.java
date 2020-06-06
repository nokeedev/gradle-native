package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes;
import dev.nokee.runtime.nativebase.internal.LibraryElements;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.Dependency;
import org.gradle.api.artifacts.ExternalDependency;
import org.gradle.api.artifacts.ModuleDependency;
import org.gradle.internal.Cast;

import javax.inject.Inject;
import java.util.Objects;

public abstract class NativeDependencyBucket extends DefaultDependencyBucket implements DependencyBucket {
	@Inject
	public NativeDependencyBucket(Configuration bucket) {
		super(bucket);
	}

	@Override
	protected void onNewDependency(Dependency dependency) {
		if (dependency instanceof ExternalDependency && Objects.equals(dependency.getGroup(), "dev.nokee.framework")) {
			requestFramework(Cast.uncheckedCast(dependency));
		}
	}

	private <T extends ModuleDependency> void requestFramework(T dependency) {
		dependency.attributes(attributes -> {
			attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.FRAMEWORK_BUNDLE));
			attributes.attribute(ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, ArtifactSerializationTypes.DESERIALIZED);
		});
	}
}
