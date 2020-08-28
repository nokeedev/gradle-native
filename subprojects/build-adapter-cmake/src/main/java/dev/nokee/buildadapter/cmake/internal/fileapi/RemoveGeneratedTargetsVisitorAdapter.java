package dev.nokee.buildadapter.cmake.internal.fileapi;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * This visitor adapter removes any targets provided by the generators for IDE conveniences.
 * For the build adapter, those targets are just noise so we remove them as if they never existed.
 * It may be MSVC generator specific issue.
 */
public final class RemoveGeneratedTargetsVisitorAdapter implements CodeModelReplyFiles.Visitor {
	private final CodeModelReplyFiles.Visitor delegate;
	private CodeModelReplyFile visitedCodeModel = null;
	private List<CodeModelTargetReplyFile> visitedTargets = new ArrayList<>();
	private long targetCount = 0;
	private final Set<String> removedTargetIds = new HashSet<>();

	public RemoveGeneratedTargetsVisitorAdapter(CodeModelReplyFiles.Visitor delegate) {
		this.delegate = delegate;
	}

	@Override
	public void visit(CodeModelReplyFile codeModel) {
		visitedCodeModel = codeModel;
		targetCount = computeTargetCount(codeModel.get());
	}

	private long computeTargetCount(CodeModel codeModel) {
		return codeModel.getConfigurations().stream().flatMap(it -> it.getTargets().stream()).count();
	}

	@Override
	public void visit(CodeModelTargetReplyFile codeModelTarget) {
		visitedTargets.add(codeModelTarget);
		if (--targetCount == 0) {
			performFiltering();
			delegate.visit(visitedCodeModel);
			visitedTargets.forEach(delegate::visit);
		}
	}

	@Override
	public void visit(IndexReplyFile replyIndex) {
		delegate.visit(replyIndex);
	}

	private void performFiltering() {
		visitedTargets.stream().map(CodeModelTargetReplyFile::get).filter(CodeModelTarget::isGeneratorProvided).forEach(this::filterTarget);
	}

	private void filterTarget(CodeModelTarget target) {
		if (removedTargetIds.add(target.getId())) {
			visitedCodeModel = visitedCodeModel.withContent(visitedCodeModel.get().withConfigurations(visitedCodeModel.get().getConfigurations().stream().map(new RemoveTargetIdFromConfiguration(target.getId())).collect(Collectors.toList())));
			visitedTargets = visitedTargets.stream().filter(it -> !it.get().getId().equals(target.getId())).map(targetFile -> {
				return targetFile.withContent(new RemoveTargetIdFromDependency(target.getId()).apply(targetFile.get()));
			}).collect(Collectors.toList());
		}
	}
}
