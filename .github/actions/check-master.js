const { Octokit } = require("@octokit/action");

const octokit = new Octokit();
const [owner, repo] = process.env.GITHUB_REPOSITORY.split("/");

(async function() {
	const { data } = await octokit.request("POST /repos/{owner}/{repo}/actions/workflows/{workflow_id}/runs?branch=master&per_page=1&event=push", {
		owner,
		repo,
		workflow_id: 1831037,
	});

	if (data.workflow_runs[0].conclusion == 'failure') {
		process.exit(1)
	}
}());
