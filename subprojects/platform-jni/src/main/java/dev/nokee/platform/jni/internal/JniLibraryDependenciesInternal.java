package dev.nokee.platform.jni.internal;

import dev.nokee.platform.jni.JniLibraryDependencies;
import dev.nokee.platform.nativebase.internal.ArtifactSerializationTypes;
import dev.nokee.platform.nativebase.internal.LibraryElements;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.util.Map;

public abstract class JniLibraryDependenciesInternal implements JniLibraryDependencies {

	private final Configuration apiDependencies;
	private final Configuration jvmImplementationDependencies;
	private final Configuration nativeImplementationDependencies;

	@Inject
	public JniLibraryDependenciesInternal(ConfigurationContainer configurations) {
		Configuration api = configurations.findByName("api");
		if (api == null) {
			apiDependencies = configurations.create("api", JniLibraryDependenciesInternal::bucket);
		} else {
			apiDependencies = api;
		}
		jvmImplementationDependencies = configurations.create("jvmImplementation", JniLibraryDependenciesInternal::bucket);
		nativeImplementationDependencies = configurations.create("nativeImplementation", JniLibraryDependenciesInternal::bucket);

		jvmImplementationDependencies.extendsFrom(apiDependencies);
	}

	private static void bucket(Configuration configuration) {
		configuration.setCanBeConsumed(false);
		configuration.setCanBeResolved(false);
	}

	@Inject
	protected abstract DependencyHandler getDependencyHandler();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Override
	public void api(Object notation) {
		apiDependencies.getDependencies().add(getDependencyHandler().create(notation));
	}

	@Override
	public void api(Object notation, Action<? super ExternalModuleDependency> action) {
		ExternalModuleDependency dependency = (ExternalModuleDependency) getDependencyHandler().create(notation);
		action.execute(dependency);
		apiDependencies.getDependencies().add(dependency);
	}

	@Override
	public void jvmImplementation(Object notation) {
		jvmImplementationDependencies.getDependencies().add(getDependencyHandler().create(notation));
	}

	@Override
	public void jvmImplementation(Object notation, Action<? super ExternalModuleDependency> action) {
		ExternalModuleDependency dependency = (ExternalModuleDependency) getDependencyHandler().create(notation);
		action.execute(dependency);
		jvmImplementationDependencies.getDependencies().add(dependency);
	}

	@Override
	public void nativeImplementation(Object notation) {
		if (isFrameworkDependency(notation)) {
			nativeImplementation(notation, requestFramework());
		} else {
			nativeImplementationDependencies.getDependencies().add(getDependencyHandler().create(notation));
		}
	}

	@Override
	public void nativeImplementation(Object notation, Action<? super ExternalModuleDependency> action) {
		ExternalModuleDependency dependency = (ExternalModuleDependency) getDependencyHandler().create(notation);
		action.execute(dependency);
		if (isFrameworkDependency(notation)) {
			requestFramework().execute(dependency);
		}
		nativeImplementationDependencies.getDependencies().add(dependency);
	}

	private boolean isFrameworkDependency(Object notation) {
		if (notation instanceof String && ((String)notation).startsWith("dev.nokee.framework:")) {
			return true;
		} else if (notation instanceof Map && ((Map)notation).get("group").equals("dev.nokee.framework")) {
			return true;
		}
		return false;
	}

	private Action<? super ExternalModuleDependency> requestFramework() {
		return dependency -> {
			dependency.attributes(attributes -> {
				attributes.attribute(LibraryElements.LIBRARY_ELEMENTS_ATTRIBUTE, getObjects().named(LibraryElements.class, LibraryElements.FRAMEWORK_BUNDLE));
				attributes.attribute(ArtifactSerializationTypes.ARTIFACT_SERIALIZATION_TYPES_ATTRIBUTE, ArtifactSerializationTypes.DESERIALIZED);
			});
		};
	}

	public Configuration getApiDependencies() {
		return apiDependencies;
	}

	public Configuration getNativeDependencies() {
		return nativeImplementationDependencies;
	}

	public Configuration getJvmDependencies() {
		return jvmImplementationDependencies;
	}
}
