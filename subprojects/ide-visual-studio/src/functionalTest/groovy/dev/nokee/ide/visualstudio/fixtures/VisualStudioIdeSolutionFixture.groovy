package dev.nokee.ide.visualstudio.fixtures

import dev.gradleplugins.test.fixtures.file.TestFile
import org.gradle.util.TextUtil

class VisualStudioIdeSolutionFixture {
	final TestFile solutionFile
	final String content
	Map<String, ProjectReference> projects = [:]

	VisualStudioIdeSolutionFixture(TestFile solutionFile) {
		solutionFile = solutionFile.assertIsFile()
		assert TextUtil.convertLineSeparators(solutionFile.text, TextUtil.windowsLineSeparator) == solutionFile.text : "Solution file contains non-windows line separators"

		content = TextUtil.normaliseLineSeparators(solutionFile.text)
		content.findAll(~/(?m)^Project\(\"\{8BC9CEB8-8B4A-11D0-8D11-00A0C91BC942\}\"\) = \"(\w+)\", \"([^\"]*)\", \"\{([\w\-]+)\}\"$/, {
			projects.put(it[1], new ProjectReference(it[1], it[2], it[3]))
		})
	}

	static VisualStudioIdeSolutionFixture of(Object path) {
		if (path instanceof File) {
			path = path.absolutePath
		}
		assert path instanceof String
		if (!path.endsWith('.sln')) {
			path = path + '.sln'
		}
		return new VisualStudioIdeSolutionFixture(TestFile.of(new File(path.toString())))
	}

	void assertHasProjects(String... names) {
		assert projects.keySet() == names as Set
	}
//
//	void assertHasProject(File projectFile) {
//		assert contentFile.projectLocationPaths.contains(projectFile.absolutePath)
//	}
//
//	void assertDerivedDataLocationRelativeToWorkspace(String path) {
//		def settings = WorkspaceSettingsFile.user(dir)
//		settings.derivedDataCustomLocation.with {
//			assert it.present
//			assert it.get() == path
//		}
//		settings.derivedDataLocationStyle.with {
//			assert it.present
//			assert it.get() == 'WorkspaceRelativePath'
//		}
//	}
//
//	List<XcodeIdeProjectFixture> getProjects() {
//		return contentFile.projectLocationPaths.collect { new XcodeIdeProjectFixture(it) }
//	}

	static class ProjectReference {
		final String name
		final String file
		final String uuid

		ProjectReference(String name, String file, String uuid) {
			this.name = name
			this.file = file
			this.uuid = uuid
		}

		String getGuid() {
			return "{${uuid}}"
		}
	}
}
