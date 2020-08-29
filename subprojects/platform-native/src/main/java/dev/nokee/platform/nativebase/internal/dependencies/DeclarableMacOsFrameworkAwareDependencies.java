package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.internal.dependencies.BaseDeclarableDependencyBucket;
import dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes;
import dev.nokee.runtime.nativebase.internal.DefaultLibraryElements;
import dev.nokee.runtime.nativebase.internal.LibraryElements;
import dev.nokee.utils.ActionUtils;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;

import javax.inject.Inject;
import java.util.Map;
import java.util.Objects;

public final class DeclarableMacOsFrameworkAwareDependencies extends BaseDeclarableDependencyBucket {
	private static final String NOKEE_MAGIC_FRAMEWORK_GROUP = "dev.nokee.framework";

	@Inject
	public DeclarableMacOsFrameworkAwareDependencies() {}

	@Override
	public void addDependency(Object notation) {
		if (isFrameworkNotation(notation)) {
			super.addDependency(notation, DeclarableMacOsFrameworkAwareDependencies::requestFramework);
		} else {
			super.addDependency(notation);
		}
	}

	@Override
	public void addDependency(Object notation, Action<? super ModuleDependency> action) {
		if (isFrameworkNotation(notation)) {
			super.addDependency(notation, ActionUtils.composite(DeclarableMacOsFrameworkAwareDependencies::requestFramework, action));
		} else {
			super.addDependency(notation, action);
		}
	}

	private static void requestFramework(ModuleDependency dependency) {
		dependency.attributes(attributes -> {
			attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, new DefaultLibraryElements(LibraryElements.FRAMEWORK_BUNDLE));
			attributes.attribute(ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, ArtifactSerializationTypes.DESERIALIZED);
		});
	}

	private static boolean isFrameworkNotation(Object notation) {
		if (notation instanceof String) {
			return ((String) notation).startsWith(NOKEE_MAGIC_FRAMEWORK_GROUP);
		} else if (notation instanceof Map) {
			return Objects.equals(((Map<?, ?>) notation).get("group"), NOKEE_MAGIC_FRAMEWORK_GROUP);
		}
		return false;
	}
}
