package dev.nokee.ide.visualstudio.fixtures

import dev.gradleplugins.test.fixtures.file.TestFile
import dev.nokee.ide.fixtures.IdePathUtils
import org.gradle.util.TextUtil

import java.nio.channels.FileChannel
import java.nio.channels.FileLock

class VisualStudioIdeSolutionFixture {
	final TestFile solutionFile
	final String content
	Map<String, ProjectReference> projects = [:]

	VisualStudioIdeSolutionFixture(TestFile solutionFile) {
		this.solutionFile = solutionFile.assertIsFile()
//		assert TextUtil.convertLineSeparators(solutionFile.text, TextUtil.windowsLineSeparator) == solutionFile.text : "Solution file contains non-windows line separators"

		content = TextUtil.normaliseLineSeparators(solutionFile.text)
		content.findAll(~/(?m)^Project\(\"\{8BC9CEB8-8B4A-11D0-8D11-00A0C91BC942\}\"\) = \"([\w\-]+)\", \"([^\"]*)\", \"\{([\w\-]+)\}\"$/, {
			projects.put(it[1], new ProjectReference(it[1], it[2], it[3]))
		})
	}

	static VisualStudioIdeSolutionFixture of(Object path) {
		return new VisualStudioIdeSolutionFixture(TestFile.of(new File(solutionName(path))))
	}

	static String solutionName(Object path) {
		return IdePathUtils.addExtensionIfAbsent(path, 'sln')
	}

	void assertHasProjects(String... names) {
		assert projects.keySet() == names as Set
	}

	void assertHasProjects(Iterable<String> names) {
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

	//region .vs Directory
	DotvsDirectory getDotvsDirectory() {
		return new DotvsDirectory(solutionFile.parentFile.createDirectory('.vs'))
	}

	static class DotvsDirectory {
		private final TestFile directory
		private final TestFile fileToLock

		DotvsDirectory(TestFile directory) {
			this.directory = directory
			directory.createFile('foo')
			directory.createFile('bar')
			this.fileToLock = directory.createFile('file-to-lock')
		}

		void assertExists() {
			directory.assertHasDescendants('foo', 'bar', 'file-to-lock')
		}

		void assertDoesNotExist() {
			directory.assertDoesNotExist()
		}

		Lock simulateVisualStudioIdeLock() {
			def inStream = null
			def fileChannel = null
			def lock = null
			try {
				inStream = new RandomAccessFile(fileToLock, 'rw')
				fileChannel = inStream.getChannel()
				lock = fileChannel.lock()
				return new Lock(inStream, fileChannel, lock)
			} catch (Throwable ex) {
				lock?.close()
				fileChannel?.close()
				inStream?.close()
				throw ex
			}
		}

		static class Lock implements AutoCloseable {
			private final RandomAccessFile inStream
			private final FileChannel fileChannel
			private final FileLock lock

			Lock(RandomAccessFile inStream, FileChannel fileChannel, FileLock lock) {
				this.inStream = inStream
				this.fileChannel = fileChannel
				this.lock = lock
			}

			@Override
			void close() {
				lock.close()
				fileChannel.close()
				inStream.close()
			}
		}
	}
	//endregion
}
