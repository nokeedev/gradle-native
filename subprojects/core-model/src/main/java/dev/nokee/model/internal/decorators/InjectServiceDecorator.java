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
import org.gradle.api.plugins.ExtensionAware;
import org.gradle.api.plugins.ExtensionContainer;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.FieldVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.lang.reflect.Type;

import static dev.nokee.internal.reflect.SignatureUtils.getterSignature;

public /*final*/ class InjectServiceDecorator implements Decorator {
	// GENERATE <serviceType> get<prop>() {
	//      if (this._nokee_<prop> == null) {
	//          this._nokee_<prop> = (<serviceType>) InjectServiceDecorator.get(<serviceType>);
	//      }
	//      return this._nokee_<prop>;
	// }

	// GENERATE private <serviceType> _nokee_<prop> = (<serviceType>) InjectServiceDecorator.get(<serviceType>);

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
				mv.visitMethodInsn(Opcodes.INVOKESTATIC, org.objectweb.asm.Type.getInternalName(DefaultInstantiator.class), "getNext", "()Ldev/nokee/internal/reflect/DefaultInstantiator$PropertyInit;", false);
				mv.visitLdcInsn(propertyName);
				mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(DefaultInstantiator.PropertyInit.class), "init", "(Ljava/lang/String;)Ljava/lang/Object;", true);
				mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(returnType.getRawType()));
				mv.visitFieldInsn(Opcodes.PUTFIELD, ownerInternalName, fieldName, org.objectweb.asm.Type.getDescriptor(returnType.getRawType()));
			}

			@Override
			public void visitFields(ClassVisitor cw) {
				FieldVisitor fv = cw.visitField(Opcodes.ACC_PRIVATE, fieldName, org.objectweb.asm.Type.getDescriptor(returnType.getRawType()), null, null);
				fv.visitEnd();
			}

			@Override
			public void visitMethods(ClassVisitor cw) {
				String methodDescriptor = "()" + org.objectweb.asm.Type.getDescriptor(returnType.getRawType());
				MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, methodName, methodDescriptor, getterSignature(returnType.getType()), null);

				mv.visitCode();

				// Load 'this' onto the stack
				mv.visitVarInsn(Opcodes.ALOAD, 0);

				// Cast 'this' to ExtensionAware
				mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(ExtensionAware.class));

				// Invoke getExtensions() on the ExtensionAware object
				mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(ExtensionAware.class), "getExtensions", "()Lorg/gradle/api/plugins/ExtensionContainer;", true);

				// Load the propertyName onto the stack
				mv.visitLdcInsn(propertyName);

				// Invoke getByName(propertyName) on the extensions map
				mv.visitMethodInsn(Opcodes.INVOKEINTERFACE, org.objectweb.asm.Type.getInternalName(ExtensionContainer.class), "getByName", "(Ljava/lang/String;)Ljava/lang/Object;", true);

				// Cast the result to the return type
				mv.visitTypeInsn(Opcodes.CHECKCAST, org.objectweb.asm.Type.getInternalName(returnType.getRawType()));

				// Return the result
				mv.visitInsn(org.objectweb.asm.Type.getType(returnType.getRawType()).getOpcode(Opcodes.IRETURN));

				mv.visitMaxs(-1, -1); // Auto compute stack and local variables size
				mv.visitEnd();
			}
		};
	}

	private static String propertyNameOf(MethodMetadata method) {
		return StringUtils.uncapitalize(method.getName().substring("get".length()));
	}

	public static Object get(Type serviceType) {
		return DefaultInstantiator.getNextService().find(serviceType);
	}
}
