package dev.nokee.ide.visualstudio.internal;

import dev.nokee.ide.visualstudio.VisualStudioIdePropertyGroup;
import lombok.Getter;
import lombok.NonNull;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;

import java.util.ArrayList;
import java.util.List;

public abstract class DefaultVisualStudioIdePropertyGroup implements VisualStudioIdePropertyGroup {
	// Workaround for https://github.com/gradle/gradle/issues/13405
	@Getter private final List<Provider<Object>> providers = new ArrayList<>();
	public abstract MapProperty<String, Object> getElements();

	@Override
	public VisualStudioIdePropertyGroup put(@NonNull String name, @NonNull Provider<Object> value) {
		providers.add(value);
		getElements().put(name, value);
		return this;
	}

	@Override
	public VisualStudioIdePropertyGroup put(@NonNull String name, @NonNull Object value) {
		getElements().put(name, value);
		return this;
	}
}
