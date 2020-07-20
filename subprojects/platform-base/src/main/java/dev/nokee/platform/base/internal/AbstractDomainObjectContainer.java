package dev.nokee.platform.base.internal;

import dev.nokee.platform.base.KnownDomainObject;
import groovy.lang.GroovyObjectSupport;
import lombok.AccessLevel;
import lombok.Getter;
import org.codehaus.groovy.runtime.wrappers.GroovyObjectWrapper;
import org.gradle.api.Action;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.specs.Spec;

import javax.inject.Inject;

public abstract class AbstractDomainObjectContainer<T> extends GroovyObjectSupport {
	private final Class<T> publicType;

	protected AbstractDomainObjectContainer(Class<T> publicType, DomainObjectStore store) {
		this.publicType = publicType;
		this.store = store;
	}

	@Inject
	protected abstract ObjectFactory getObjects();

	@Getter(AccessLevel.PROTECTED)
	private final DomainObjectStore store;

	public void whenElementKnown(Action<KnownDomainObject<? extends T>> action) {
		store.whenElementKnown(publicType, action);
	}

	public <U extends T> void whenElementKnown(Class<U> type, Action<KnownDomainObject<? extends U>> action) {
		store.whenElementKnown(type, action);
	}

	public void configureEach(Action<? super T> action) {
		store.configureEach(publicType, action);
	}

	public <U extends T> void configureEach(Class<U> type, Action<? super U> action) {
		store.configureEach(type, action);
	}

	public void configureEach(Spec<? super T> spec, Action<? super T> action) {
		store.configureEach(publicType, spec, action);
	}

	public void forceRealize() {
//		store.forceRealize(publicType);
	}
}
