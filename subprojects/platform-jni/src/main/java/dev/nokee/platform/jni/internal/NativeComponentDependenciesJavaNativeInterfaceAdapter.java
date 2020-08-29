package dev.nokee.platform.jni.internal;

import dev.nokee.platform.base.DeclarableDependencyBucket;
import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.DependencyBucketName;
import dev.nokee.platform.base.internal.dependencies.*;
import dev.nokee.platform.jni.JavaNativeInterfaceNativeComponentDependencies;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.artifacts.ModuleDependency;

import java.util.Optional;

public final class NativeComponentDependenciesJavaNativeInterfaceAdapter extends BaseComponentDependenciesContainer implements NativeComponentDependencies {
	private final JavaNativeInterfaceNativeComponentDependencies delegate;

	public NativeComponentDependenciesJavaNativeInterfaceAdapter(JavaNativeInterfaceNativeComponentDependencies delegate) {
		super((ComponentDependenciesContainer) delegate);
		this.delegate = delegate;
	}

	@Override
	public <T extends DependencyBucket> T register(DependencyBucketName name, Class<T> type) {
		return super.register(prefixIfNotHeaderSearchPaths(name), type);
	}

	@Override
	public <T extends DependencyBucket> T register(DependencyBucketName name, Class<T> type, Action<? super T> action) {
		return super.register(prefixIfNotHeaderSearchPaths(name), type, action);
	}

	@Override
	public Optional<DependencyBucket> findByName(DependencyBucketName name) {
		return super.findByName(DependencyBucketName.of(prefixWithNative(name.get())));
	}

	private static DependencyBucketName prefixIfNotHeaderSearchPaths(DependencyBucketName name) {
		if ("headerSearchPaths".equals(name.get())) {
			return name;
		}
		return DependencyBucketName.of(prefixWithNative(name.get()));
	}

	private static String prefixWithNative(String target) {
		return "native" + StringUtils.capitalize(target);
	}

	@Override
	public void implementation(Object notation) {
		delegate.nativeImplementation(notation);
	}

	@Override
	public void implementation(Object notation, Action<? super ModuleDependency> action) {
		delegate.nativeImplementation(notation, action);
	}

	@Override
	public void compileOnly(Object notation) {
		getCompileOnly().addDependency(notation);
	}

	@Override
	public void compileOnly(Object notation, Action<? super ModuleDependency> action) {
		getCompileOnly().addDependency(notation, action);
	}

	@Override
	public void linkOnly(Object notation) {
		delegate.nativeLinkOnly(notation);
	}

	@Override
	public void linkOnly(Object notation, Action<? super ModuleDependency> action) {
		delegate.nativeLinkOnly(notation, action);
	}

	@Override
	public void runtimeOnly(Object notation) {
		delegate.nativeRuntimeOnly(notation);
	}

	@Override
	public void runtimeOnly(Object notation, Action<? super ModuleDependency> action) {
		delegate.nativeRuntimeOnly(notation, action);
	}

	@Override
	public DeclarableDependencyBucket getImplementation() {
		return delegate.getNativeImplementation();
	}

	@Override
	public DeclarableDependencyBucket getRuntimeOnly() {
		return delegate.getNativeRuntimeOnly();
	}

	@Override
	public DeclarableDependencyBucket getCompileOnly() {
		// FIXME: findByName(Name, Type)
		val compileOnly = ((ComponentDependenciesContainer)delegate).findByName(DependencyBucketName.of("nativeCompileOnly"));
		if (compileOnly.isPresent()) {
			return (DeclarableDependencyBucket) compileOnly.get();
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public DeclarableDependencyBucket getLinkOnly() {
		return delegate.getNativeLinkOnly();
	}
}
