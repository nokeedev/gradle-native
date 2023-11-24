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
import dev.nokee.model.internal.decorators.DecoratorHandlers;
import dev.nokee.model.internal.decorators.InjectService;
import dev.nokee.model.internal.decorators.ModelDecorator;
import dev.nokee.model.internal.decorators.MutableModelDecorator;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Deque;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Consumer;

public final class DefaultInstantiator implements Instantiator, DecoratorHandlers {
	private final ObjectFactory objects;
	private final MutableModelDecorator decorator = new MutableModelDecorator();

	// TODO: We should keep the decorated class globally across all projects, maybe use a BuildService
	private final InjectorClassLoader classLoader = new InjectorClassLoader(DefaultInstantiator.class.getClassLoader());

	public DefaultInstantiator(ObjectFactory objects) {
		this.objects = objects;
	}

	@Override
	public <T> T newInstance(Class<? extends T> type, Object... parameters) throws ObjectInstantiationException {
		// TODO: Inspect @Inject constructor and pass along Nokee build service
		return ModelDecorator.decorateUsing(decorator, () -> objects.newInstance(generateSubType(type), parameters));
	}

	private <T> Class<? extends T> generateSubType(Class<? extends T> type) {
		// TODO: If the type implements ModelMixIn or ModelMixInSupport do not decorate, else decorate
		// TODO: We should merge decorator with this class so we register the decorator here
		// TODO: If type is final, do direct instantiator
		return new ClassInspector().inspectType(type).generateClass(classLoader);
	}

	@Override
	public void nestedObject(Consumer<? super NestedObjectContext> action) {
		decorator.nestedObject(action);
	}

	@Override
	public void injectService(Consumer<? super InjectServiceContext> action) {
		decorator.injectService(action);
	}

	@EqualsAndHashCode
	static class GeneratedMethod {
		private final ModelType<?> returnType;
		private final String methodName;
		private final String propertyName;

		GeneratedMethod(ModelType<?> returnType, String methodName, String propertyName) {
			this.returnType = returnType;
			this.methodName = methodName;
			this.propertyName = propertyName;
		}

		void applyTo(ClassWriter cw) {
			String methodDescriptor = "()" + Type.getDescriptor(returnType.getRawType());
			MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, methodName, methodDescriptor, null, null);

			mv.visitCode();

			// Load 'this' onto the stack
			mv.visitVarInsn(Opcodes.ALOAD, 0);

			// Cast 'this' to ExtensionAware
			mv.visitTypeInsn(Opcodes.CHECKCAST, "org/gradle/api/plugins/ExtensionAware");

			// Invoke getExtensions() on the ExtensionAware object
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/gradle/api/plugins/ExtensionAware", "getExtensions", "()Lorg/gradle/api/plugins/ExtensionContainer;", true);

			// Load the propertyName onto the stack
			mv.visitLdcInsn(propertyName);

			// Invoke getByName(propertyName) on the extensions map
			mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, "org/gradle/api/plugins/ExtensionContainer", "getByName", "(Ljava/lang/String;)Ljava/lang/Object;", true);

			// Cast the result to the return type
			mv.visitTypeInsn(Opcodes.CHECKCAST, Type.getInternalName(returnType.getRawType()));

			// Return the result
			mv.visitInsn(Type.getType(returnType.getRawType()).getOpcode(Opcodes.IRETURN));

			mv.visitMaxs(-1, -1); // Auto compute stack and local variables size
			mv.visitEnd();
		}
	}

	private static final class ClassInspector {
		public ClassInspection inspectType(Class<?> type) {
			Set<GeneratedMethod> result = new LinkedHashSet<>();

			List<Constructor<?>> injectedConstructor = new ArrayList<>();

			new SuperClassFirstClassVisitor(new MethodFieldVisitor(new NotPrivateOrStaticMethodsVisitor(new OnlyNestedOrInjectGetterMethod(new NestedOrInjectVisitor() {
				Class<?> current;

				@Override
				public void visitClass(Class<?> type) {
					current = type;
				}

				@Override
				public void visitInjectedConstructor(Constructor<?> constructor) {
					if (constructor.getDeclaringClass().equals(type)) {
						injectedConstructor.add(constructor);
					}
				}

				@Override
				public void visitInjectedProperty(Method method) {
					assert current != null;
					result.add(new GeneratedMethod(returnTypeOf(method), method.getName(), propertyNameOf(method)));
				}

				@Override
				public void visitNestedProperty(Method method) {
					assert current != null;
					result.add(new GeneratedMethod(returnTypeOf(method), method.getName(), propertyNameOf(method)));
				}

				private ModelType<?> returnTypeOf(Method method) {
					try {
						Method m = method.getDeclaringClass().getMethod(method.getName());
						return ModelType.of(TypeToken.of(ModelTypeUtils.toUndecoratedType(type)).resolveType(m.getGenericReturnType()).getType());
					} catch (NoSuchMethodException e) {
						throw new RuntimeException(e);
					}
				}

				private String propertyNameOf(Method method) {
					return StringUtils.uncapitalize(method.getName().substring(3));
				}

				@Override
				public void visitEnd() {
					current = null;
				}
			})))).visitClass(type);

			return new ClassInspection() {
				@Override
				@SuppressWarnings("unchecked")
				public <T> Class<T> generateClass(InjectorClassLoader classLoader) {
					return (Class<T>) classLoader.defineClass(Type.getInternalName(type) + "Subclass", generateSubclass(type, result));
				}
			};
		}
	}

	public static byte[] generateSubclass(Class<?> superClass, Collection<GeneratedMethod> methods) {
		ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);

		String superClassNameInternal = Type.getInternalName(superClass);
		String subclassNameInternal = superClassNameInternal + "Subclass";

		cw.visit(Opcodes.V1_8, Opcodes.ACC_PUBLIC + Opcodes.ACC_ABSTRACT, subclassNameInternal, null, superClassNameInternal, null);

		for (Constructor<?> constructor : superClass.getConstructors()) {
			if (constructor.isAnnotationPresent(Inject.class)) {
				generateConstructor(cw, superClassNameInternal, constructor);
			}
		}

		for (GeneratedMethod method : methods) {
			method.applyTo(cw);
		}

		cw.visitEnd();

		return cw.toByteArray();
	}

	private static void generateConstructor(ClassWriter cw, String superClassNameInternal, Constructor<?> constructor) {
		String constructorDescriptor = Type.getConstructorDescriptor(constructor);
		MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", constructorDescriptor, null, null);

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

		// Complete the constructor with return
		mv.visitInsn(Opcodes.RETURN);

		// Compute the max stack size and max locals automatically
		mv.visitMaxs(-1, -1);
		mv.visitEnd();
	}

	private static void superClasses(Class<?> current, Collection<Class<?>> supers) {
		Class<?> superclass = current.getSuperclass();
		while (superclass != null) {
			supers.add(superclass);
			superclass = superclass.getSuperclass();
		}
	}


	private interface ClassVisitor {
		void visitClass(Class<?> type);
	}

	private static final class SuperClassFirstClassVisitor {
		private final ClassVisitor visitor;

		private SuperClassFirstClassVisitor(ClassVisitor visitor) {
			this.visitor = visitor;
		}

		public void visitClass(Class<?> type) {
			Set<Class<?>> seen = new HashSet<Class<?>>();
			Deque<Class<?>> queue = new ArrayDeque<Class<?>>();

			queue.add(type);
			superClasses(type, queue);
			while (!queue.isEmpty()) {
				Class<?> current = queue.removeFirst();
				if (!seen.add(current)) {
					continue;
				}
				visitor.visitClass(current);
				Collections.addAll(queue, current.getInterfaces());
			}
		}
	}

	interface ClassMethodFieldVisitor {
		void visitClass(Class<?> type);
		void visitConstructor(Constructor<?> constructor);
		void visitMethod(Method method);
		void visitEnd();
	}

	private static final class MethodFieldVisitor implements ClassVisitor {
		private final ClassMethodFieldVisitor visitor;

		private MethodFieldVisitor(ClassMethodFieldVisitor visitor) {
			this.visitor = visitor;
		}

		@Override
		public void visitClass(Class<?> type) {
			visitor.visitClass(type);

			for (Constructor<?> constructor : type.getDeclaredConstructors()) {
				visitor.visitConstructor(constructor);
			}

			for (Method method : type.getDeclaredMethods()) {
				visitor.visitMethod(method);
			}

			// No support for field for now

			visitor.visitEnd();
		}
	}

	private static final class NotPrivateOrStaticMethodsVisitor implements ClassMethodFieldVisitor {
		private final ClassMethodFieldVisitor visitor;

		private NotPrivateOrStaticMethodsVisitor(ClassMethodFieldVisitor visitor) {
			this.visitor = visitor;
		}

		@Override
		public void visitClass(Class<?> type) {
			visitor.visitClass(type);
		}

		@Override
		public void visitConstructor(Constructor<?> constructor) {
			if (Modifier.isPrivate(constructor.getModifiers())) {
				// skip
			} else {
				visitor.visitConstructor(constructor);
			}
		}

		@Override
		public void visitMethod(Method method) {
			if (Modifier.isPrivate(method.getModifiers()) || Modifier.isStatic(method.getModifiers())) {
				// skip
			} else {
				visitor.visitMethod(method);
			}
		}

		@Override
		public void visitEnd() {
			visitor.visitEnd();
		}
	}

	private static final class OnlyNestedOrInjectGetterMethod implements ClassMethodFieldVisitor {
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
					visitor.visitNestedProperty(method);
				} else if (method.isAnnotationPresent(InjectService.class)) {
					visitor.visitInjectedProperty(method);
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
		void visitInjectedProperty(Method method);
		void visitNestedProperty(Method method);
		void visitEnd();
	}

	private interface ClassInspection {
		<T> Class<T> generateClass(InjectorClassLoader classLoader);
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
