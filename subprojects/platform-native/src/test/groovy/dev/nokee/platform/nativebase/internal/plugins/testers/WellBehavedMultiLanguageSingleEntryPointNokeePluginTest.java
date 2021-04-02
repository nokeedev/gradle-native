package dev.nokee.platform.nativebase.internal.plugins.testers;

import dev.gradleplugins.grava.testing.WellBehavedPluginTester;
import dev.gradleplugins.grava.testing.util.TestCaseUtils;
import dev.nokee.platform.base.Component;
import org.gradle.api.Plugin;
import org.gradle.util.GUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.TestFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public abstract class WellBehavedMultiLanguageSingleEntryPointNokeePluginTest {
	/**
	 * Returns the fully qualified plugin ID under test, i.e. {@literal dev.nokee.cpp-application}.
	 *
	 * @return the fully qualified plugin ID to test, ever null.
	 */
	protected String getQualifiedPluginIdUnderTest() {
		return "dev.nokee." + guessPluginName();
	}

	private String guessPluginName() {
		Matcher matcher = Pattern.compile("(\\w+)PluginTest").matcher(getClass().getSimpleName());
		if (matcher.matches()) {
			return GUtil.toWords(matcher.group(1), '-');
		}
		throw new UnsupportedOperationException(String.format("Cannot determine plugin id from class name '%s'.", getClass().getSimpleName()));
	}

	/**
	 * Returns the plugin type under test, i.e. {@link dev.nokee.platform.cpp.internal.plugins.CppApplicationPlugin}.
	 *
	 * @return the plugin type to test, never null.
	 */
	@SuppressWarnings("unchecked")
	protected Class<? extends Plugin<?>> getPluginTypeUnderTest() {
		try {
			return (Class<? extends Plugin<?>>) Class.forName(this.getClass().getPackage().getName() + "." + guessClassName());
		} catch (ClassNotFoundException e) {
			throw new UnsupportedOperationException(String.format("Cannot determine plugin type, please override getPluginTypeUnderTest()."), e);
		}
	}

	private String guessClassName() {
		Matcher matcher = Pattern.compile("(\\w+)Test").matcher(getClass().getSimpleName());
		if (matcher.matches()) {
			return matcher.group(1);
		}
		throw new UnsupportedOperationException(String.format("Cannot determine plugin type from class name '%s'.", getClass().getSimpleName()));
	}

	/**
	 * Returns the extension name under test, i.e. {@literal application}.
	 *
	 * @return the extension name to test, never null.
	 */
	protected String getExtensionNameUnderTest() {
		return guessExtensionName();
	}

	private String guessExtensionName() {
		if (getClass().getSimpleName().contains("Application")) {
			return "application";
		} else if (getClass().getSimpleName().contains("Library")) {
			return "library";
		}
		throw new UnsupportedOperationException(String.format("Cannot determine entry point extension name from class name '%s'.", getClass().getSimpleName()));
	}

	protected abstract Class<? extends Component> getComponentTypeUnderTest();
	protected abstract Class<?> getExtensionTypeUnderTest();

	@Nested
	@DisplayName("a well-behaved plugin")
	class WellBehavedPluginTest {
		@TestFactory
		Stream<DynamicTest> checkWellBehavedPlugin() {
			return new WellBehavedPluginTester()
				.qualifiedPluginId(WellBehavedMultiLanguageSingleEntryPointNokeePluginTest.this.getQualifiedPluginIdUnderTest())
				.pluginClass(WellBehavedMultiLanguageSingleEntryPointNokeePluginTest.this.getPluginTypeUnderTest())
				.stream().map(TestCaseUtils::toJUnit5DynamicTest);
		}
	}

	@Nested
	@DisplayName("a main component provider plugin")
	class MainComponentProvider extends MainComponentPluginTester {
		@Override
		protected Class<? extends Component> getComponentType() {
			return getComponentTypeUnderTest();
		}

		@Override
		protected String getQualifiedPluginId() {
			return WellBehavedMultiLanguageSingleEntryPointNokeePluginTest.this.getQualifiedPluginIdUnderTest();
		}
	}

	@Nested
	@DisplayName("a main entry point extension provider plugin")
	class MainEntryPointExtensionProvider extends MainEntryPointExtensionPluginTester {
		@Override
		protected String getExtensionName() {
			return getExtensionNameUnderTest();
		}

		@Override
		protected Class<?> getExtensionType() {
			return getExtensionTypeUnderTest();
		}

		@Override
		protected String getQualifiedPluginId() {
			return WellBehavedMultiLanguageSingleEntryPointNokeePluginTest.this.getQualifiedPluginIdUnderTest();
		}
	}
}
