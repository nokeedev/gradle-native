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

package dev.nokee.model.internal.decorators;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import dev.nokee.internal.reflect.DefaultInstantiator;
import dev.nokee.internal.reflect.Instantiator;
import dev.nokee.model.internal.ModelElementSupport;
import dev.nokee.model.internal.ModelObject;
import dev.nokee.model.internal.ModelObjectIdentifier;
import dev.nokee.model.internal.ModelObjectRegistry;
import dev.nokee.model.internal.names.ElementName;
import dev.nokee.model.internal.names.TaskName;
import dev.nokee.model.internal.type.ModelType;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Named;
import org.gradle.api.NamedDomainObjectProvider;
import org.gradle.api.Task;
import org.gradle.api.provider.Provider;
import org.gradle.api.tasks.TaskProvider;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import javax.annotation.Nullable;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.Objects;

import static dev.nokee.internal.reflect.SignatureUtils.getGenericSignature;
import static dev.nokee.internal.reflect.SignatureUtils.getterSignature;

public /*final*/ class NestedObjectDecorator implements Decorator {
	// TODO: Make sure objectType can be generic type
	// GENERATE <objectType> get<prop>() {
	//      if (this._nokee_<prop> == null) {
	//          this._nokee_<prop> = <init>;
	//      }
	//      return this._nokee_<prop>;
	// }

	// GENERATE private <objectType> _nokee_<prop> = <init>;

	// <init> => NestedObjectDecorator.create(TaskName/ElementName.of('name'), <objectType>)

	public static boolean isTaskType(Type type) {
		if (TaskProvider.class.isAssignableFrom(TypeToken.of(type).getRawType())) {
			return true;
		} else if (TypeToken.of(type).getType() instanceof ParameterizedType) {
			if (Task.class.isAssignableFrom((Class<?>) ((ParameterizedType) TypeToken.of(type).getType()).getActualTypeArguments()[0])) {
				return true;
			}
		}
		return false;
	}

	public ClassGenerationVisitor applyTo(MethodMetadata method) {
		return new ClassGenerationVisitor() {
			private final String fieldName = "_nokee_" + propertyNameOf(method);
			private final String methodName = method.getName();
			private final String propertyName = propertyNameOf(method);
			private final ModelType<?> returnType = ModelType.of(method.getGenericReturnType());

			@Override
			public void visitFieldsInitialization(String ownerInternalName, MethodVisitor mv) {
				mv.visitVarInsn(Opcodes.ALOAD, 0); // Load 'this' to set the field on

				if (isTaskType(returnType.getType())) {
					String taskName = propertyName;
					if (taskName.endsWith("Task")) {
						taskName = taskName.substring(0, taskName.length() - "Task".length());
					}
					mv.visitLdcInsn(taskName);
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, org.objectweb.asm.Type.getInternalName(TaskName.class), "of", "(Ljava/lang/String;)Ldev/nokee/model/internal/names/TaskName;", false);
				} else if (Provider.class.isAssignableFrom(returnType.getRawType()) || Named.class.isAssignableFrom(returnType.getRawType())) {
					mv.visitLdcInsn(propertyName);
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, org.objectweb.asm.Type.getInternalName(ElementName.class), "of", "(Ljava/lang/String;)Ldev/nokee/model/internal/names/ElementName;", true);
				} else {
					NestedObject nestedObject = (NestedObject) method.getAnnotations().filter(it -> it.annotationType().equals(NestedObject.class)).findFirst().orElse(null);
					if (nestedObject == null || nestedObject.value().length() == 0) {
						mv.visitInsn(Opcodes.ACONST_NULL);
					} else {
						mv.visitLdcInsn(nestedObject.value());
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, org.objectweb.asm.Type.getInternalName(ElementName.class), "of", "(Ljava/lang/String;)Ldev/nokee/model/internal/names/ElementName;", true);
					}
				}

				if (returnType.getType() instanceof ParameterizedType) {
					assert ((ParameterizedType) returnType.getType()).getActualTypeArguments().length == 1;
					mv.visitLdcInsn(org.objectweb.asm.Type.getType((Class<?>) ((ParameterizedType) returnType.getType()).getRawType()));
					Type typeArgument = ((ParameterizedType) returnType.getType()).getActualTypeArguments()[0];
					if (typeArgument instanceof ParameterizedType) {
						mv.visitLdcInsn(org.objectweb.asm.Type.getType((Class<?>) ((ParameterizedType) typeArgument).getRawType()));
						mv.visitLdcInsn(org.objectweb.asm.Type.getType((Class<?>) ((ParameterizedType) typeArgument).getActualTypeArguments()[0]));
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, org.objectweb.asm.Type.getInternalName(InjectServiceDecorator.class), "typeOf", "(Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/reflect/Type;", false);
					} else {
						mv.visitLdcInsn(org.objectweb.asm.Type.getType((Class<?>) typeArgument));
						mv.visitMethodInsn(Opcodes.INVOKESTATIC, org.objectweb.asm.Type.getInternalName(InjectServiceDecorator.class), "typeOf", "(Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/reflect/Type;", false);
					}
				} else {
					assert returnType.getType() instanceof Class;
					mv.visitLdcInsn(org.objectweb.asm.Type.getType((Class<?>) returnType.getType()));
				}

				mv.visitMethodInsn(Opcodes.INVOKESTATIC, org.objectweb.asm.Type.getInternalName(NestedObjectDecorator.class), "create", "(Ldev/nokee/model/internal/names/ElementName;Ljava/lang/reflect/Type;)Ljava/lang/Object;", false);
				mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(returnType.getRawType()));
				mv.visitFieldInsn(Opcodes.PUTFIELD, ownerInternalName, fieldName, org.objectweb.asm.Type.getDescriptor(returnType.getRawType()));
			}

			@Override
			public void visitFields(ClassVisitor cw) {
				FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, fieldName, org.objectweb.asm.Type.getDescriptor(returnType.getRawType()), getGenericSignature(returnType.getType()), null);
				fv.visitEnd();
			}

			@Override
			public void visitMethods(String ownerInternalName, ClassVisitor cw) {
				String methodDescriptor = "()" + org.objectweb.asm.Type.getDescriptor(returnType.getRawType());
				MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, methodName, methodDescriptor, getterSignature(returnType.getType()), null);

				mv.visitCode();

				// Load 'this' onto the stack
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitFieldInsn(Opcodes.GETFIELD, ownerInternalName, fieldName, org.objectweb.asm.Type.getDescriptor(returnType.getRawType()));

				// If not null, skip the initialization block
				Label initDone = new Label();
				mv.visitJumpInsn(Opcodes.IFNONNULL, initDone);

				// Initialize the field
				visitFieldsInitialization(ownerInternalName, mv);

				// Label for the end of the initialization block
				mv.visitLabel(initDone);
				mv.visitFrame(Opcodes.F_APPEND, 1, new Object[] { org.objectweb.asm.Type.getInternalName(returnType.getRawType()) }, 0, null);

				// Load 'this' and get the field value to return it
				mv.visitVarInsn(Opcodes.ALOAD, 0);
				mv.visitFieldInsn(Opcodes.GETFIELD, ownerInternalName, fieldName, org.objectweb.asm.Type.getDescriptor(returnType.getRawType()));
				mv.visitInsn(org.objectweb.asm.Type.getType(returnType.getRawType()).getOpcode(Opcodes.IRETURN));

				mv.visitMaxs(-1, -1);
				mv.visitEnd();
			}
		};
	}

	@SuppressWarnings("unchecked")
	public static Object create(@Nullable ElementName elementName, Type objectType) {
		ModelObjectIdentifier identifier = nextIdentifier();
		if (elementName != null) {
			identifier = identifier.child(elementName);
		}

		if (objectType instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType) objectType).getRawType();
			assert rawType instanceof Class;
			if (NamedDomainObjectProvider.class.isAssignableFrom((Class<?>) rawType)) {
				assert ((ParameterizedType) objectType).getActualTypeArguments().length == 1;
				assert ((ParameterizedType) objectType).getActualTypeArguments()[0] instanceof Class;
				Class<Object> elementType = (Class<Object>) ((ParameterizedType) objectType).getActualTypeArguments()[0];
				return ((ModelObjectRegistry<? super Object>) Objects.requireNonNull(DefaultInstantiator.getNextService().find(registryTypeOf(elementType)))).register(identifier, elementType).asProvider();
			} else if (ModelObject.class.isAssignableFrom((Class<?>) rawType)) {
				assert ((ParameterizedType) objectType).getActualTypeArguments().length == 1;
				assert ((ParameterizedType) objectType).getActualTypeArguments()[0] instanceof Class;
				Class<Object> elementType = (Class<Object>) ((ParameterizedType) objectType).getActualTypeArguments()[0];
				return ((ModelObjectRegistry<? super Object>) Objects.requireNonNull(DefaultInstantiator.getNextService().find(registryTypeOf(elementType)))).register(identifier, elementType);
			} else {
				throw new UnsupportedOperationException(rawType + " -- " + objectType.getTypeName());
			}
		} else {
			assert objectType instanceof Class;
			Class<Object> elementType = (Class<Object>) objectType;
			final ModelObjectRegistry<? super Object> registry = (ModelObjectRegistry<? super Object>) DefaultInstantiator.getNextService().find(registryTypeOf(elementType));
			if (registry != null) {
				return registry.register(identifier, elementType).get();
			} else {
				return ModelObjectIdentifierSupport.newInstance(identifier, () -> ((Instantiator) DefaultInstantiator.getNextService().find(Instantiator.class)).newInstance(elementType));
			}
		}
	}

	// TODO: Should get it from ServiceLookup
	private static ModelObjectIdentifier nextIdentifier() {
		ModelObjectIdentifier result = ModelObjectIdentifierSupport.nextIdentifier();
		if (result == null) {
			result = ModelElementSupport.nextIdentifier();
		}
		return Objects.requireNonNull(result);
	}

	private static String propertyNameOf(MethodMetadata method) {
		return StringUtils.uncapitalize(method.getName().substring("get".length()));
	}

	private static <T> Type registryTypeOf(Class<T> type) {
		return new TypeToken<ModelObjectRegistry<? super T>>() {}.where(new TypeParameter<T>() {}, type).getType();
	}
}
