/*
 * Copyright 2021 the original author or authors.
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
package dev.nokee.platform.jni;

import dev.nokee.internal.testing.AbstractPluginTest;
import dev.nokee.internal.testing.PluginRequirement;
import dev.nokee.language.c.internal.plugins.CLanguageBasePlugin;
import dev.nokee.language.jvm.internal.plugins.JvmLanguageBasePlugin;
import dev.nokee.model.internal.registry.ModelRegistry;
import dev.nokee.platform.base.Binary;
import dev.nokee.platform.base.BinaryView;
import dev.nokee.platform.base.VariantView;
import dev.nokee.platform.base.internal.plugins.ComponentModelBasePlugin;
import dev.nokee.platform.base.testers.BinaryAwareComponentTester;
import dev.nokee.platform.base.testers.ComponentTester;
import dev.nokee.platform.base.testers.DependencyAwareComponentTester;
import dev.nokee.platform.base.testers.VariantAwareComponentTester;
import dev.nokee.platform.nativebase.NativeLibrary;
import dev.nokee.platform.nativebase.testers.TargetMachineAwareComponentTester;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;

import static dev.nokee.platform.jni.internal.plugins.JniLibraryPlugin.javaNativeInterfaceLibrary;

@PluginRequirement.Require(type = ComponentModelBasePlugin.class)
class JavaNativeInterfaceLibraryComponentTest extends AbstractPluginTest implements ComponentTester<JavaNativeInterfaceLibrary>
	, DependencyAwareComponentTester<JavaNativeInterfaceLibraryComponentDependencies>
	, VariantAwareComponentTester<VariantView<NativeLibrary>>
	, BinaryAwareComponentTester<BinaryView<Binary>>
	, TargetMachineAwareComponentTester
{
	private JavaNativeInterfaceLibrary subject;

	@BeforeEach
	void createSubject() {
		project.getPluginManager().apply(CLanguageBasePlugin.class);
		project.getPluginManager().apply(JvmLanguageBasePlugin.class);
		this.subject = project.getExtensions().getByType(ModelRegistry.class).register(javaNativeInterfaceLibrary("quzu", project)).as(JavaNativeInterfaceLibrary.class).get();
	}

	@Override
	public JavaNativeInterfaceLibrary subject() {
		return subject;
	}

	@Nested
	class ComponentSourcesTest extends JavaNativeInterfaceLibrarySourcesIntegrationTester {
		@Override
		public JavaNativeInterfaceLibrarySources subject() {
			return subject.getSources();
		}
	}
}
