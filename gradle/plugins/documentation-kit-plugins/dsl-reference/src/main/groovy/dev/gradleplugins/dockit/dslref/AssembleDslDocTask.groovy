/*
 * Copyright 2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package dev.gradleplugins.dockit.dslref

import com.google.common.collect.ImmutableMap
import com.google.common.collect.ImmutableList
import dev.gradleplugins.dockit.DocGenerationException
import dev.gradleplugins.dockit.dsl.docbook.AsciidoctorRenderer
import dev.gradleplugins.dockit.dsl.docbook.DocLinkBuilder
import dev.gradleplugins.dockit.dsl.docbook.DslDocModel
import dev.gradleplugins.dockit.dsl.docbook.model.ClassDoc
import dev.gradleplugins.dockit.dsl.docbook.model.ClassExtensionMetaData
import dev.gradleplugins.dockit.dsl.docbook.model.ExtensionMetaData
import dev.gradleplugins.dockit.dsl.docbook.model.MixinMetaData
import dev.gradleplugins.dockit.dsl.links.ClassLinkMetaData
import dev.gradleplugins.dockit.dsl.links.LinkMetaData
import dev.gradleplugins.dockit.dsl.source.TypeNameResolver
import dev.gradleplugins.dockit.dsl.source.model.ClassMetaData
import dev.gradleplugins.dockit.dsl.source.model.TypeMetaData
import dev.gradleplugins.dockit.model.ClassMetaDataRepository
import dev.gradleplugins.dockit.model.SimpleClassMetaDataRepository
import groovy.text.GStringTemplateEngine
import groovy.xml.dom.DOMCategory
import org.apache.commons.io.FileUtils
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Transformer
import org.gradle.api.file.ConfigurableFileCollection
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ListProperty
import org.gradle.api.tasks.*
import org.w3c.dom.Element

import java.nio.charset.Charset
import java.nio.file.Path

/**
 * Generates the docbook source for the DSL reference guide.
 *
 * Uses the following as input:
 * <ul>
 * <li>Meta-data extracted from the source by {@link dev.nokee.docs.dsl.source.ExtractDslMetaDataTask}.</li>
 * <li>Meta-data about the plugins, in the form of an XML file.</li>
 * <li>{@code sourceFile} - A main docbook template file containing the introductory material and a list of classes to document.</li>
 * <li>{@code classDocbookDir} - A directory that should contain docbook template for each class referenced in main docbook template.</li>
 * </ul>
 *
 * Produces the following:
 * <ul>
 * <li>A docbook book XML file containing the reference.</li>
 * <li>A meta-data file containing information about where the canonical documentation for each class can be found:
 * as dsl doc, javadoc or groovydoc.</li>
 * </ul>
 */
@CacheableTask
abstract class AssembleDslDocTask extends DefaultTask {
	@PathSensitive(PathSensitivity.NONE)
	@InputFiles
	abstract ConfigurableFileCollection getClassMetaDataFiles();

	@PathSensitive(PathSensitivity.NONE)
	@InputFiles
	protected Provider<Object> getInputPluginsMetaDataFile() {
		return getPluginsMetaDataFile().map { it.asFile.exists() ? it : ImmutableList.of() }
	}

	@Internal
	abstract RegularFileProperty getPluginsMetaDataFile();

	@SkipWhenEmpty
	@PathSensitive(PathSensitivity.RELATIVE)
	@InputFiles
	abstract ConfigurableFileCollection getClassDocbookDirectories();

	@Input
	abstract ListProperty<String> getClassNames();

	@OutputDirectory
	abstract DirectoryProperty getDestinationDirectory();

	@PathSensitive(PathSensitivity.NONE)
	@InputFile
	abstract RegularFileProperty getTemplateFile();

	@TaskAction
	def transform() {
		ClassMetaDataRepository<ClassMetaData> classRepository = new SimpleClassMetaDataRepository<ClassMetaData>()
		getClassMetaDataFiles().each {file ->
			ClassMetaDataRepository<ClassMetaData> repository = new SimpleClassMetaDataRepository<ClassMetaData>()
			repository.load(file)
			repository.each { name, ClassMetaData metaData ->
				classRepository.put(name, metaData)
			}
		}

		// TODO: Share the code with extract task
		// Note: If we make the extract task aware of multiple repository, we would only need to resolve the types during extraction.
		TypeNameResolver resolver = new TypeNameResolver(classRepository);
		classRepository.each { name, ClassMetaData metaData ->
			fullyQualifyAllTypeNames(metaData, resolver);
		}

		ClassMetaDataRepository<ClassLinkMetaData> linkRepository = new SimpleClassMetaDataRepository<ClassLinkMetaData>()
		//for every method found in class meta, create a javadoc link
		classRepository.each { name, ClassMetaData metaData ->
			linkRepository.put(name, new ClassLinkMetaData(metaData))
		}

		use(DOMCategory) {
			DslDocModel model = new DslDocModel(classDocbookDirectories.getFiles(), classRepository, loadPluginsMetaData())
			classRepository.each { name, ClassMetaData metaData ->
				if (!metaData.getClassName().contains(".internal.")) {
					if (model.findClassDoc(metaData.getClassName()) == null) {
						println("Unaccounted for public DSL type: " + metaData.getClassName())
					}
				}
			}
			classNames.get().each { className ->
				generateDocForType(model, linkRepository, model.findClassDoc(className))
			}
		}
	}

	void fullyQualifyAllTypeNames(ClassMetaData classMetaData, TypeNameResolver resolver) {
		try {
			classMetaData.resolveTypes(new Transformer<String, String>() {
				public String transform(String i) {
					return resolver.resolve(i, classMetaData);
				}
			});
			classMetaData.visitTypes(new Action<TypeMetaData>() {
				public void execute(TypeMetaData t) {
					resolver.resolve(t, classMetaData);
				}
			});
		} catch (Exception e) {
			throw new RuntimeException("Could not resolve types in class '" + classMetaData.getClassName() + "'.", e);
		}
	}

	def loadPluginsMetaData() {
		if (!pluginsMetaDataFile.get().asFile.exists()) {
			return ImmutableMap.of()
		}
		XIncludeAwareXmlProvider provider = new XIncludeAwareXmlProvider()
		provider.parse(pluginsMetaDataFile.get().asFile)
		Map<String, ClassExtensionMetaData.Builder> extensions = [:]
		provider.root.plugin.each { Element plugin ->
			def pluginId = plugin.'@id'
			if (!pluginId) {
				throw new RuntimeException("No id specified for plugin: ${plugin.'@description' ?: 'unknown'}")
			}
			plugin.extends.each { Element e ->
				def targetClass = e.'@targetClass'
				if (!targetClass) {
					throw new RuntimeException("No targetClass specified for extension provided by plugin '$pluginId'.")
				}
				def extension = extensions[targetClass]
				if (!extension) {
					extension = ClassExtensionMetaData.builder().targetClass(targetClass)
					extensions[targetClass] = extension
				}
				def mixinClass = e.'@mixinClass'
				if (mixinClass) {
					extension.mixinClass(new MixinMetaData(pluginId, mixinClass))
				}
				def extensionClass = e.'@extensionClass'
				if (extensionClass) {
					def extensionId = e.'@id'
					if (!extensionId) {
						throw new RuntimeException("No id specified for extension '$extensionClass' for plugin '$pluginId'.")
					}
					extension.extensionClass(new ExtensionMetaData(pluginId, extensionId, extensionClass))
				}
			}
		}
		return extensions.collectEntries { k, t -> [k: t.build()] }
	}

	def generateDocForType(DslDocModel model, ClassMetaDataRepository<ClassLinkMetaData> linkRepository, ClassDoc classDoc) {
		try {
			//classDoc renderer renders the content of the class and also links to properties/methods
			def linkMetaData = linkRepository.get(classDoc.name)
			linkMetaData.style = LinkMetaData.Style.Dsldoc
			classDoc.classMethods.each { methodDoc ->
				linkMetaData.addMethod(methodDoc.metaData, LinkMetaData.Style.Dsldoc)
			}
			classDoc.classBlocks.each { blockDoc ->
				linkMetaData.addBlockMethod(blockDoc.blockMethod.metaData)
			}
			classDoc.classProperties.each { propertyDoc ->
				linkMetaData.addPropertyAccessorMethod(propertyDoc.name, propertyDoc.metaData.getter ?: propertyDoc.metaData.setter)
			}

			FileUtils.writeStringToFile(outputFileFor(classDoc), serialize(classDoc, new DocLinkBuilder(model)), Charset.defaultCharset());
		} catch (Exception e) {
			throw new DocGenerationException("Failed to generate documentation for class '$classDoc.name'.", e)
		}
	}

	File outputFileFor(ClassDoc element) {
		return getDestinationDirectory().file(element.name + ".adoc").get().getAsFile();
	}

	private static final ThreadLocal<Context> CONTEXT_THREAD_LOCAL = new ThreadLocal<>()
	private static boolean BINDING_PATCH = false
	private static final Object BINDING_PATCH_LOCK = new Object()
	private String serialize(ClassDoc element, DocLinkBuilder linkBuilder) {
		try {
			def bindings = ImmutableMap.of("content", element, "linkBuilder", linkBuilder, "renderer", new AsciidoctorRenderer())
			def workingDirectory = getTemplateFile().get().getAsFile().parentFile.toPath()
			CONTEXT_THREAD_LOCAL.set(new Context(bindings: bindings, workingDirectory: workingDirectory))

			if (!BINDING_PATCH) {
				synchronized (BINDING_PATCH_LOCK) {
					if (!BINDING_PATCH) {
						Binding.metaClass.include << { path ->
							Path p = CONTEXT_THREAD_LOCAL.get().workingDirectory.resolve(path)
							return new GStringTemplateEngine().createTemplate(p.toFile()).make(CONTEXT_THREAD_LOCAL.get().bindings).toString()
						}
						BINDING_PATCH = true
					}
				}
			}
			return new GStringTemplateEngine().createTemplate(getTemplateFile().get().getAsFile()).make(bindings).toString();
		} catch (ClassNotFoundException | IOException e) {
			throw new RuntimeException(e);
		} finally {
			CONTEXT_THREAD_LOCAL.set(null)
		}
	}

	private static final class Context {
		Map bindings
		Path workingDirectory
	}
}

