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
import java.util.Set;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.descendentOf;
import static dev.nokee.utils.ActionUtils.onlyIf;
import static dev.nokee.utils.TransformerUtils.toSetTransformer;

public abstract class AbstractDomainObjectView<TYPE, T extends TYPE> extends GroovyObjectSupport {
	protected final DomainObjectIdentifier viewOwner;
	protected final Class<T> viewElementType;
	private final Provider<Set<T>> elementsProvider;
	protected final DomainObjectConfigurer<TYPE> configurer;
	private final DomainObjectViewFactory<TYPE> viewFactory;

	protected AbstractDomainObjectView(DomainObjectIdentifier viewOwner, Class<T> viewElementType, RealizableDomainObjectRepository<TYPE> repository, DomainObjectConfigurer<TYPE> configurer, DomainObjectViewFactory<TYPE> viewFactory) {
		this.viewOwner = viewOwner;
		this.viewElementType = viewElementType;
		this.elementsProvider = viewElements(repository, viewOwner, viewElementType);
		this.configurer = configurer;
		this.viewFactory = viewFactory;
	}

	private static <TYPE, T extends TYPE> Provider<Set<T>> viewElements(RealizableDomainObjectRepository<TYPE> repository, DomainObjectIdentifier viewOwner, Class<T> viewElementType) {
		return repository
			.filtered(descendentOf(viewOwner).and(DomainObjectIdentifierUtils.withType(viewElementType)))
			.map(toSetTransformer(viewElementType));
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

	public Provider<Set<T>> getElements() {
		return elementsProvider;
	}

	public Set<T> get() {
		return elementsProvider.get();
	}

	public <S> Provider<List<S>> map(Transformer<? extends S, ? super T> mapper) {
		return elementsProvider.map(ProviderUtils.map(mapper));
	}

	public <S> Provider<List<S>> flatMap(Transformer<Iterable<? extends S>, ? super T> mapper) {
		return elementsProvider.map(ProviderUtils.flatMap(mapper));
	}

	public Provider<List<T>> filter(Spec<? super T> spec) {
		return getElements().map(ProviderUtils.filter(spec));
	}

	public <S extends T> DomainObjectView<S> withType(Class<S> type) {
		return viewFactory.create(viewOwner, type);
	}

	protected class ConfigureDirectlyOwnedSourceSetByNameMethodInvoker {
		private final HasConfigureElementByNameSupport<T> thiz;

		public ConfigureDirectlyOwnedSourceSetByNameMethodInvoker(HasConfigureElementByNameSupport<T> thiz) {
			this.thiz = thiz;
		}

		public Object invokeMethod(String name, Object args) {
			val argsArray = (Object[]) args;
			if (argsArray.length == 1 && argsArray[0] instanceof Closure) {
				thiz.configure(name, ConfigureUtil.configureUsing((Closure<Void>) argsArray[0]));
				return null;
			} else if (argsArray.length == 2 && argsArray[0] instanceof Class && argsArray[1] instanceof Closure) {
				thiz.configure(name, (Class) argsArray[0], ConfigureUtil.configureUsing((Closure<Void>) argsArray[1]));
				return null;
			}
			return AbstractDomainObjectView.super.invokeMethod(name, args);
		}
	}
}
