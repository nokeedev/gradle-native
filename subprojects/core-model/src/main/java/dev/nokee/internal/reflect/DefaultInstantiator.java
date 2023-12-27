/*
 * Copyright 2023 the original author or authors.
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

package dev.nokee.internal.reflect;

import com.google.common.reflect.TypeToken;
import dev.nokee.internal.services.ServiceLookup;
import dev.nokee.model.internal.decorators.ClassGenerationVisitor;
import dev.nokee.model.internal.decorators.Decorate;
import dev.nokee.model.internal.decorators.Decorator;
import dev.nokee.model.internal.decorators.InjectService;
import dev.nokee.model.internal.decorators.NestedObject;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.model.internal.type.ModelTypeUtils;
import lombok.EqualsAndHashCode;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.model.ObjectFactory;
import org.gradle.api.reflect.ObjectInstantiationException;
import org.objectweb.asm.AnnotationVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

import javax.inject.Inject;
import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.nokee.internal.reflect.SignatureUtils.getConstructorSignature;

public final class DefaultInstantiator implements Instantiator {
	private static final ThreadLocal<ServiceLookup> nextService = new ThreadLocal<>();
	private final ObjectFactory objects;
	private final ServiceLookup serviceLookup;

	// TODO: We should keep the decorated class globally across all projects, maybe use a BuildService
	private final InjectorClassLoader classLoader = new InjectorClassLoader(DefaultInstantiator.class.getClassLoader());

	public static ServiceLookup getNextService() {
		return nextService.get();
	}

	public DefaultInstantiator(ObjectFactory objects, ServiceLookup serviceLookup) {
		this.objects = objects;
		this.serviceLookup = serviceLookup;
	}

	@Override
	@SuppressWarnings("unchecked")
	public <T> T newInstance(Class<? extends T> type, Object... parameters) throws ObjectInstantiationException {
		// TODO: Inspect @Inject constructor and pass along Nokee build service
		try {
			return (T) generateSubType(type).newInstance(serviceLookup, objects, parameters);
		} catch (InvocationTargetException | IllegalAccessException | InstantiationException e) {
			throw new ObjectInstantiationException(type, e);
		}
	}

	private <T> InstantiationStrategy generateSubType(Class<? extends T> type) {
		// TODO: If the type implements ModelMixIn or ModelMixInSupport do not decorate, else decorate
		// TODO: We should merge decorator with this class so we register the decorator here
		// TODO: If type is final, do direct instantiator
		return new ClassInspector().inspectType(type).generateClass(classLoader);
	}

	@EqualsAndHashCode
	static class GeneratedMethod {
		private final ModelType<?> returnType;
		private final String methodName;
		private final String propertyName;
		@EqualsAndHashCode.Exclude private final ClassGenerationVisitor visitor;

		GeneratedMethod(ModelType<?> returnType, String methodName, String propertyName, Decorator decorator, Annotation[] annotations) {
			this.returnType = returnType;
			this.methodName = methodName;
			this.propertyName = propertyName;
			this.visitor = decorator.applyTo(new Decorator.MethodMetadata() {
				@Override
				public String getName() {
					return methodName;
				}

				@Override
				public Class<?> getReturnType() {
					return returnType.getRawType();
				}

				@Override
				public java.lang.reflect.Type getGenericReturnType() {
					return returnType.getType();
				}

				@Override
				public Stream<Annotation> getAnnotations() {
					return Arrays.stream(annotations);
				}
			});
		}
	}

	private final class ClassInspector {
		public ClassInspection inspectType(Class<?> type) {
			Set<GeneratedMethod> result = new LinkedHashSet<>();
			Set<Annotation> inheritedAnnotations = new LinkedHashSet<>();

			new SuperClassFirstClassVisitor(new MethodFieldVisitor(new NotPrivateOrStaticMethodsVisitor(new OnlyNestedOrInjectGetterMethod(new NestedOrInjectVisitor() {
				@Override
				public void visitClass(Class<?> type) {
					for (Annotation annotation : type.getDeclaredAnnotations()) {
						if (annotation.annotationType().isAnnotationPresent(Inherited.class)) {
							inheritedAnnotations.add(annotation);
						}
					}
				}

				@Override
				public void visitInjectedConstructor(Constructor<?> constructor) {
					// for now, do nothing
				}

				@Override
				public void visitDecoratedProperty(Class<? extends Decorator> decoratorType, Method method) {
					result.add(new GeneratedMethod(returnTypeOf(method), method.getName(), propertyNameOf(method), DefaultInstantiator.this.newInstance(decoratorType), method.getAnnotations()));
				}

				private ModelType<?> returnTypeOf(Method method) {
					try {
						Method m = method.getDeclaringClass().getMethod(method.getName());
						return ModelType.of(TypeToken.of(type).resolveType(m.getGenericReturnType()).getType());
					} catch (NoSuchMethodException e) {
						throw new RuntimeException(e);
					}
				}

				private String propertyNameOf(Method method) {
					return StringUtils.uncapitalize(method.getName().substring(3));
				}

				@Override
				public void visitEnd() {}
			})))).visitClass(type);

			return new ClassInspection() {
				@Override
				@SuppressWarnings("unchecked")
				public InstantiationStrategy generateClass(InjectorClassLoader classLoader) {
					return new InstantiationStrategy() {
						@Override
						public Object newInstance(ServiceLookup serviceLookup, ObjectFactory objects, Object[] params) throws InvocationTargetException, IllegalAccessException, InstantiationException {
							Class<?> typeToInstantiate = classLoader.defineClass(Type.getInternalName(type) + "Subclass", generateSubclass(type, result, inheritedAnnotations));

							ServiceLookup previousService = nextService.get();
							try {
								nextService.set(serviceLookup);

								return objects.newInstance(typeToInstantiate, paramsOf(serviceLookup, typeToInstantiate, params));
							} finally {
								nextService.set(previousService);
							}
						}
					};
				}
			};
		}
	}

	private static Object[] paramsOf(ServiceLookup serviceLookup, Class<?> type, Object[] params) {
		List<TypeToken<?>> parameterTypes = null;
		for (Constructor<?> declaredConstructor : type.getDeclaredConstructors()) {
			if (declaredConstructor.isAnnotationPresent(Inject.class) && !Modifier.isPrivate(declaredConstructor.getModifiers())) {
				parameterTypes = Arrays.stream(declaredConstructor.getGenericParameterTypes()).map(TypeToken::of).collect(Collectors.toList());
			}
		}

		if (parameterTypes == null) {
			return params;
		} else {
			List<Object> result = new ArrayList<>();
			int iv = 0; // index in params
			for (int i = 0; i < parameterTypes.size(); i++) {
				if (iv < params.length && parameterTypes.get(i).getRawType().isInstance(params[iv])) {
					result.add(params[iv]);
					iv++; // parameter type is the same as params, consume the param
				} else {
					Object value = serviceLookup.find(parameterTypes.get(i).getType());
					if (value != null) {
						result.add(value);
					}
				}
			}

			return result.toArray(new Object[0]);
		}
	}

	interface InstantiationStrategy {
		Object newInstance(ServiceLookup serviceLookup, ObjectFactory objects, Object[] params) throws InvocationTargetException, IllegalAccessException, InstantiationException;
	}

	public static byte[] generateSubclass(Class<?> superClass, Collection<GeneratedMethod> methods, Collection<Annotation> annotations) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

		String superClassNameInternal = Type.getInternalName(superClass);
		String subclassNameInternal = superClassNameInternal + "Subclass";

		cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT, subclassNameInternal, null, superClassNameInternal, null);

		for (Annotation annotation : annotations) {
			AnnotationVisitor av = cw.visitAnnotation(Type.getDescriptor(annotation.annotationType()), true);
			for (Method method : annotation.annotationType().getDeclaredMethods()) {
				try {
					final Object value = method.invoke(annotation);
					if (value instanceof Class) {
						av.visit(method.getName(), Type.getType((Class<?>) value));
					} else {
						throw new RuntimeException();
					}
				} catch (IllegalAccessException | InvocationTargetException e) {
					throw new RuntimeException(e);
				}
			}
			av.visitEnd();
		}

		boolean injectConstructorFound = false;
		for (Constructor<?> constructor : superClass.getConstructors()) {
			if (constructor.isAnnotationPresent(Inject.class)) {
				injectConstructorFound = true;
				generateConstructor(cw, subclassNameInternal, superClassNameInternal, constructor, methods);
			}
		}

		if (!injectConstructorFound) {
			generateDefaultConstructor(cw, subclassNameInternal, superClassNameInternal, methods);
		}

		for (GeneratedMethod method : methods) {
			method.visitor.visitFields(cw);
			method.visitor.visitMethods(subclassNameInternal, cw);
		}

		cw.visitEnd();

		return cw.toByteArray();
	}

	private static void generateDefaultConstructor(ClassWriter cw, String classNameInternal, String superClassNameInternal, Collection<GeneratedMethod> methods) {
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);

		// Note: there is no need to annotate the default constructor with @Inject

		mv.visitCode();
		mv.visitVarInsn(Opcodes.ALOAD, 0); // Load "this"
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superClassNameInternal, "<init>", "()V", false); // Call the superclass constructor

		for (GeneratedMethod method : methods) {
			method.visitor.visitFieldsInitialization(classNameInternal, mv);
		}

		mv.visitInsn(Opcodes.RETURN);
		mv.visitMaxs(-1, -1); // Auto compute stack and locals
		mv.visitEnd();
	}

	private static void generateConstructor(ClassWriter cw, String classNameInternal, String superClassNameInternal, Constructor<?> constructor, Collection<GeneratedMethod> methods) {
		String constructorDescriptor = Type.getConstructorDescriptor(constructor);
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", constructorDescriptor, getConstructorSignature(constructor), null);

		if (constructor.isAnnotationPresent(Inject.class)) {
			AnnotationVisitor av = mv.visitAnnotation(Type.getDescriptor(Inject.class), true);
			av.visitEnd();
		}

		mv.visitCode();

		// Load "this" onto the stack
		mv.visitVarInsn(Opcodes.ALOAD, 0);

		// Load all constructor arguments onto the stack
		Class<?>[] params = constructor.getParameterTypes();
		int localIndex = 1; // Local variables index 0 is "this", parameters start at 1
		for (Class<?> paramType : params) {
			mv.visitVarInsn(Type.getType(paramType).getOpcode(Opcodes.ILOAD), localIndex);
			localIndex += Type.getType(paramType).getSize();
		}

		// Call the super constructor
		mv.visitMethodInsn(Opcodes.INVOKESPECIAL, superClassNameInternal, "<init>", constructorDescriptor, false);

		for (GeneratedMethod method : methods) {
			method.visitor.visitFieldsInitialization(classNameInternal, mv);
		}

		// Complete the constructor with return
		mv.visitInsn(Opcodes.RETURN);

		// Compute the max stack size and max locals automatically
		mv.visitMaxs(-1, -1);
		mv.visitEnd();
	}

	private static final class OnlyNestedOrInjectGetterMethod implements MethodFieldVisitor.ClassMethodFieldVisitor {
		private final NestedOrInjectVisitor visitor;

		private OnlyNestedOrInjectGetterMethod(NestedOrInjectVisitor visitor) {
			this.visitor = visitor;
		}

		@Override
		public void visitClass(Class<?> type) {
			visitor.visitClass(type);
		}

		@Override
		public void visitConstructor(Constructor<?> constructor) {
			if (constructor.isAnnotationPresent(Inject.class)) {
				visitor.visitInjectedConstructor(constructor);
			}
		}

		@Override
		public void visitMethod(Method method) {
			if (method.getName().startsWith("get") && method.getParameterCount() == 0) {
				if (method.isAnnotationPresent(NestedObject.class)) {
					visitor.visitDecoratedProperty(method.getAnnotation(NestedObject.class).annotationType().getAnnotation(Decorate.class).value(), method);
				} else if (method.isAnnotationPresent(InjectService.class)) {
					visitor.visitDecoratedProperty(method.getAnnotation(InjectService.class).annotationType().getAnnotation(Decorate.class).value(), method);
				} else if (method.isAnnotationPresent(Decorate.class)) {
					visitor.visitDecoratedProperty(method.getAnnotation(Decorate.class).value(), method);
				}
			}
		}

		@Override
		public void visitEnd() {
			visitor.visitEnd();
		}
	}

	private interface NestedOrInjectVisitor {
		void visitClass(Class<?> type);
		void visitInjectedConstructor(Constructor<?> constructor);
		void visitDecoratedProperty(Class<? extends Decorator> decoratorType, Method method);
		void visitEnd();
	}

	private interface ClassInspection {
		InstantiationStrategy generateClass(InjectorClassLoader classLoader);
	}

	public static final class InjectorClassLoader extends ClassLoader {
		private final ConcurrentMap<String, Class<?>> cache = new ConcurrentHashMap<>();

		public InjectorClassLoader(ClassLoader parent) {
			super(parent);
		}

		public Class<?> defineClass(String name, byte[] b) {
			return cache.computeIfAbsent(name, __ -> {
				// Convert the class name to a binary name
				String binaryName = name.replace('/', '.');
				return super.defineClass(binaryName, b, 0, b.length);
			});
		}
	}
}
