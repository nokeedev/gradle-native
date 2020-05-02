package dev.nokee.ide.xcode.fixtures

import dev.gradleplugins.test.fixtures.file.TestFile

class XcodeIdeWorkspaceFixture {
	final TestFile dir
	final WorkspaceFile contentFile

	XcodeIdeWorkspaceFixture(TestFile workspaceLocation) {
		dir = workspaceLocation
		dir.assertIsDirectory()
		contentFile = new WorkspaceFile(workspaceLocation.file("contents.xcworkspacedata"))
	}

	void assertHasProjects(String... paths) {
		assert contentFile.projectLocationPaths.size() == paths.length
		assert contentFile.projectLocationPaths == paths.collect { dir.parentFile.file(it).absolutePath } as Set
	}

	void assertHasProject(File projectFile) {
		assert contentFile.projectLocationPaths.contains(projectFile.absolutePath)
	}

	List<XcodeIdeProjectFixture> getProjects() {
		return contentFile.projectLocationPaths.collect { new XcodeIdeProjectFixture(it) }
	}

	static class WorkspaceFile {
		final TestFile file
		final String name
		final Node contentXml

		WorkspaceFile(TestFile workspaceFile) {
			workspaceFile.assertIsFile()
			file = workspaceFile
			name = file.name.replace(".xcworkspacedata", "")
			contentXml = new XmlParser().parse(file)
		}

		Set<String> getProjectLocationPaths() {
			return contentXml.FileRef*.@location*.replaceAll('absolute:', '') as Set
		}
	}
}
