package dev.nokee.runtime.nativebase;

import dev.nokee.runtime.base.internal.RuntimeBasePlugin;
import dev.nokee.runtime.nativebase.internal.NativeRuntimeBasePlugin;
import lombok.val;
import org.gradle.api.Project;
import org.junit.jupiter.api.Test;

import static dev.gradleplugins.grava.testing.util.ProjectTestUtils.rootProject;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class NativeRuntimeBasePluginTest {
	private static Project createSubject() {
		val project = rootProject();
		project.getPluginManager().apply(NativeRuntimeBasePlugin.class);
		return project;
	}

	@Test
	void appliesRuntimeBasePlugin() {
		assertThat(createSubject().getPlugins(), hasItem(isA(RuntimeBasePlugin.class)));
	}

	@Test
	void registerOperatingSystemFamilyAttribute() {
		assertThat(createSubject().getDependencies().getAttributesSchema().getAttributes(),
			hasItem(is(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE)));
	}

	@Test
	void registerMachineArchitectureAttribute() {
		assertThat(createSubject().getDependencies().getAttributesSchema().getAttributes(),
			hasItem(is(MachineArchitecture.ARCHITECTURE_ATTRIBUTE)));
	}

	@Test
	void registerBuildTypeAttribute() {
		assertThat(createSubject().getDependencies().getAttributesSchema().getAttributes(),
			hasItem(is(BuildType.BUILD_TYPE_ATTRIBUTE)));
	}

	@Test
	void registerBinaryLinkageAttribute() {
		assertThat(createSubject().getDependencies().getAttributesSchema().getAttributes(),
			hasItem(is(BinaryLinkage.BINARY_LINKAGE_ATTRIBUTE)));
	}
}
