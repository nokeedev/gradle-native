package dev.nokee.platform.jni.internal;

import dev.nokee.platform.base.DependencyBucket;
import dev.nokee.platform.base.internal.dependencies.BaseComponentDependencies;
import dev.nokee.platform.base.internal.dependencies.ComponentDependenciesInternal;
import dev.nokee.platform.jni.JavaNativeInterfaceNativeComponentDependencies;
import dev.nokee.platform.nativebase.NativeComponentDependencies;
import lombok.val;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Action;
import org.gradle.api.artifacts.Configuration;
import org.gradle.api.artifacts.ModuleDependency;

import java.util.Optional;

public class NativeComponentDependenciesJavaNativeInterfaceAdapter extends BaseComponentDependencies implements NativeComponentDependencies {
	private final JavaNativeInterfaceNativeComponentDependencies delegate;

	public NativeComponentDependenciesJavaNativeInterfaceAdapter(JavaNativeInterfaceNativeComponentDependencies delegate) {
		super((ComponentDependenciesInternal) delegate);
		this.delegate = delegate;
	}

	@Override
	public DependencyBucket create(String name) {
		if ("headerSearchPaths".equals(name)) {
			return super.create(name);
		}
		return super.create(prefixWithNative(name));
	}

	@Override
	public DependencyBucket create(String name, Action<Configuration> action) {
		if ("headerSearchPaths".equals(name)) {
			return super.create(name, action);
		}
		return super.create(prefixWithNative(name), action);
	}

	@Override
	public Optional<DependencyBucket> findByName(String name) {
		return super.findByName(prefixWithNative(name));
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
	public DependencyBucket getImplementation() {
		return delegate.getNativeImplementation();
	}

	@Override
	public DependencyBucket getRuntimeOnly() {
		return delegate.getNativeRuntimeOnly();
	}

	@Override
	public DependencyBucket getCompileOnly() {
		val compileOnly = ((ComponentDependenciesInternal)delegate).findByName("nativeCompileOnly");
		if (compileOnly.isPresent()) {
			return compileOnly.get();
		} else {
			throw new UnsupportedOperationException();
		}
	}

	@Override
	public DependencyBucket getLinkOnly() {
		return delegate.getNativeLinkOnly();
	}
}
