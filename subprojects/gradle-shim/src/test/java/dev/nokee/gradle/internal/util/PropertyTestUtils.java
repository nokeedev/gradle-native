/*
 * Copyright 2022 the original author or authors.
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
package dev.nokee.gradle.internal.util;

import org.gradle.api.model.ObjectFactory;
import org.gradle.testfixtures.ProjectBuilder;

import java.util.stream.Stream;

import static dev.nokee.gradle.internal.util.ClassTestUtils.allInterfaces;

public final class PropertyTestUtils {
	private PropertyTestUtils() {}

	public static Stream<Class<?>> allPropertyInterfaces() {
		ObjectFactory objectFactory = ProjectBuilder.builder().build().getObjects();
		return allInterfaces(objectFactory.property(MyType.class).getClass());
	}

	public static Class<?> propertyClassUnderTest() throws ClassNotFoundException {
		return Class.forName(System.getProperty("packageUnderTest") + ".DefaultProperty");
	}

	public static Stream<Class<?>> allSetPropertyInterfaces() {
		ObjectFactory objectFactory = ProjectBuilder.builder().build().getObjects();
		return allInterfaces(objectFactory.setProperty(MyType.class).getClass());
	}

	public static Class<?> setPropertyClassUnderTest() throws ClassNotFoundException {
		return Class.forName(System.getProperty("packageUnderTest") + ".DefaultSetProperty");
	}

	public static Stream<Class<?>> allListPropertyInterfaces() {
		ObjectFactory objectFactory = ProjectBuilder.builder().build().getObjects();
		return allInterfaces(objectFactory.listProperty(MyType.class).getClass());
	}

	public static Class<?> listPropertyClassUnderTest() throws ClassNotFoundException {
		return Class.forName(System.getProperty("packageUnderTest") + ".DefaultListProperty");
	}

	private interface MyType {}
}
