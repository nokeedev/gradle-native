package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.model.DomainObjectView;
import dev.nokee.model.internal.dsl.GroovyDslDefaultInvoker;
import dev.nokee.model.internal.dsl.GroovyDslInvoker;
import dev.nokee.model.internal.dsl.GroovyDslViewInvoker;
import groovy.lang.GroovyObjectSupport;
import org.gradle.api.Action;
import org.gradle.api.Transformer;
import org.gradle.api.provider.Provider;
import org.gradle.api.specs.Spec;

import java.util.List;
import java.util.Set;

import static dev.nokee.model.internal.DomainObjectIdentifierUtils.descendentOf;
import static dev.nokee.utils.ActionUtils.onlyIf;
import static dev.nokee.utils.TransformerUtils.*;

public abstract class AbstractDomainObjectView<TYPE, T extends TYPE> extends GroovyObjectSupport {
	protected final DomainObjectIdentifier viewOwner;
	protected final Class<T> viewElementType;
	private final Provider<Set<T>> elementsProvider;
	protected final DomainObjectConfigurer<TYPE> configurer;
	private final DomainObjectViewFactory<TYPE> viewFactory;
	private final GroovyDslInvoker<T> invoker;

	protected AbstractDomainObjectView(DomainObjectIdentifier viewOwner, Class<T> viewElementType, RealizableDomainObjectRepository<TYPE> repository, DomainObjectConfigurer<TYPE> configurer, DomainObjectViewFactory<TYPE> viewFactory) {
		this.viewOwner = viewOwner;
		this.viewElementType = viewElementType;
		this.elementsProvider = viewElements(repository, viewOwner, viewElementType);
		this.configurer = configurer;
		this.viewFactory = viewFactory;
		if (this instanceof HasConfigureElementByNameSupport) {
			this.invoker = new GroovyDslViewInvoker<>(this, viewOwner, viewElementType, repository, configurer);
		} else {
			this.invoker = new GroovyDslDefaultInvoker<>(this);
		}
	}

	private static <TYPE, T extends TYPE> Provider<Set<T>> viewElements(RealizableDomainObjectRepository<TYPE> repository, DomainObjectIdentifier viewOwner, Class<T> viewElementType) {
		return repository
			.filtered(descendentOf(viewOwner).and(DomainObjectIdentifierUtils.withType(viewElementType)))
			.map(toSetTransformer(viewElementType));
	}

	public Class<T> getElementType() {
		return viewElementType;
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
		return elementsProvider.map(transformEach(mapper).andThen(toListTransformer()));
	}

	public <S> Provider<List<S>> flatMap(Transformer<? extends Iterable<S>, ? super T> mapper) {
		return elementsProvider.map(flatTransformEach(mapper).andThen(toListTransformer()));
	}

	public Provider<List<T>> filter(Spec<? super T> spec) {
		return getElements().map(matching(spec).andThen(toListTransformer(getElementType())));
	}

	public <S extends T> DomainObjectView<S> withType(Class<S> type) {
		return viewFactory.create(viewOwner, type);
	}

	@Override
	public Object invokeMethod(String name, Object args) {
		return invoker.invokeMethod(name, args);
	}
}
