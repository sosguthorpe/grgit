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

import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Repository
import org.ajoberstar.grgit.exception.GrgitException
import org.ajoberstar.grgit.monitoring.MonitorableOp
import org.ajoberstar.grgit.util.JGitUtil
import org.eclipse.jgit.api.RevertCommand
import org.eclipse.jgit.api.errors.GitAPIException
import org.eclipse.jgit.revwalk.RevCommit

/**
 * Revert one or more commits. Returns the new HEAD {@link Commit}.
 *
 * <p>Revert one or more commits.</p>
 *
 * <pre>
 * grgit.revert(commits: ['1234567', '1234568'])
 * </pre>
 *
 * See <a href="http://git-scm.com/docs/git-revert">git-revert Manual Page</a>.
 *
 * @since 0.1.0
 * @see <a href="http://git-scm.com/docs/git-revert">git-revert Manual Page</a>
 */
class RevertOp extends MonitorableOp implements Callable<Commit> {
	private final Repository repo

	/**
	 * List of commits to revert.
	 */
	List<Object> commits = []

	RevertOp(Repository repo) {
		this.repo = repo
	}

	Commit call() {
		RevertCommand cmd = repo.jgit.revert()
		commits.each {
			cmd.include(JGitUtil.resolveObject(repo, it))
		}
		try {
			RevCommit commit = cmd.call()
			if (commit == null) {
				throw new GrgitException('Problem reverting commits.')
			}
			return JGitUtil.convertCommit(commit)
		} catch (GitAPIException e) {
			throw new GrgitException('Problem reverting commits.', e)
		}
	}
}
