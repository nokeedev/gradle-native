package dev.nokee.model

import dev.gradleplugins.integtests.fixtures.AbstractGradleSpecification
import dev.nokee.model.registry.ModelRegistry

abstract class AbstractModelBasePluginFunctionalTest extends AbstractGradleSpecification {
	protected static String applyModelBasePlugin() {
		return """
			plugins {
				id 'dev.nokee.model-base'
			}

			import ${ModelRegistry.canonicalName}
			import ${NokeeExtension.canonicalName}
		"""
	}
}
