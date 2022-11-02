const { Octokit } = require("@octokit/action");

const octokit = new Octokit();
const [owner, repo] = process.env.GITHUB_REPOSITORY.split("/");

(async function() {
	const { data } = await octokit.request("GET /repos/{owner}/{repo}/actions/workflows/{workflow_id}/runs?branch=master&per_page=20&event=push", {
		owner,
		repo,
		workflow_id: 1831037,
	});

	data.workflow_runs.forEach(run => {
		if (run.conclusion == 'failure') {
			console.log("Branch 'master' is currently failing! Please fix it...");
			process.exit(-1)
		} else if (run.conclusion == 'success') {
			process.exit(0)
		}
	})
}());
