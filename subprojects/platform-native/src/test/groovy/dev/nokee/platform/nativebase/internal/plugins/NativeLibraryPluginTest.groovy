package dev.nokee.platform.nativebase.internal.plugins

import dev.nokee.platform.base.Component
import dev.nokee.platform.nativebase.NativeLibraryExtension
import dev.nokee.platform.nativebase.internal.DefaultMultiLanguageNativeLibraryComponent
import dev.nokee.platform.nativebase.internal.plugins.testers.WellBehavedMultiLanguageSingleEntryPointNokeePluginTest

class NativeLibraryPluginTest extends WellBehavedMultiLanguageSingleEntryPointNokeePluginTest {
	@Override
	protected Class<? extends Component> getComponentTypeUnderTest() {
		return DefaultMultiLanguageNativeLibraryComponent
	}

	@Override
	protected Class<?> getExtensionTypeUnderTest() {
		return NativeLibraryExtension
	}
}
