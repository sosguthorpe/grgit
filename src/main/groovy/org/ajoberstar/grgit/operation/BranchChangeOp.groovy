/*
 * Copyright 2012-2014 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.ajoberstar.grgit.operation

import java.util.concurrent.Callable

import org.ajoberstar.grgit.Branch
import org.ajoberstar.grgit.Repository
import org.ajoberstar.grgit.exception.GrgitException
import org.ajoberstar.grgit.monitoring.MonitorableOp
import org.ajoberstar.grgit.util.JGitUtil
import org.eclipse.jgit.api.CreateBranchCommand
import org.eclipse.jgit.api.CreateBranchCommand.SetupUpstreamMode
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.lib.Ref

/**
 * Changes a branch's start point and/or upstream branch. Returns the changed {@link Branch}.
 *
 * <p>To change the branch to start at, but not track, a local start point.</p>
 *
 * <pre>
 * grgit.branch.change(name: 'existing-branch', startPoint: 'local-branch')
 * grgit.branch.change(name: 'existing-branch', startPoint: 'local-branch', mode: BranchChangeOp.Mode.NO_TRACK)
 * </pre>
 *
 * <p>To change the branch to start at and track a local start point.</p>
 *
 * <pre>
 * grgit.branch.change(name: 'existing-branch', startPoint: 'local-branch', mode: BranchChangeOp.Mode.TRACK)
 * </pre>
 *
 * <p>To change the branch to start from and track a remote start point.</p>
 *
 * <pre>
 * grgit.branch.change(name: 'existing-branch', startPoint: 'origin/remote-branch')
 * grgit.branch.change(name: 'existing-branch', startPoint: 'origin/remote-branch', mode: BranchChangeOp.Mode.TRACK)
 * </pre>
 *
 * <p>To change the branch to start from, but not track, a remote start point.</p>
 *
 * <pre>
 * grgit.branch.change(name: 'existing-branch', startPoint: 'origin/remote-branch', mode: BranchChangeOp.Mode.NO_TRACK)
 * </pre>
 *
 * See <a href="http://git-scm.com/docs/git-branch">git-branch Manual Page</a>.
 *
 * @since 0.2.0
 * @see <a href="http://git-scm.com/docs/git-branch">git-branch Manual Page</a>
 */
class BranchChangeOp extends MonitorableOp implements Callable<Branch> {
	private final Repository repo

	/**
	 * The name of the branch to change.
	 */
	String name

	/**
	 * The commit the branch should now start at.
	 */
	String startPoint

	/**
	 * The tracking mode to use.
	 */
	Mode mode

	BranchChangeOp(Repository repo) {
		this.repo = repo
	}

	Branch call() {
		if (!JGitUtil.resolveBranch(repo, name)) {
			throw new GrgitException("Branch does not exist: ${name}")
		}
		if (!startPoint) {
			throw new GrgitException('Must set new startPoint.')
		}
		CreateBranchCommand cmd = repo.jgit.branchCreate()
		cmd.name = name
		cmd.force = true
		if (startPoint) { cmd.startPoint = startPoint }
		if (mode) { cmd.upstreamMode = mode.jgit }

		try {
			Ref ref = cmd.call()
			return JGitUtil.resolveBranch(repo, ref)
		} catch (GitAPIException e) {
			throw new GrgitException('Problem changing branch.', e)
		}
	}

	static enum Mode {
		TRACK(SetupUpstreamMode.TRACK),
		NO_TRACK(SetupUpstreamMode.NOTRACK)

		private final SetupUpstreamMode jgit

		Mode(SetupUpstreamMode jgit) {
			this.jgit = jgit
		}
	}
}
