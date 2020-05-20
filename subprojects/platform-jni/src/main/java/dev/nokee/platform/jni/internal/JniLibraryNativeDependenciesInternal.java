package dev.nokee.platform.jni.internal;

import dev.nokee.platform.base.internal.NamingScheme;
import dev.nokee.platform.jni.JniLibraryNativeDependencies;
import dev.nokee.platform.nativebase.internal.ConfigurationUtils;
import dev.nokee.runtime.nativebase.internal.ArtifactSerializationTypes;
import dev.nokee.runtime.nativebase.internal.LibraryElements;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ConfigurationContainer;
import org.gradle.api.artifacts.ExternalModuleDependency;
import org.gradle.api.artifacts.dsl.DependencyHandler;
import org.gradle.api.model.ObjectFactory;

import javax.inject.Inject;
import java.util.Map;

// TODO: Add tests for linkOnly and runtimeOnly
// TODO: Add tests for per variant dependencies
public abstract class JniLibraryNativeDependenciesInternal implements JniLibraryNativeDependencies {
	private final Configuration nativeImplementationDependencies;
	private final Configuration nativeLinkOnly;
	private final Configuration nativeRuntimeOnly;

	@Inject
	protected abstract DependencyHandler getDependencies();

	@Inject
	protected abstract ObjectFactory getObjects();

	@Inject
	protected abstract ConfigurationContainer getConfigurations();

	@Inject
	public JniLibraryNativeDependenciesInternal(NamingScheme names) {
		nativeImplementationDependencies = getConfigurations().create(names.getConfigurationName("nativeImplementation"), ConfigurationUtils::configureAsBucket);
		nativeLinkOnly = getConfigurations().create(names.getConfigurationName("nativeLinkOnly"), ConfigurationUtils::configureAsBucket);
		nativeRuntimeOnly = getConfigurations().create(names.getConfigurationName("nativeRuntimeOnly"), ConfigurationUtils::configureAsBucket);
	}

	public JniLibraryNativeDependenciesInternal() {
		nativeImplementationDependencies = getConfigurations().create("nativeImplementation", ConfigurationUtils::configureAsBucket);
		nativeLinkOnly = getConfigurations().create("nativeLinkOnly", ConfigurationUtils::configureAsBucket);
		nativeRuntimeOnly = getConfigurations().create("nativeRuntimeOnly", ConfigurationUtils::configureAsBucket);
	}

	@Override
	public void nativeImplementation(Object notation) {
		if (isFrameworkDependency(notation)) {
			nativeImplementation(notation, requestFramework());
		} else {
			nativeImplementationDependencies.getDependencies().add(getDependencies().create(notation));
		}
	}

	@Override
	public void nativeImplementation(Object notation, Action<? super ExternalModuleDependency> action) {
		ExternalModuleDependency dependency = (ExternalModuleDependency) getDependencies().create(notation);
		action.execute(dependency);
		if (isFrameworkDependency(notation)) {
			requestFramework().execute(dependency);
		}
		nativeImplementationDependencies.getDependencies().add(dependency);
	}

	@Override
	public void nativeLinkOnly(Object notation) {
		if (isFrameworkDependency(notation)) {
			nativeLinkOnly(notation, requestFramework());
		} else {
			nativeLinkOnly.getDependencies().add(getDependencies().create(notation));
		}
	}

	@Override
	public void nativeLinkOnly(Object notation, Action<? super ExternalModuleDependency> action) {
		ExternalModuleDependency dependency = (ExternalModuleDependency) getDependencies().create(notation);
		action.execute(dependency);
		if (isFrameworkDependency(notation)) {
			requestFramework().execute(dependency);
		}
		nativeLinkOnly.getDependencies().add(dependency);
	}

	@Override
	public void nativeRuntimeOnly(Object notation) {
		if (isFrameworkDependency(notation)) {
			nativeRuntimeOnly(notation, requestFramework());
		} else {
			nativeRuntimeOnly.getDependencies().add(getDependencies().create(notation));
		}
	}

	@Override
	public void nativeRuntimeOnly(Object notation, Action<? super ExternalModuleDependency> action) {
		ExternalModuleDependency dependency = (ExternalModuleDependency) getDependencies().create(notation);
		action.execute(dependency);
		if (isFrameworkDependency(notation)) {
			requestFramework().execute(dependency);
		}
		nativeRuntimeOnly.getDependencies().add(dependency);
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

	public Configuration getNativeDependencies() {
		return nativeImplementationDependencies;
	}

	public Configuration getNativeLinkOnlyDependencies() {
		return nativeLinkOnly;
	}

	public Configuration getNativeRuntimeOnlyDependencies() {
		return nativeRuntimeOnly;
	}

	public JniLibraryNativeDependenciesInternal extendsFrom(JniLibraryNativeDependenciesInternal dependencies) {
		getNativeDependencies().extendsFrom(dependencies.getNativeDependencies());
		getNativeLinkOnlyDependencies().extendsFrom(dependencies.getNativeLinkOnlyDependencies());
		getNativeRuntimeOnlyDependencies().extendsFrom(dependencies.getNativeRuntimeOnlyDependencies());
		return this;
	}
}
