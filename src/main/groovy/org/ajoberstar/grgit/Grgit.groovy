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
package org.ajoberstar.grgit

import org.ajoberstar.grgit.operation.*
import org.ajoberstar.grgit.service.*
import org.ajoberstar.grgit.util.JGitUtil
import org.ajoberstar.grgit.util.OpSyntaxUtil

import org.eclipse.jgit.api.Git

/**
 * Provides support for performaing operations on and getting information about
 * a Git repository.
 *
 * <p>A Grgit instance can be obtained via 3 methods.</p>
 *
 * <ul>
 *   <li>
 *     <p>{@link #open(String, Credentials) Open} an existing repository.</p>
 *     <pre>def grgit = Grgit.open('path/to/my/repo')</pre>
 *   </li>
 *   <li>
 *     <p>{@link org.ajoberstar.grgit.operation.InitOp Initialize} a new repository.</p>
 *     <pre>def grgit = Grgit.init(dir: new File('path/to/my/repo'))</pre>
 *   </li>
 *   <li>
 *     <p>{@link org.ajoberstar.grgit.operation.CloneOp Clone} an existing repository.</p>
 *     <pre>def grgit = Grgit.clone(dir: new File('path/to/my/repo'), uri: 'git@github.com:ajoberstar/grgit.git')</pre>
 *   </li>
 * </ul>
 *
 * <p>
 *   Once obtained, operations can be called with two syntaxes.
 * </p>
 *
 * <ul>
 *   <li>
 *     <p>Map syntax. Any public property on the {@code *Op} class can be provided as a Map entry.</p>
 *     <pre>grgit.commit(message: 'Committing my code.', amend: true)</pre>
 *   </li>
 *   <li>
 *     <p>Closure syntax. Any public property or method on the {@code *Op} class can be used.</p>
 *     <pre>
 * grgit.log {
 *   range 'master', 'my-new-branch'
 *   maxCommits = 5
 * }
 *     </pre>
 *   </li>
 * </ul>
 *
 * <p>
 *   Details of each operation's properties and methods are available on the
 *   doc page for the class. The following operations are supported directly on a
 *   Grgit instance.
 * </p>
 *
 * <ul>
 *   <li>{@link org.ajoberstar.grgit.operation.AddOp add}</li>
 *   <li>{@link org.ajoberstar.grgit.operation.ApplyOp apply}</li>
 *   <li>{@link org.ajoberstar.grgit.operation.CheckoutOp checkout}</li>
 *   <li>{@link org.ajoberstar.grgit.operation.CleanOp clean}</li>
 *   <li>{@link org.ajoberstar.grgit.operation.CommitOp commit}</li>
 *   <li>{@link org.ajoberstar.grgit.operation.FetchOp fetch}</li>
 *   <li>{@link org.ajoberstar.grgit.operation.LogOp log}</li>
 *   <li>{@link org.ajoberstar.grgit.operation.MergeOp merge}</li>
 *   <li>{@link org.ajoberstar.grgit.operation.PullOp pull}</li>
 *   <li>{@link org.ajoberstar.grgit.operation.PushOp push}</li>
 *   <li>{@link org.ajoberstar.grgit.operation.RmOp remove}</li>
 *   <li>{@link org.ajoberstar.grgit.operation.ResetOp reset}</li>
 *   <li>{@link org.ajoberstar.grgit.operation.RevertOp revert}</li>
 * </ul>
 *
 * <p>
 *   And the following operations are supported statically on the Grgit class.
 * </p>
 *
 * <ul>
 *   <li>{@link org.ajoberstar.grgit.operation.CloneOp clone}</li>
 *   <li>{@link org.ajoberstar.grgit.operation.InitOp init}</li>
 * </ul>
 *
 * @since 0.1.0
 */
class Grgit {
	private static final Map STATIC_OPERATIONS = [init: InitOp, clone: CloneOp]
	private static final Map OPERATIONS = [
		clean: CleanOp,
		status: StatusOp, add: AddOp, remove: RmOp, reset: ResetOp, apply: ApplyOp,
		pull: PullOp, push: PushOp, fetch: FetchOp,
		checkout: CheckoutOp,
		log: LogOp, commit: CommitOp, revert: RevertOp/*,
		cherryPick: CherryPickOp*/, merge: MergeOp/*, rebase: RebaseOp*/].asImmutable()

	/**
	 * The repository opened by this object.
	 */
	final Repository repository

	/**
	 * Supports operations on branches.
	 */
	final BranchService branch

	// final NoteService note

	// final RemoteService remote

	// final StashService stash

	/**
	 * Convenience methods for resolving various objects.
	 */
	final ResolveService resolve

	/**
	 * Supports operations on tags.
	 */
	final TagService tag

	private Grgit(Repository repository) {
		this.repository = repository
		this.branch = new BranchService(repository)
		// this.note = null
		// this.remote = null
		// this.stash = null
		this.tag = new TagService(repository)
		this.resolve = new ResolveService(repository)
	}

	/**
	 * Returns the commit located at the current HEAD of the repository.
	 * @return the current HEAD commit
	 */
	Commit head() {
		return resolve.toCommit('HEAD')
	}

	/**
	 * Checks if {@code base} is an ancestor of {@code tip}.
	 * @param base the version that might be an ancestor
	 * @param tip the tip version
	 * @since 0.2.2
	 */
	boolean isAncestorOf(Object base, Object tip) {
		Commit baseCommit = resolve.toCommit(base)
		Commit tipCommit = resolve.toCommit(tip)
		return JGitUtil.isAncestorOf(repository, baseCommit, tipCommit)
	}

	/**
	 * Resolves the given revision string to a commit given the current
	 * state of the repository. Any
	 * <a href="http://git-scm.com/docs/gitrevisions.html">Git revision string</a>
	 * should be supported.
	 * @param revstr a revision string representing the desired commit
	 * @return the commit represented by {@code revstr}
	 * @throws GrgitException if there was a problem finding the commit
	 * @deprecated replaced by {@link org.ajoberstar.grgit.service.ResolveService#toCommit(Object)}
	 * @see <a href="http://git-scm.com/docs/gitrevisions.html">gitrevisions Manual Page</a>
	 */
	@Deprecated
	Commit resolveCommit(String revstr) {
		return resolve.toCommit(revstr)
	}

	/**
	 * Release underlying resources used by this instance. After calling close
	 * you should not use this instance anymore.
	 */
	void close() {
		repository.jgit.close()
	}

	def methodMissing(String name, args) {
		OpSyntaxUtil.tryOp(this.class, OPERATIONS, [repository] as Object[], name, args)
	}

	static {
		Grgit.metaClass.static.methodMissing = { name, args ->
			OpSyntaxUtil.tryOp(Grgit, STATIC_OPERATIONS, [] as Object[], name, args)
		}
	}

	/**
	 * Opens a {@code Grgit} instance in {@code rootDirPath}. If credentials
	 * are provided they will be used for all operations with using remotes.
	 * @param rootDirPath path to the repository's root directory
	 * @param creds harcoded credentials to use for remote operations
	 * @throws GrgitException if an existing Git repository can't be opened
	 * in {@code rootDirPath}
	 */
	static Grgit open(String rootDirPath, Credentials creds = null) {
		return open(new File(rootDirPath), creds)
	}

	/**
	 * Opens a {@code Grgit} instance in {@code rootDir}. If credentials
	 * are provided they will be used for all operations with using remotes.
	 * @param rootDir path to the repository's root directory
	 * @param creds harcoded credentials to use for remote operations
	 * @throws GrgitException if an existing Git repository can't be opened
	 * in {@code rootDir}
	 */
	static Grgit open(File rootDir, Credentials creds = null) {
		def repo = new Repository(rootDir, Git.open(rootDir), creds)
		return new Grgit(repo)
	}
}
