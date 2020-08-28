package dev.nokee.buildadapter.cmake.internal.fileapi

import spock.lang.Specification
import spock.lang.Subject

@Subject(RemoveGeneratedTargetsVisitorAdapter)
class RemoveGeneratedTargetsVisitorAdapterTest extends Specification {
	def "can filter generated targets"() {
		given:
		def testSetDirectory = getReplyFilesOf('msvc2019-googletest')
		def subject = CodeModelReplyFiles.of(testSetDirectory)

		when:
		def visitedIndex = null
		def visitedCodeModel = null
		def visitedTargets = []
		subject.visit(new RemoveGeneratedTargetsVisitorAdapter(new CodeModelReplyFiles.Visitor() {
			@Override
			void visit(CodeModel codeModel) {
				visitedCodeModel = codeModel
			}

			@Override
			void visit(CodeModelTarget codeModelTarget) {
				visitedTargets << codeModelTarget
			}

			@Override
			void visit(Index replyIndex) {
				visitedIndex = replyIndex
			}
		}))

		then:
		visitedTargets.size() == 16
		visitedTargets*.name as Set == ['gtest_main', 'gtest', 'gmock_main', 'gmock'] as Set
		visitedIndex.objects.size() == 1
		visitedIndex.objects[0].version.major == 2
		visitedIndex.objects[0].version.minor == 1
	}

	private static File getReplyFilesOf(String testSetName) {
		def replyDirectory = System.getProperty('dev.nokee.cmake-replies')
		assert replyDirectory != null
		return new File(replyDirectory, testSetName)
	}
}
