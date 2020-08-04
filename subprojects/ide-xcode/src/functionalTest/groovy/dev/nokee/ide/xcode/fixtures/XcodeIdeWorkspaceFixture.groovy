package dev.nokee.ide.xcode.fixtures

import com.dd.plist.NSDictionary
import com.dd.plist.NSObject
import com.dd.plist.PropertyListParser
import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.ide.fixtures.IdePathUtils
import dev.nokee.ide.fixtures.IdeWorkspaceFixture

import static org.apache.commons.io.FilenameUtils.getName
import static org.apache.commons.io.FilenameUtils.removeExtension

class XcodeIdeWorkspaceFixture implements IdeWorkspaceFixture {
	final TestFile dir
	final WorkspaceFile contentFile

	XcodeIdeWorkspaceFixture(TestFile workspaceLocation) {
		dir = workspaceLocation
		dir.assertIsDirectory()
		contentFile = new WorkspaceFile(workspaceLocation.file("contents.xcworkspacedata"))
	}

	static XcodeIdeWorkspaceFixture of(Object path) {
		return new XcodeIdeWorkspaceFixture(TestFile.of(new File(workspaceName(path))))
	}

	static String workspaceName(Object path) {
		return IdePathUtils.addExtensionIfAbsent(path, 'xcworkspace')
	}

	XcodeIdeWorkspaceFixture assertHasProjects(Iterable<String> paths) {
		assert contentFile.projectLocationPaths.size() == paths.size()
		assert contentFile.projectLocationPaths.collect { removeExtension(getName(it)) } as Set == paths as Set
		return this
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
