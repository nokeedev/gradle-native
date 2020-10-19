package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectView;
import dev.nokee.utils.ProviderUtils;
import groovy.lang.Closure;
import groovy.lang.GroovyObjectSupport;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;
import org.gradle.util.ConfigureUtil;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.directlyOwnedBy;
import static dev.nokee.model.internal.DomainObjectIdentifierUtils.named;
import static dev.nokee.utils.ActionUtils.onlyIf;

public abstract class AbstractDomainObjectView<TYPE, T extends TYPE> extends GroovyObjectSupport {
	protected final DomainObjectIdentifier viewOwner;
	protected final Class<T> viewElementType;
	private final Provider<Set<? extends T>> elementsProvider;
	protected final DomainObjectConfigurer<TYPE> configurer;
	private final DomainObjectViewFactory<TYPE> viewFactory;

	protected AbstractDomainObjectView(DomainObjectIdentifier viewOwner, Class<T> viewElementType, Provider<Set<? extends T>> elementsProvider, DomainObjectConfigurer<TYPE> configurer, DomainObjectViewFactory<TYPE> viewFactory) {
		this.viewOwner = viewOwner;
		this.viewElementType = viewElementType;
		this.elementsProvider = elementsProvider;
		this.configurer = configurer;
		this.viewFactory = viewFactory;
	}

	public void configureEach(Action<? super T> action) {
		configurer.configureEach(viewOwner, viewElementType, action);
	}

	public <S extends T> void configureEach(Class<S> type, Action<? super S> action) {
		configurer.configureEach(viewOwner, type, action);
	}

	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		configurer.configureEach(viewOwner, viewElementType, onlyIf(spec, action));
	}

	public Provider<Set<? extends T>> getElements() {
		return elementsProvider;
	}

	public Set<? extends T> get() {
		return elementsProvider.get();
	}

	public <S> Provider<List<? extends S>> map(Transformer<? extends S, ? super T> mapper) {
		return elementsProvider.map(ProviderUtils.map(mapper));
	}

	public <S> Provider<List<? extends S>> flatMap(Transformer<Iterable<? extends S>, ? super T> mapper) {
		return elementsProvider.map(ProviderUtils.flatMap(mapper));
	}

	public Provider<List<? extends T>> filter(Spec<? super T> spec) {
		return getElements().map(ProviderUtils.filter(spec));
	}

	public <S extends T> DomainObjectView<S> withType(Class<S> type) {
		return viewFactory.create(viewOwner, type);
	}

	protected class ConfigureDirectlyOwnedSourceSetByNameMethodInvoker {
		private final RealizableDomainObjectRepository<TYPE> repository;

		public ConfigureDirectlyOwnedSourceSetByNameMethodInvoker(RealizableDomainObjectRepository<TYPE> repository) {
			this.repository = repository;
		}

		private Optional<TypeAwareDomainObjectIdentifier<? extends TYPE>> findByName(String name) {
			return repository.findKnownIdentifier(directlyOwnedBy(viewOwner).and(named(name)));
		}

		public Object invokeMethod(String name, Object args) {
			val argsArray = (Object[])args;
			if (argsArray.length == 1 && argsArray[0] instanceof Closure) {
				val identifier = findByName(name);
				if (identifier.isPresent()) {
					configurer.configure(identifier.get(), ConfigureUtil.configureUsing((Closure<Void>)argsArray[0]));
					return null;
				}
			}
			return AbstractDomainObjectView.super.invokeMethod(name, args);
		}
	}
}
