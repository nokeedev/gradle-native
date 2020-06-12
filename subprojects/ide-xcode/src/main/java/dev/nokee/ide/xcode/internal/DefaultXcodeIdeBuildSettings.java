package dev.nokee.ide.xcode.internal;

import dev.nokee.ide.xcode.XcodeIdeBuildSettings;
import lombok.Getter;
import lombok.NonNull;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;

import java.util.ArrayList;
import java.util.List;

public abstract class DefaultXcodeIdeBuildSettings implements XcodeIdeBuildSettings {
	// Workaround for https://github.com/gradle/gradle/issues/13405
	@Getter private final List<Provider<Object>> providers = new ArrayList<>();
	public abstract MapProperty<String, Object> getElements();

	@Override
	public XcodeIdeBuildSettings put(@NonNull String name, @NonNull Provider<Object> value) {
		providers.add(value);
		getElements().put(name, value);
		return this;
	}

	@Override
	public XcodeIdeBuildSettings put(@NonNull String name, @NonNull Object value) {
		getElements().put(name, value);
		return this;
	}
}
