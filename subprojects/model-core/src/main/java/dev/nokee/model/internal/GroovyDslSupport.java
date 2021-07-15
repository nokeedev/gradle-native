package dev.nokee.model.internal;

import groovy.lang.MetaClass;
import groovy.lang.MissingMethodException;
import groovy.lang.MissingPropertyException;
import groovy.lang.Tuple;
import lombok.val;

import java.util.*;
import java.util.function.Predicate;

final class GroovyDslSupport {
	private static final Object[] EMPTY_ARRAY = {};
	private final MetaClass metaClass;
	private final PropertyGetter getter;
	private final List<Map.Entry<Predicate<Object[]>, GenericMethodInvoker>> methods;

	public GroovyDslSupport(MetaClass metaClass, PropertyGetter getter, List<Map.Entry<Predicate<Object[]>, GenericMethodInvoker>> methods) {
		this.metaClass = metaClass;
		this.getter = getter;
		this.methods = methods;
	}

	public Object invokeMethod(String methodName, Object arguments) {
		if (arguments == null) {
			return invokeMethod(methodName, EMPTY_ARRAY);
		}
		if (arguments instanceof Tuple) {
			Tuple<?> tuple = (Tuple<?>) arguments;
			return invokeMethod(methodName, tuple.toArray());
		}
		if (arguments instanceof Object[]) {
			return invokeMethod(methodName, (Object[]) arguments);
		} else {
			return invokeMethod(methodName, new Object[]{arguments});
		}
	}

	private Object invokeMethod(String methodName, Object[] arguments) {
		val metaMethod = metaClass.getMetaMethod(methodName, arguments);
		if (metaMethod != null) {
			return metaMethod.invoke(this, arguments);
		}

		for (val method : methods) {
			if (method.getKey().test(arguments)) {
				return method.getValue().invoke(methodName, arguments);
			}
		}
		throw new MissingMethodException(methodName, metaClass.getTheClass(), arguments);
	}

	public Object getProperty(String name) {
		val property = metaClass.getMetaProperty(name);
		if (property != null) {
			return property.getProperty(this);
		}

		return getter.get(name).orElseThrow(() -> new MissingPropertyException(name, metaClass.getTheClass()));
	}

	public static Builder builder() {
		return new Builder();
	}

	interface BiMethodInvoker<A, B> {
		Object invoke(String methodName, A firstArg, B secondArg);
	}

	interface SingleMethodInvoker<A> {
		Object invoke(String methodName, A arg);
	}

	interface MethodInvoker {
		Object invoke(String methodName);
	}

	interface GenericMethodInvoker {
		Object invoke(String methodName, Object[] arguments);
	}

	interface PropertyGetter {
		Optional<Object> get(String propertyName);
	}

	public static final class Builder {
		private MetaClass metaClass;
		private PropertyGetter getter;
		private final List<Map.Entry<Predicate<Object[]>, GenericMethodInvoker>> invokers = new ArrayList<>();

		public Builder metaClass(MetaClass metaClass) {
			this.metaClass = metaClass;
			return this;
		}

		public Builder whenInvokeMethod(MethodInvoker invoker) {
			val methodInvoker = new GenericMethodInvoker() {
				@Override
				public Object invoke(String methodName, Object[] arguments) {
					return invoker.invoke(methodName);
				}

				@Override
				public String toString() {
					return "Object.<name>()";
				}
			};
			invokers.add(new AbstractMap.SimpleImmutableEntry<>(noArgs(), methodInvoker));
			return this;
		}

		public <A> Builder whenInvokeMethod(Class<A> argType, SingleMethodInvoker<A> invoker) {
			val methodInvoker = new GenericMethodInvoker() {
				@Override
				public Object invoke(String methodName, Object[] args) {
					return invoker.invoke(methodName, argType.cast(args[0]));
				}

				@Override
				public String toString() {
					return "Object.<name>(" + argType.getSimpleName() + ")";
				}
			};
			invokers.add(new AbstractMap.SimpleImmutableEntry<>(args(argType), methodInvoker));
			return this;
		}

		public <A, B> Builder whenInvokeMethod(Class<A> firstArgType, Class<B> secondArgType, BiMethodInvoker<A, B> invoker) {
			val methodInvoker = new GenericMethodInvoker() {
				@Override
				public Object invoke(String methodName, Object[] args) {
					return invoker.invoke(methodName, firstArgType.cast(args[0]), secondArgType.cast(args[1]));
				}

				@Override
				public String toString() {
					return "Object.<name>(" + firstArgType.getSimpleName() + ", " + secondArgType.getSimpleName() + ")";
				}
			};
			invokers.add(new AbstractMap.SimpleImmutableEntry<>(args(firstArgType, secondArgType), methodInvoker));
			return this;
		}

		private static Predicate<Object[]> noArgs() {
			return args -> args.length == 0;
		}

		private static Predicate<Object[]> args(Class<?> t) {
			return args -> args.length == 1 && t.isInstance(args[0]);
		}

		private static Predicate<Object[]> args(Class<?> t0, Class<?> t1) {
			return args -> args.length == 2 && t0.isInstance(args[0]) && t1.isInstance(args[1]);
		}

		public Builder whenGetProperty(PropertyGetter getter) {
			this.getter = getter;
			return this;
		}

		public GroovyDslSupport build() {
			return new GroovyDslSupport(metaClass, getter, invokers);
		}
	}
}
