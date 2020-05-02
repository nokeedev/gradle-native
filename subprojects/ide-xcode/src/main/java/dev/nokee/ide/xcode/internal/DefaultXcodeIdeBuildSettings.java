package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.xcode.XcodeIdeBuildSettings;
import lombok.NonNull;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;

public abstract class DefaultXcodeIdeBuildSettings implements XcodeIdeBuildSettings {
	public abstract MapProperty<String, Object> getElements();

	@Override
	public XcodeIdeBuildSettings put(@NonNull String name, @NonNull Provider<Object> value) {
		getElements().put(name, value);
		return this;
	}

	@Override
	public XcodeIdeBuildSettings put(@NonNull String name, @NonNull Object value) {
		getElements().put(name, value);
		return this;
	}
}
