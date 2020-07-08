package dev.nokee.ide.visualstudio.fixtures

import dev.gradleplugins.test.fixtures.file.TestFile
import org.apache.commons.io.FilenameUtils

import javax.annotation.Nullable

class VisualStudioIdeProjectFixture {
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

	VisualStudioIdeProjectFixture assertHasBuildFiles(String... files) {
		return assertHasBuildFiles(Arrays.asList(files))
	}

	VisualStudioIdeProjectFixture assertHasBuildFiles(Iterable<String> files) {
		assert projectFile.buildFiles == files as Set
		assert filtersFile.filesWithoutFilter == files as Set
		return this
	}

	VisualStudioIdeProjectFixture assertHasTargets(String... targetNames) {
		assert projectFile.targets*.name as Set == targetNames as Set
		return this
	}

	List<ProjectFixture.ProjectConfigurationFixture> getProjectConfigurations() {
		return projectFile.projectConfigurations
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
			String configurationName
			String platformName

			ProjectConfigurationFixture(String configurationName, String platformName) {
				this.configurationName = configurationName
				this.platformName = platformName
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
			def paths = content.ItemGroup*.children().flatten().findAll { it.name().localPart != 'Filter' && it.Filter == null }
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
