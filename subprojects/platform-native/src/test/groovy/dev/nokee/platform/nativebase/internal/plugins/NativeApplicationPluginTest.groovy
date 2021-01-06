package dev.nokee.platform.nativebase.internal.plugins

import dev.nokee.platform.base.Component
import dev.nokee.platform.nativebase.NativeApplicationExtension
import dev.nokee.platform.nativebase.internal.plugins.testers.WellBehavedMultiLanguageSingleEntryPointNokeePluginTest

class NativeApplicationPluginTest extends WellBehavedMultiLanguageSingleEntryPointNokeePluginTest {
	@Override
	protected Class<? extends Component> getComponentTypeUnderTest() {
		return NativeApplicationExtension
	}

	@Override
	protected Class<?> getExtensionTypeUnderTest() {
		return NativeApplicationExtension
	}
}
