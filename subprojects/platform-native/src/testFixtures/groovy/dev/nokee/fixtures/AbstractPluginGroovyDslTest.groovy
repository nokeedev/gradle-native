package dev.nokee.fixtures

import dev.nokee.internal.testing.utils.TestUtils
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.SetProperty
import org.gradle.util.GUtil
import spock.lang.Specification
import spock.lang.Unroll

import java.lang.reflect.Method

abstract class AbstractPluginGroovyDslTest extends Specification {
	@Unroll
	def "has Groovy DSL support [#propertyUnderTest]"(propertyUnderTest) {
		expect:
		assertHasGroovyDsl(propertyUnderTest)

		where:
		propertyUnderTest << discoverTypes(pluginId)
	}

	private static final Set<Class<?>> PROPERTY_TYPES = [Property, SetProperty, ListProperty] as Set
	protected boolean isPropertyGetter(Method method) {
		return method.getName().startsWith("get") && PROPERTY_TYPES.contains(method.returnType) && method.parameterCount == 0;
	}

	protected void assertHasGroovyDsl(PropertyGetter propertyUnderTest) {
		assertHasGroovyDslSetter(propertyUnderTest.type, propertyUnderTest.name)
	}

	protected void assertHasGroovyDslSetter(Class type, String getterName) {
		def groovyDslSetters = type.methods.findAll { it.name == "set${getterName.substring(3)}" && it.parameterCount == 1 && it.parameterTypes[0].equals(Object) }
		assert groovyDslSetters.size() == 1 : "no setter for getter ${getterName} in type ${type.canonicalName}"
	}

	protected String getPluginId() {
		return 'dev.nokee.' + GUtil.toWords(getClass().simpleName.replace('Plugin_GroovyDslTest', '')).split(' ').collect { it.toLowerCase() }.join('-')
	}

	private List<PropertyGetter> discoverTypes(String pluginId) {
		def project = TestUtils.rootProject()
		project.apply(plugin: pluginId)
		project.evaluate()

		return project.extensions.extensionsSchema.collect { project.extensions.getByName(it.name).getClass() }.findAll { it.canonicalName.startsWith('dev.nokee') }.collect { type ->
			type.methods.findAll { isPropertyGetter(it) }.collect {
				new PropertyGetter(type, it.name)
			}
		}.flatten()
	}

	static class PropertyGetter {
		private final Class<?> type
		private final String name

		PropertyGetter(Class<?> type, String name) {
			this.type = type
			this.name = name
		}

		@Override
		public String toString() {
			return "${type.simpleName}#${name}"
		}
	}
}
