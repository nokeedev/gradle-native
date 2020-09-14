package dev.nokee.testing.base.internal;

import dev.nokee.model.internal.DomainObjectIdentifierUtils;
import dev.nokee.platform.base.DomainObjectProvider;
import dev.nokee.platform.base.internal.AbstractDomainObjectContainer;
import dev.nokee.platform.base.internal.DomainObjectFactory;
import dev.nokee.model.DomainObjectIdentifier;
import dev.nokee.platform.base.internal.DomainObjectStore;
import dev.nokee.testing.base.TestSuiteComponent;
import dev.nokee.testing.base.TestSuiteContainer;
import dev.nokee.utils.Cast;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.api.NamedDomainObjectFactory;
import org.gradle.api.model.ObjectFactory;
import org.gradle.internal.metaobject.*;
import org.gradle.util.ConfigureUtil;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

public class DefaultTestSuiteContainer extends AbstractDomainObjectContainer<TestSuiteComponent> implements TestSuiteContainer, MethodMixIn {
	private final Map<Class<?>, NamedDomainObjectFactory<?>> bindings = new HashMap<>();
	private final Map<Class<?>, Class<?>> implementationTypes = new HashMap<>();

	@Inject
	public DefaultTestSuiteContainer(DomainObjectStore store, ObjectFactory objectFactory) {
		super(TestSuiteComponent.class, store, objectFactory);
	}

	public <U extends TestSuiteComponent> void registerFactory(Class<U> type, Class<? extends U> implementationType, NamedDomainObjectFactory<U> factory) {
		bindings.put(type, factory);
		implementationTypes.put(type, implementationType);
	}

	@Override
	public <T extends TestSuiteComponent> DomainObjectProvider<T> register(String name, Class<T> type) {
		return getStore().register(new DomainObjectFactory<T>() {
			@Override
			public T create() {
				return type.cast(bindings.get(type).create(name));
			}

			public Class<? extends T> getImplementationType() {
				return Cast.uncheckedCastBecauseOfTypeErasure(implementationTypes.get(type));
			}

			@Override
			public Class<T> getType() {
				return type;
			}

			@Override
			public DomainObjectIdentifier getIdentity() {
				return DomainObjectIdentifierUtils.named(name);
			}
		});
	}

	@Override
	public <T extends TestSuiteComponent> DomainObjectProvider<T> register(String name, Class<T> type, Action<? super T> action) {
		val result = register(name, type);
		result.configure(action);
		return result;
	}

	private final ContainerElementsDynamicObject elementsDynamicObject = new ContainerElementsDynamicObject();

	private class ContainerElementsDynamicObject extends AbstractDynamicObject {
		@Override
		public String getDisplayName() {
			return "";
		}

		@Override
		public boolean hasMethod(String name, Object... arguments) {
			return isConfigureMethod(name, arguments);
		}

		@Override
		public DynamicInvokeResult tryInvokeMethod(String name, Object... arguments) {
			if (isConfigureMethod(name, arguments)) {
				if (arguments.length == 1) {
					return DynamicInvokeResult.found(register(name, (Class)arguments[0]));
				} else if (arguments.length == 2) {
					return DynamicInvokeResult.found(register(name, (Class)arguments[0], ConfigureUtil.configureUsing((Closure) arguments[1])));
				}
			}
			return DynamicInvokeResult.notFound();
		}

		private boolean isConfigureMethod(String name, Object... arguments) {
			return (arguments.length == 1 && arguments[0] instanceof Class
				|| arguments.length == 2 && arguments[0] instanceof Class && arguments[1] instanceof Closure);
		}
	}

	protected DynamicObject getElementsAsDynamicObject() {
		return elementsDynamicObject;
	}

	@Override
	public MethodAccess getAdditionalMethods() {
		return getElementsAsDynamicObject();
	}

}
