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

package dev.nokee.platform.base.internal;

import com.google.common.reflect.TypeParameter;
import com.google.common.reflect.TypeToken;
import dev.nokee.internal.Factory;
import dev.nokee.internal.reflect.DefaultInstantiator;
import dev.nokee.model.internal.decorators.ClassGenerationVisitor;
import dev.nokee.model.internal.decorators.Decorator;
import dev.nokee.model.internal.decorators.InjectServiceDecorator;
import dev.nokee.model.internal.type.ModelType;
import dev.nokee.platform.base.TaskView;
import dev.nokee.platform.base.Variant;
import dev.nokee.platform.base.VariantView;
import org.apache.commons.lang3.StringUtils;
import org.gradle.api.Task;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static dev.nokee.internal.reflect.SignatureUtils.getGenericSignature;
import static dev.nokee.internal.reflect.SignatureUtils.getterSignature;

public /*final*/ class NestedViewDecorator implements Decorator {
	// TODO: Make sure objectType can be generic type
	// GENERATE <viewType> get<prop>() {
	//      if (this._nokee_<prop> == null) {
	//          this._nokee_<prop> = <init>;
	//      }
	//      return this._nokee_<prop>;
	// }

	// GENERATE private <viewType> _nokee_<prop> = <init>;

	// <init> => NestedViewDecorator.create(<viewType>)

	public ClassGenerationVisitor applyTo(MethodMetadata method) {
		return new ClassGenerationVisitor() {
			private final String fieldName = "_nokee_" + propertyNameOf(method);
			private final String methodName = method.getName();
			private final String propertyName = propertyNameOf(method);
			private final ModelType<?> returnType = ModelType.of(method.getGenericReturnType());

			@Override
			public void visitFieldsInitialization(String ownerInternalName, MethodVisitor mv) {
				mv.visitVarInsn(Opcodes.ALOAD, 0); // Load 'this' to set the field on

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

				mv.visitMethodInsn(Opcodes.INVOKESTATIC, org.objectweb.asm.Type.getInternalName(NestedViewDecorator.class), "create", "(Ljava/lang/reflect/Type;)Ljava/lang/Object;", false);
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
	public static Object create(Type objectType) {
		if (objectType instanceof ParameterizedType) {
			Type rawType = ((ParameterizedType) objectType).getRawType();
			assert rawType instanceof Class;
			if (TaskView.class.isAssignableFrom((Class<?>) rawType)) {
				Class<? extends Task> elementType = (Class<? extends Task>) ((ParameterizedType) objectType).getActualTypeArguments()[0];
				return ((TaskViewFactory) DefaultInstantiator.getNextService().find(TaskViewFactory.class)).create(elementType);
			} else if (VariantView.class.isAssignableFrom((Class<?>) rawType)) {
				Class<? extends Variant> elementType = (Class<? extends Variant>) ((ParameterizedType) objectType).getActualTypeArguments()[0];
				return ((VariantViewFactory) DefaultInstantiator.getNextService().find(VariantViewFactory.class)).create(elementType);
			} else {
				Factory<?> factory = (Factory<?>) DefaultInstantiator.getNextService().find(factoryOf(TypeToken.of(objectType)));
				if (factory != null) {
					return factory.create();
				}
				throw new UnsupportedOperationException(rawType + " -- " + objectType.getTypeName());
			}
		}
		throw new UnsupportedOperationException(objectType.getTypeName());
	}

	private static String propertyNameOf(MethodMetadata method) {
		return StringUtils.uncapitalize(method.getName().substring("get".length()));
	}

	private static <T> Type factoryOf(TypeToken<T> type) {
		return new TypeToken<Factory<T>>() {}.where(new TypeParameter<T>() {}, type).getType();
	}
}
