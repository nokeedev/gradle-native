package dev.nokee.internal.testing.testers;

import org.gradle.api.Plugin;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;

@DisplayName("a well-behaved plugin")
public abstract class WellBehavedPluginTester {
	protected abstract String getQualifiedPluginIdUnderTest();
	protected abstract Class<? extends Plugin<?>> getPluginTypeUnderTest();

	@Nested
	@DisplayName("can apply plugin by id")
	class ApplyById extends WellBehavedPluginApplyByIdTester {
		@Override
		protected String getQualifiedPluginIdUnderTest() {
			return WellBehavedPluginTester.this.getQualifiedPluginIdUnderTest();
		}

		@Override
		protected Class<? extends Plugin<?>> getPluginTypeUnderTest() {
			return WellBehavedPluginTester.this.getPluginTypeUnderTest();
		}
	}

	@Nested
	@DisplayName("can apply plugin by type")
	class ApplyByType extends WellBehavedPluginApplyByTypeTester {
		@Override
		protected Class<? extends Plugin<?>> getPluginTypeUnderTest() {
			return WellBehavedPluginTester.this.getPluginTypeUnderTest();
		}
	}

	@Nested
	@DisplayName("can apply plugin via plugin DSL")
	class ApplyByIdViaPluginDsl extends WellBehavedPluginApplyViaPluginDslTester {
		@Override
		protected String getQualifiedPluginIdUnderTest() {
			return WellBehavedPluginTester.this.getQualifiedPluginIdUnderTest();
		}
	}

	@Nested
	@DisplayName("can execute basic help task")
	class ExecuteBasicHelpTasks extends WellBehavedPluginBasicHelpTasksTester {
		@Override
		protected String getQualifiedPluginIdUnderTest() {
			return WellBehavedPluginTester.this.getQualifiedPluginIdUnderTest();
		}
	}

	@Nested
	@DisplayName("can avoid task configuration")
	class AvoidTaskConfiguration extends WellBehavedPluginTaskConfigurationAvoidanceTester {
		@Override
		protected String getQualifiedPluginIdUnderTest() {
			return WellBehavedPluginTester.this.getQualifiedPluginIdUnderTest();
		}
	}
}
