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

import org.ajoberstar.grgit.auth.TransportOpUtil

import java.util.concurrent.Callable

import org.ajoberstar.grgit.Repository
import org.ajoberstar.grgit.exception.GrgitException
import org.ajoberstar.grgit.monitoring.MonitorableOp
import org.eclipse.jgit.api.PullCommand
import org.eclipse.jgit.api.PullResult
import org.eclipse.jgit.api.errors.GitAPIException

/**
 * Pulls changes from the remote on the current branch. If the changes
 * conflict, the pull will fail, leaving the working tree as-is, and
 * throwing an exception.
 *
 * <p>Pull changes.</p>
 *
 * <pre>
 * grgit.pull()
 * grgit.pull(rebase: false)
 * </pre>
 *
 * <p>Pull changes and rebase the current branch on top of the remote changes.</p>
 *
 * <pre>
 * grgit.pull(rebase: true)
 * </pre>
 *
 * See <a href="http://git-scm.com/docs/git-pull">git-pull Manual Page</a>.
 *
 * @since 0.2.0
 * @see <a href="http://git-scm.com/docs/git-pull">git-pull Manual Page</a>
 */
class PullOp extends MonitorableOp implements Callable<Void> {
	private final Repository repo

	/**
	 * Rebase on top of the changes when they are pulled in, if
	 * {@code true}. {@code false} (the default) otherwise.
	 */
	boolean rebase = false

	PullOp(Repository repo) {
		this.repo = repo
	}

	Void call() {
		PullCommand cmd = repo.jgit.pull()
		cmd.rebase = rebase
        TransportOpUtil.configure(cmd, repo.credentials)

		try {
			PullResult result = cmd.call()
			println result
			if (!result.successful) {
				throw new GrgitException("Could not pull: ${result}")
			}
			return null
		} catch (GitAPIException e) {
			throw new GrgitException('Problem merging.', e)
		}
	}
}
