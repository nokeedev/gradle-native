package dev.nokee.ide.visualstudio.internal;

import com.google.common.collect.ImmutableMap;
import dev.nokee.ide.visualstudio.VisualStudioIdePropertyGroup;
import lombok.Getter;
import lombok.NonNull;
import org.gradle.api.provider.MapProperty;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

public abstract class DefaultVisualStudioIdePropertyGroup implements VisualStudioIdePropertyGroup {
	// Workaround for https://github.com/gradle/gradle/issues/13405
	@Getter private final List<Provider<Object>> taskProviders = new ArrayList<>();
	public abstract MapProperty<String, Object> getElements();

	@Inject
	public abstract ProviderFactory getProviders();

	@Override
	public VisualStudioIdePropertyGroup put(@NonNull String name, @NonNull Provider<Object> value) {
		taskProviders.add(value);
		getElements().putAll(getProviders().provider(() -> {
			if (value.isPresent()) {
				return ImmutableMap.of(name, value.get());
			}
			return ImmutableMap.of();
		}));
		return this;
	}

	@Override
	public VisualStudioIdePropertyGroup put(@NonNull String name, @NonNull Object value) {
		getElements().put(name, value);
		return this;
	}
}
