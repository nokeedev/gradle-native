package dev.nokee.platform.nativebase.internal.dependencies;

import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes;
import dev.nokee.runtime.nativebase.internal.DefaultLibraryElements;
import dev.nokee.runtime.nativebase.internal.LibraryElements;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

import java.util.Map;
import java.util.Objects;

import static dev.gradleplugins.grava.util.ActionUtils.composite;

public final class FrameworkAwareDependencyBucket implements DependencyBucket {
	private static final String NOKEE_MAGIC_FRAMEWORK_GROUP = "dev.nokee.framework";
	private static final RequestFrameworkAction REQUEST_FRAMEWORK_ACTION = new RequestFrameworkAction();
	private final DependencyBucket delegate;

	public FrameworkAwareDependencyBucket(DependencyBucket delegate) {
		this.delegate = delegate;
	}

	@Override
	public String getName() {
		return delegate.getName();
	}

	@Override
	public void addDependency(Object notation) {
		if (isFrameworkNotation(notation)) {
			delegate.addDependency(notation, REQUEST_FRAMEWORK_ACTION);
		} else {
			delegate.addDependency(notation);
		}
	}

	@Override
	public void addDependency(Object notation, Action<? super ModuleDependency> action) {
		if (isFrameworkNotation(notation)) {
			delegate.addDependency(notation, composite(REQUEST_FRAMEWORK_ACTION, action));
		} else {
			delegate.addDependency(notation, action);
		}
	}

	@Override
	public Configuration getAsConfiguration() {
		return delegate.getAsConfiguration();
	}

	private static class RequestFrameworkAction implements Action<ModuleDependency> {
		@Override
		public void execute(ModuleDependency dependency) {
			dependency.attributes(attributes -> {
				attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, new DefaultLibraryElements(LibraryElements.FRAMEWORK_BUNDLE));
				attributes.attribute(ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, ArtifactSerializationTypes.DESERIALIZED);
			});
		}
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
