package dev.nokee.platform.base.internal;

import com.google.common.base.Preconditions;
import dev.nokee.utils.ProviderUtils;
import lombok.AccessLevel;
import lombok.Getter;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.Set;

public abstract class AbstractView<T> {
	@Getter(AccessLevel.PROTECTED)
	private final ProviderFactory providers;

	protected AbstractView(ProviderFactory providers) {
		this.providers = providers;
	}

	protected abstract Set<? extends T> get();

	public Provider<Set<? extends T>> getElements() {
		return getProviders().provider(this::get);
	}

	public <S> Provider<List<? extends S>> map(Transformer<? extends S, ? super T> mapper) {
		Preconditions.checkArgument(mapper != null, "map mapper for %s must not be null", getDisplayName());
		return getElements().map(ProviderUtils.map(mapper));
	}

	public <S> Provider<List<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super T> mapper) {
		Preconditions.checkArgument(mapper != null, "flatMap mapper for %s must not be null", getDisplayName());
		return getElements().map(ProviderUtils.flatMap(mapper));
	}

	public Provider<List<? extends T>> filter(Spec<? super T> spec) {
		Preconditions.checkArgument(spec != null, "filter spec for %s must not be null", getDisplayName());
		return getElements().map(ProviderUtils.filter(spec));
	}

	protected abstract String getDisplayName();
}
