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

import dev.nokee.internal.reflect.DefaultInstantiator;
import dev.nokee.model.internal.type.ModelType;
import org.apache.commons.lang3.StringUtils;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.Label;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

import static dev.nokee.internal.reflect.SignatureUtils.getGenericSignature;
import static dev.nokee.internal.reflect.SignatureUtils.getterSignature;

public /*final*/ class InjectServiceDecorator implements Decorator {
	// GENERATE <serviceType> get<prop>() {
	//      if (this._nokee_<prop> == null) {
	//          this._nokee_<prop> = <init>;
	//      }
	//      return this._nokee_<prop>;
	// }

	// GENERATE private <serviceType> _nokee_<prop> = <init>;

	// <init> => (<serviceType>) InjectServiceDecorator.get(<serviceType>)

	@Override
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
					mv.visitLdcInsn(org.objectweb.asm.Type.getType((Class<?>) ((ParameterizedType) returnType.getType()).getActualTypeArguments()[0]));
					mv.visitMethodInsn(Opcodes.INVOKESTATIC, org.objectweb.asm.Type.getInternalName(InjectServiceDecorator.class), "typeOf", "(Ljava/lang/Class;Ljava/lang/Class;)Ljava/lang/reflectType;", false);
				} else {
					assert returnType.getType() instanceof Class;
					mv.visitLdcInsn(org.objectweb.asm.Type.getType((Class<?>) returnType.getType()));
				}
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, org.objectweb.asm.Type.getInternalName(InjectServiceDecorator.class), "getService", "(Ljava/lang/reflect/Type;)Ljava/lang/Object;", false);
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

	private static String propertyNameOf(MethodMetadata method) {
		return StringUtils.uncapitalize(method.getName().substring("get".length()));
	}

	public static Object getService(Type serviceType) {
		return DefaultInstantiator.getNextService().find(serviceType);
	}

	public static Type typeOf(Class<?> rawType, Class<?> firstTypeArgument) {
		return new ParameterizedType() {
			@Override
			public Type[] getActualTypeArguments() {
				return new Type[] { firstTypeArgument };
			}

			@Override
			public Type getRawType() {
				return rawType;
			}

			@Override
			public Type getOwnerType() {
				return null;
			}
		};
	}
}
