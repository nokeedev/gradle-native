package dev.nokee.model

import dev.nokee.model.registry.ModelRegistry

trait ModelBasePluginSpec {
	static String applyModelBasePlugin() {
		return """
			plugins {
				id 'dev.nokee.model-base'
			}

			import ${ModelRegistry.canonicalName}
			import ${NokeeExtension.canonicalName}
		"""
	}
}
