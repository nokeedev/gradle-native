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

import org.objectweb.asm.signature.SignatureWriter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.TypeVariable;
import java.lang.reflect.WildcardType;

public final class SignatureUtils {

	public static String getConstructorSignature(Constructor<?> constructor) {
		SignatureWriter signatureWriter = new SignatureWriter();

		// Add generic type parameters of the class (if any)
		TypeVariable<?>[] typeParameters = constructor.getDeclaringClass().getTypeParameters();
		for (TypeVariable<?> typeParam : typeParameters) {
			signatureWriter.visitFormalTypeParameter(typeParam.getName());
			Type[] bounds = typeParam.getBounds();
			// Considering only the first bound here, multiple bounds would require additional handling
			if (bounds.length > 0 && bounds[0] instanceof Class) {
				signatureWriter.visitClassBound().visitClassType(((Class<?>) bounds[0]).getName().replace('.', '/'));
				signatureWriter.visitEnd();
			}
		}

		// Add method parameter types
		signatureWriter.visitParameterType();
		Type[] genericParameterTypes = constructor.getGenericParameterTypes();
		for (Type genericParameterType : genericParameterTypes) {
			generateSignature(signatureWriter, genericParameterType);
		}

		// Add return type (void for constructors)
		signatureWriter.visitReturnType().visitBaseType('V');

		return signatureWriter.toString();
	}

	public static String getMethodSignature(Method method) {
		SignatureWriter signatureWriter = new SignatureWriter();

		// Add generic type parameters of the method (if any)
		TypeVariable<Method>[] typeParameters = method.getTypeParameters();
		for (TypeVariable<?> typeParam : typeParameters) {
			signatureWriter.visitFormalTypeParameter(typeParam.getName());
			Type[] bounds = typeParam.getBounds();
			for (Type bound : bounds) {
				signatureWriter.visitClassBound();
				generateSignature(signatureWriter, bound);
			}
		}

		// Add method parameter types
		Type[] genericParameterTypes = method.getGenericParameterTypes();
		for (Type genericParameterType : genericParameterTypes) {
			signatureWriter.visitParameterType();
			generateSignature(signatureWriter, genericParameterType);
		}

		// Add method return type
		Type genericReturnType = method.getGenericReturnType();
		signatureWriter.visitReturnType();
		generateSignature(signatureWriter, genericReturnType);

		return signatureWriter.toString();
	}

	public static String getterSignature(Type genericReturnType) {
		SignatureWriter signatureWriter = new SignatureWriter();

		// Add method return type
		signatureWriter.visitReturnType();
		generateSignature(signatureWriter, genericReturnType);

		return signatureWriter.toString();
	}

	public static String getGenericSignature(Type type) {
		SignatureWriter signatureWriter = new SignatureWriter();

		generateSignature(signatureWriter, type);

		return signatureWriter.toString();
	}

	private static void generateSignature(SignatureWriter signatureWriter, Type type) {
		if (type instanceof Class) {
			Class<?> clazz = (Class<?>) type;
			if (clazz.isArray()) {
				signatureWriter.visitArrayType();
				generateSignature(signatureWriter, clazz.getComponentType());
			} else {
				String internalName = org.objectweb.asm.Type.getInternalName(clazz);
				signatureWriter.visitClassType(internalName);
				signatureWriter.visitEnd();
			}
		} else if (type instanceof ParameterizedType) {
			ParameterizedType parameterizedType = (ParameterizedType) type;
			Type rawType = parameterizedType.getRawType();

			// For parameterized types, visit the class type and its type arguments
			if (rawType instanceof Class) {
				Class<?> rawTypeClass = (Class<?>) rawType;
				signatureWriter.visitClassType(org.objectweb.asm.Type.getInternalName(rawTypeClass));
				for (Type argType : parameterizedType.getActualTypeArguments()) {
					if (argType instanceof WildcardType) {
						// Wildcard type, can add more specific handling for extends/super here
						signatureWriter.visitTypeArgument();
					} else {
						signatureWriter.visitTypeArgument('=');
						generateSignature(signatureWriter, argType);
					}
				}
				signatureWriter.visitEnd();
			}
		} else if (type instanceof TypeVariable<?>) {
			TypeVariable<?> typeVariable = (TypeVariable<?>) type;
			// Visit a type variable
			signatureWriter.visitTypeVariable(typeVariable.getName());
		}
		// If other types like GenericArrayType or WildcardType are also to be supported,
		// corresponding else if blocks should be added here.
	}
}
