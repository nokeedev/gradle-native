/*
 * Copyright 2020 the original author or authors.
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
package dev.nokee.ide.visualstudio.fixtures

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.ide.fixtures.IdePathUtils
import dev.nokee.ide.fixtures.IdeProjectFixture
import org.apache.commons.io.FilenameUtils

import javax.annotation.Nullable

class VisualStudioIdeProjectFixture implements IdeProjectFixture {
	final TestFile project
	final TestFile filters
	final ProjectFixture projectFile
	final FiltersFixture filtersFile

    VisualStudioIdeProjectFixture(TestFile projectLocation) {
		project = projectLocation.assertIsFile()
		filters = projectLocation.parentFile.file(projectLocation.name + ".filters").assertIsFile()
		projectFile = new ProjectFixture(project)
		filtersFile = new FiltersFixture(filters)
	}

	static VisualStudioIdeProjectFixture of(Object path) {
		return new VisualStudioIdeProjectFixture(TestFile.of(new File(projectName(path))))
	}

	static String projectName(Object path) {
		return IdePathUtils.addExtensionIfAbsent(path, 'vcxproj')
	}

	static String filtersName(Object path) {
		return IdePathUtils.addExtensionIfAbsent(path, 'vcxproj.filters')
	}

	VisualStudioIdeProjectFixture assertHasSourceLayout(String... files) {
		return assertHasSourceLayout(Arrays.asList(files))
	}

	VisualStudioIdeProjectFixture assertHasSourceLayout(Iterable<String> files) {
		assert filtersFile.getSourceLayout() == files as Set
		return this
	}

	VisualStudioIdeProjectFixture assertHasSourceFiles(String... files) {
		return assertHasSourceFiles(Arrays.asList(files))
	}

	VisualStudioIdeProjectFixture assertHasSourceFiles(Iterable<String> files) {
		assert projectFile.sourceFiles == files as Set
		assert filtersFile.getFilesWithFilter('Source Files') == files as Set
		return this
	}

	VisualStudioIdeProjectFixture assertHasHeaderFiles(String... files) {
		return assertHasHeaderFiles(Arrays.asList(files))
	}

	VisualStudioIdeProjectFixture assertHasHeaderFiles(Iterable<String> files) {
		assert projectFile.headerFiles == files as Set
		assert filtersFile.getFilesWithFilter('Header Files') == files as Set
		return this
	}

	VisualStudioIdeProjectFixture assertHasResourceFiles(String... files) {
		return assertHasResourceFiles(Arrays.asList(files))
	}

	VisualStudioIdeProjectFixture assertHasResourceFiles(Iterable<String> files) {
		assert projectFile.resourceFiles == files as Set
		assert filtersFile.getFilesWithFilter('Resource Files') == files as Set
		return this
	}

	@Override
	VisualStudioIdeProjectFixture assertHasBuildFiles(Iterable<String> files) {
		assert projectFile.buildFiles.collect { relativePathToIdeProject(it) } as Set == files as Set
		assert filtersFile.filesWithoutFilter.collect { relativePathToIdeProject(it) } as Set == files as Set
		return this
	}

	private String relativePathToIdeProject(String path) {
		return FilenameUtils.separatorsToUnix(path).replace(FilenameUtils.separatorsToUnix(projectFile.file.parentFile.absolutePath), '').substring(1)
	}

	VisualStudioIdeProjectFixture assertHasTargets(String... targetNames) {
		assert projectFile.targets*.name as Set == targetNames as Set
		return this
	}

	List<ProjectFixture.ProjectConfigurationFixture> getProjectConfigurations() {
		return projectFile.projectConfigurations
	}

	VisualStudioIdeProjectFixture assertHasProjectConfigurations(String... projectConfigurations) {
		assert projectFile.projectConfigurations*.name as Set == projectConfigurations as Set
		return this
	}

	@Override
	IdeProjectFixture assertHasBuildTypes(Iterable<String> buildTypes) {
		assert projectFile.projectConfigurations*.configurationName as Set == buildTypes as Set
		return this
	}

	ProjectFixture.TargetFixture getTargetByName(String targetName) {
		def target = projectFile.targets.find { it.name == targetName }
		assert target != null
		return target
	}

	VisualStudioIdeProjectFixture assertHasTarget(String targetName) {
		assert projectFile.targets*.name.contains(targetName)
		return this
	}

	static class ProjectFixture {
		final TestFile file
		final Node content

		ProjectFixture(TestFile file) {
			this.file = file
			this.content = new XmlParser().parse(file)
		}

		Set<String> getSourceFiles() {
			def sources = itemGroup('Sources')?.ClCompile
			return normalise(sources*.'@Include')
		}

		Set<String> getHeaderFiles() {
			def sources = itemGroup('Headers')?.ClInclude
			return normalise(sources*.'@Include')
		}

		Set<String> getResourceFiles() {
			def sources = itemGroup('References')?.ResourceCompile
			return normalise(sources*.'@Include')
		}

		Set<String> getBuildFiles() {
			def sources = itemGroup('Builds')?.None
			return normalise(sources*.'@Include')
		}

		List<TargetFixture> getTargets() {
			return content.Target.collect { new TargetFixture(it) }
		}

		List<ProjectConfigurationFixture> getProjectConfigurations() {
			return itemGroup("ProjectConfigurations").collect {
				new ProjectConfigurationFixture(it.Configuration[0].text(), it.Platform[0].text())
			}
		}

		private Node itemGroup(String label) {
			return content.ItemGroup.find({it.'@Label' == label}) as Node
		}

		class TargetFixture {
			final Node content

			TargetFixture(Node node) {
				this.content = node
			}

			String getName() {
				return content.'@Name'
			}

			String getCommand() {
				assert content.Exec.size() == 1
				return content.Exec.'@Command'.iterator().next()
			}

			/**
			 * Asserts the target delegate to Gradle build tool.
			 * Delegating to Gradle means several things:
			 * <ul>
			 *     <li>a build tool path ending with Gradle execution entry point</li>
			 *     <li>build arguments forwarding important build settings to Gradle via project properties</li>
			 *     <li>build arguments containing the Xcode IDE bridge task format</li>
			 * </ul>
			 *
			 * @return this fixture instance, never null
			 */
			TargetFixture assertTargetDelegateToGradle() {
				assert getCommand() =~ /\/bin\/gradle(w)?" /
				getCommand().with {
					assert it.contains('-Pdev.nokee.internal.visualStudio.bridge.OutDir=$(OutDir)')
					assert it.contains('-Pdev.nokee.internal.visualStudio.bridge.PlatformName=$(PlatformName)')
					assert it.contains('-Pdev.nokee.internal.visualStudio.bridge.Configuration=$(Configuration)')
					assert it.contains('-Pdev.nokee.internal.visualStudio.bridge.ProjectName=$(ProjectName)')
					assert it =~ /-Pdev.nokee.internal.visualStudio.bridge.Action=(build|clean)/
					assert it =~ /:_visualStudio__(build|clean)_\$\(ProjectName\)_\$\(Configuration\)_\$\(Platform\)/
				}
				return this
			}
		}

		class ProjectConfigurationFixture {
			final String configurationName
			final String platformName

			ProjectConfigurationFixture(String configurationName, String platformName) {
				this.configurationName = configurationName
				this.platformName = platformName
			}

			String getName() {
				return "${configurationName}|${platformName}"
			}

			@Nullable
			String getLanguageStandard() {
				def itemDefinitionGroupNode = content.ItemDefinitionGroup.find({ it.'@Condition' == condition }) as Node
				if (itemDefinitionGroupNode == null) {
					return null
				}
				return itemDefinitionGroupNode.ClCompile[0].LanguageStandard[0]?.text()
			}

			private String getCondition() {
				"'\$(Configuration)|\$(Platform)'=='${configurationName}|${platformName}'"
			}
		}
	}

	static class FiltersFixture {
		final TestFile file
		final Node content

		FiltersFixture(TestFile file) {
			this.file = file
			this.content = new XmlParser().parse(file)
		}

		Set<String> getSourceLayout() {
			Map<String, List<String>> filesByGroup = [:].withDefault { [] }
			content.ItemGroup*.children().flatten().findAll { it.name().localPart != 'Filter' }.each {
				filesByGroup[it.Filter.text()].add(FilenameUtils.getName(it.'@Include'))
			}

			def allGroups = content.ItemGroup*.children().flatten().findAll { it.name().localPart == 'Filter' }*.'@Include' as Set
			assert allGroups.containsAll(filesByGroup.keySet() - [''])
			return filesByGroup.collect { k, v -> v.collect {
					if (k.empty) {
						return it
					}
					return "$k/$it".toString()
				}}.flatten()
		}

		Set<String> getFilesWithFilter(String filter) {
			def paths = content.ItemGroup*.children().flatten().findAll { it.name().localPart != 'Filter' && it.Filter.text() == filter }
			return normalise(paths*.'@Include')
		}

		Set<String> getFilesWithoutFilter() {
			def paths = content.ItemGroup*.children().flatten().findAll { it.name().localPart != 'Filter' && it.Filter.isEmpty() }
			return normalise(paths*.'@Include')
		}
	}

	private static List<String> normalise(List<String> files) {
		if (files == null) {
			return []
		}
		return files.collect({ FilenameUtils.separatorsToUnix(it)}).sort()
	}
}
