package dev.nokee.ide.xcode.fixtures

import com.dd.plist.NSDictionary
import com.dd.plist.NSObject
import com.dd.plist.PropertyListParser
import dev.gradleplugins.test.fixtures.file.TestFile

class XcodeIdeWorkspaceFixture {
	final TestFile dir
	final WorkspaceFile contentFile

	XcodeIdeWorkspaceFixture(TestFile workspaceLocation) {
		dir = workspaceLocation
		dir.assertIsDirectory()
		contentFile = new WorkspaceFile(workspaceLocation.file("contents.xcworkspacedata"))
	}

	static XcodeIdeWorkspaceFixture of(Object path) {
		if (path instanceof File) {
			path = path.absolutePath
		}
		assert path instanceof String
		if (!path.endsWith('.xcworkspace')) {
			path = path + '.xcworkspace'
		}
		return new XcodeIdeWorkspaceFixture(TestFile.of(new File(path.toString())))
	}

	void assertHasProjects(String... paths) {
		assert contentFile.projectLocationPaths.size() == paths.length
		assert contentFile.projectLocationPaths == paths.collect { dir.parentFile.file(it).absolutePath } as Set
	}

	void assertHasProject(File projectFile) {
		assert contentFile.projectLocationPaths.contains(projectFile.absolutePath)
	}

	void assertDerivedDataLocationRelativeToWorkspace(String path) {
		def settings = WorkspaceSettingsFile.user(dir)
		settings.derivedDataCustomLocation.with {
			assert it.present
			assert it.get() == path
		}
		settings.derivedDataLocationStyle.with {
			assert it.present
			assert it.get() == 'WorkspaceRelativePath'
		}
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

	static class WorkspaceSettingsFile {
		final TestFile file
		final NSDictionary content

		WorkspaceSettingsFile(TestFile workspaceSettingsFile) {
			workspaceSettingsFile.assertIsFile()
			file = workspaceSettingsFile
			content = (NSDictionary)PropertyListParser.parse(file)
		}

		Optional<String> getDerivedDataCustomLocation() {
			return Optional.ofNullable(content.get('DerivedDataCustomLocation')).map { asString(it) }
		}

		Optional<String> getDerivedDataLocationStyle() {
			return Optional.ofNullable(content.get('DerivedDataLocationStyle')).map { asString(it) }
		}

		private static String asString(NSObject obj) {
			return obj.toJavaObject(String)
		}

		static WorkspaceSettingsFile shared(TestFile workspaceLocation) {
			return new WorkspaceSettingsFile(workspaceLocation.file('xcshareddata/WorkspaceSettings.xcsettings'))
		}

		static WorkspaceSettingsFile user(TestFile workspaceLocation) {
			return new WorkspaceSettingsFile(workspaceLocation.file("xcuserdata/${System.getProperty('user.name')}.xcuserdatad/WorkspaceSettings.xcsettings"))
		}
	}
}
