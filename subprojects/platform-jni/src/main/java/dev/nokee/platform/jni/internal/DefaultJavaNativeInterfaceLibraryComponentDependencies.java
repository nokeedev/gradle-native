package dev.nokee.platform.jni.internal;

import dev.nokee.platform.base.ComponentDependencies;
import dev.nokee.platform.base.DeclarableDependencyBucket;
import dev.nokee.platform.base.DependencyBucketName;
import dev.nokee.platform.base.internal.dependencies.BaseComponentDependenciesContainer;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesContainer;
import dev.nokee.platform.base.internal.dependencies.DeclarableDependencies;
import dev.nokee.platform.jni.JavaNativeInterfaceLibraryComponentDependencies;
import lombok.Getter;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;

public final class DefaultJavaNativeInterfaceLibraryComponentDependencies extends BaseComponentDependenciesContainer implements JavaNativeInterfaceLibraryComponentDependencies, ComponentDependencies {
	@Getter private final DeclarableDependencyBucket api;
	@Getter private final DeclarableDependencyBucket jvmImplementation;
	@Getter private final DeclarableDependencyBucket jvmRuntimeOnly;
	@Getter private final ConsumableJvmApiElements apiElements;
	@Getter private final ConsumableJvmRuntimeElements runtimeElements;
	private final DefaultJavaNativeInterfaceNativeComponentDependencies nativeComponentDependencies;

	public DefaultJavaNativeInterfaceLibraryComponentDependencies(ComponentDependenciesContainer delegate) {
		super(delegate);
		this.nativeComponentDependencies = new DefaultJavaNativeInterfaceNativeComponentDependencies(delegate);
		this.api = delegate.register(DependencyBucketName.of("api"), DeclarableDependencies.class, this::configureApiBucket);
		this.jvmImplementation = delegate.register(DependencyBucketName.of("jvmImplementation"), DeclarableDependencies.class, this::configureImplementationBucket);
		this.jvmRuntimeOnly = delegate.register(DependencyBucketName.of("jvmRuntimeOnly"), DeclarableDependencies.class, this::configureRuntimeOnlyBucket);
		this.apiElements = delegate.register(DependencyBucketName.of("apiElements"), ConsumableJvmApiElements.class, this::configureApiElements);
		this.runtimeElements = delegate.register(DependencyBucketName.of("runtimeElements"), ConsumableJvmRuntimeElements.class, this::configureRuntimeElements);
	}

	private void configureApiElements(ConsumableJvmApiElements bucket) {
		bucket.extendsFrom(api);
	}

	private void configureRuntimeElements(ConsumableJvmRuntimeElements bucket) {
		bucket.extendsFrom(api);
	}

	private void configureApiBucket(DeclarableDependencies bucket) {}

	private void configureImplementationBucket(DeclarableDependencies bucket) {
		bucket.extendsFrom(api);
	}

	private void configureRuntimeOnlyBucket(DeclarableDependencies bucket) {
		bucket.extendsFrom(jvmImplementation);
	}

	@Override
	public void api(Object notation) {
		api.addDependency(notation);
	}

	@Override
	public void api(Object notation, Action<? super ModuleDependency> action) {
		api.addDependency(notation, action);
	}

	@Override
	public void jvmImplementation(Object notation) {
		jvmImplementation.addDependency(notation);
	}

	@Override
	public void jvmImplementation(Object notation, Action<? super ModuleDependency> action) {
		jvmImplementation.addDependency(notation, action);
	}

	@Override
	public void jvmRuntimeOnly(Object notation) {
		jvmRuntimeOnly.addDependency(notation);
	}

	@Override
	public void jvmRuntimeOnly(Object notation, Action<? super ModuleDependency> action) {
		jvmRuntimeOnly.addDependency(notation, action);
	}

	@Override
	public void nativeImplementation(Object notation) {
		nativeComponentDependencies.nativeImplementation(notation);
	}

	@Override
	public void nativeImplementation(Object notation, Action<? super ModuleDependency> action) {
		nativeComponentDependencies.nativeImplementation(notation, action);
	}

	@Override
	public void nativeLinkOnly(Object notation) {
		nativeComponentDependencies.nativeLinkOnly(notation);
	}

	@Override
	public void nativeLinkOnly(Object notation, Action<? super ModuleDependency> action) {
		nativeComponentDependencies.nativeLinkOnly(notation, action);
	}

	@Override
	public void nativeRuntimeOnly(Object notation) {
		nativeComponentDependencies.nativeRuntimeOnly(notation);
	}

	@Override
	public void nativeRuntimeOnly(Object notation, Action<? super ModuleDependency> action) {
		nativeComponentDependencies.nativeRuntimeOnly(notation, action);
	}

	@Override
	public DeclarableDependencyBucket getNativeImplementation() {
		return nativeComponentDependencies.getNativeImplementation();
	}

	@Override
	public DeclarableDependencyBucket getNativeRuntimeOnly() {
		return nativeComponentDependencies.getNativeRuntimeOnly();
	}

	@Override
	public DeclarableDependencyBucket getNativeLinkOnly() {
		return nativeComponentDependencies.getNativeLinkOnly();
	}
}
