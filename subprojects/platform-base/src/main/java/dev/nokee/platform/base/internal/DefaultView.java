package dev.nokee.platform.base.internal;

import com.google.common.base.Preconditions;
import dev.nokee.platform.base.DomainObjectElement;
import dev.nokee.platform.base.DomainObjectElementConfigurer;
import dev.nokee.platform.base.View;
import dev.nokee.utils.Cast;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.provider.ProviderFactory;
import org.gradle.api.specs.Spec;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

public class DefaultView<T> extends AbstractView<T> implements View<T> {
	private final org.gradle.api.DomainObjectCollection<DomainObjectElement<T>> delegate;
	private final DomainObjectElementFilter<T> filter;
	@Getter(AccessLevel.PROTECTED) private final ProviderFactory providers;
	private final DomainObjectElementConfigurer<T> elementConfigurer;

	public DefaultView(org.gradle.api.DomainObjectCollection<DomainObjectElement<T>> delegate, DomainObjectElementFilter<T> filter, ProviderFactory providers) {
		this.delegate = delegate;
		this.filter = filter;
		this.providers = providers;
		this.elementConfigurer = new DefaultDomainObjectElementConfigurer<>(delegate, filter);
	}

	@Override
	public void configureEach(Action<? super T> action) {
		Preconditions.checkArgument(action != null, "configure each action for variant view must not be null");
		elementConfigurer.configureEach(action);
	}

	@Override
	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		Preconditions.checkArgument(action != null, "configure each action for variant view must not be null");
		elementConfigurer.configureEach(type, action);
	}

	@Override
	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		elementConfigurer.configureEach(spec, action);
	}

	@Override
	public <S extends T> View<S> withType(Class<S> type) {
		Preconditions.checkArgument(type != null, "variant view subview type must not be null");
		return new DefaultView<>(Cast.uncheckedCast("", delegate), DomainObjectElementFilter.typed(type), providers);
	}

	@Override
	public Set<? extends T> get() {
		val result = new LinkedHashSet<T>();
		delegate.stream().forEach(element -> filter.filter(it -> result.add(it.get())).execute(element));
		return Collections.unmodifiableSet(result);
	}

	@Override
	protected String getDisplayName() {
		return null;
	}
}
