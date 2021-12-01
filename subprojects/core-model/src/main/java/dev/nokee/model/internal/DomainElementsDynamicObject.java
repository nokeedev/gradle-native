/*
 * Copyright 2020-2021 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.nokee.model.internal;

import dev.nokee.model.DomainObjectProvider;
import dev.nokee.model.internal.type.ModelType;
import groovy.lang.Closure;
import lombok.val;
import org.gradle.api.Action;
import org.gradle.internal.metaobject.AbstractDynamicObject;
import org.gradle.internal.metaobject.DynamicInvokeResult;
import org.gradle.util.ConfigureUtil;

import java.util.Map;
import java.util.function.Supplier;

public abstract class DomainElementsDynamicObject extends AbstractDynamicObject {
	@Override
	public String getDisplayName() {
		return "";
	}

	@Override
	public final boolean hasProperty(String name) {
		return hasElement(name);
	}

	@Override
	public final DynamicInvokeResult tryGetProperty(String name) {
		if (hasElement(name)) {
			return DynamicInvokeResult.found(getElement(name));
		}
		return DynamicInvokeResult.notFound();
	}

	protected abstract ModelType<?> getElementType();
	protected abstract boolean hasElement(String name);
	protected final DomainObjectProvider<?> getElement(String name) {
		return getElement(name, getElementType());
	}
	protected abstract DomainObjectProvider<?> getElement(String name, ModelType<?> type);
	protected abstract Map<String, ? extends DomainObjectProvider<?>> getElementsAsMap();
	protected abstract DomainObjectProvider<?> doRegister(String name, ModelType<?> type);
	protected abstract boolean canRegister();

	@Override
	public final Map<String, ? extends DomainObjectProvider<?>> getProperties() {
		return getElementsAsMap();
	}

	@Override
	public final boolean hasMethod(String name, Object... arguments) {
		return isGetByTypeMethod(name, arguments) ||
			isRegisterByTypeMethod(name, arguments) ||
			isConfigureMethod(name, arguments) ||
			isConfigureByTypeMethod(name, arguments) ||
			isRegisterByTypeWithConfigurationMethod(name, arguments);
	}

	@Override
	public final DynamicInvokeResult tryInvokeMethod(String name, Object... arguments) {
		if (isGetByTypeMethod(name, arguments)) {
			return DynamicInvokeResult.found(getElement(name, asType(arguments[0])));
		} else if (isRegisterByTypeMethod(name, arguments)) {
			val provider = doRegister(name, asType(arguments[0]));
			return DynamicInvokeResult.found(provider);
		} else if (isConfigureMethod(name, arguments)) {
			getElement(name).configure(asAction(arguments[0]));
			return DynamicInvokeResult.found();
		} else if (isConfigureByTypeMethod(name, arguments)) {
			getElement(name, asType(arguments[0])).configure(asAction(arguments[1]));
			return DynamicInvokeResult.found();
		} else if (isRegisterByTypeWithConfigurationMethod(name, arguments)) {
			val provider = doRegister(name, asType(arguments[0]));
			provider.configure(asAction(arguments[1]));
			return DynamicInvokeResult.found(provider);
		}
		return DynamicInvokeResult.notFound();
	}

	private static Boolean log(String message, Supplier<Boolean> factory) {
		val result = factory.get();
		return result;
	}

	// Ex: <name> { <closure> }
	private boolean isConfigureMethod(String name, Object... arguments) {
		return log("is configure using <name> { <closure> }", () -> (arguments.length == 1 && isClosure(arguments[0])) && hasElement(name));
	}

	// Ex: <name>(<type>) { <closure> }
	private boolean isConfigureByTypeMethod(String name, Object... arguments) {
		return log("is configure using <name>(<type>) { <closure> }", () -> (arguments.length == 2 && isType(arguments[0]) && isClosure(arguments[1])) && hasElement(name));
	}

	// Ex: <name>(<type>)
	private boolean isGetByTypeMethod(String name, Object... arguments) {
		return log("is get using <name>(<type>)", () -> (arguments.length == 1 && isType(arguments[0])) && hasElement(name));
	}

	// Ex: <name>(<type>)
	private boolean isRegisterByTypeMethod(String name, Object... arguments) {
		return log("is register using <name>(<type>)", () -> canRegister() && (arguments.length == 1 && isType(arguments[0])) && !hasElement(name));
	}

	// Ex: <name>(<type>) { <closure> }
	private boolean isRegisterByTypeWithConfigurationMethod(String name, Object... arguments) {
		return log("is register using <name>(<type>) { <closure> }", () -> canRegister() && (arguments.length == 2 && isType(arguments[0]) && isClosure(arguments[1])) && !hasElement(name));
	}

	private static boolean isClosure(Object o) {
		return o instanceof Closure;
	}

	@SuppressWarnings("rawtypes")
	private static Action<Object> asAction(Object o) {
		return ConfigureUtil.configureUsing((Closure) o);
	}

	private static ModelType<?> asType(Object o) {
		return ModelType.of((Class<?>) o);
	}

	private static boolean isType(Object o) {
		return o instanceof Class;
	}
}
