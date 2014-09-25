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
package org.ajoberstar.grgit.service

import org.ajoberstar.grgit.Branch
import org.ajoberstar.grgit.Commit
import org.ajoberstar.grgit.Repository
import org.ajoberstar.grgit.Tag
import org.ajoberstar.grgit.util.JGitUtil

/**
 * Convenience methods to resolve various objects.
 * @since 0.2.2
 */
class ResolveService {
	private final Repository repository

	ResolveService(Repository repository) {
		this.repository = repository
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

	/**
	 * Resolves a commit from the given object. Can handle any of the following
	 * types:
	 *
	 * <ul>
	 *   <li>{@link org.ajoberstar.grgit.Commit}</li>
	 *   <li>{@link org.ajoberstar.grgit.Tag}</li>
	 *   <li>{@link org.ajoberstar.grgit.Branch}</li>
	 *   <li>{@link String}</li>
	 * </ul>
	 *
	 * <p>
	 * String arguments can be in the format of any
	 * <a href="http://git-scm.com/docs/gitrevisions.html">Git revision string</a>.
	 * </p>
	 * @param object the object to resolve
	 * @return the corresponding commit
	 */
	Commit toCommit(Object object) {
		if (object instanceof Commit) {
			return object
		} else if (object instanceof Tag) {
			return object.commit
		} else if (object instanceof Branch) {
			return JGitUtil.resolveCommit(repository, object.fullName)
		} else if (object instanceof String) {
			return JGitUtil.resolveCommit(repository, object)
		} else {
			throwIllegalArgument(object)
		}
	}

	/**
	 * Resolves a branch from the given object. Can handle any of the following
	 * types:
	 * <ul>
	 *   <li>{@link String}</li>
	 * </ul>
	 * @param object the object to resolve
	 * @return the corresponding commit
	 */
	Branch toBranch(Object object) {
		if (object instanceof String) {
			JGitUtil.resolveBranch(repository, object)
		} else {
			throwIllegalArgument(object)
		}
	}

	/**
	 * Resolves a tag from the given object. Can handle any of the following
	 * types:
	 * <ul>
	 *   <li>{@link String}</li>
	 * </ul>
	 * @param object the object to resolve
	 * @return the corresponding commit
	 */
	Tag toTag(Object object) {
		if (object instanceof String) {
			JGitUtil.resolveTag(repository, object)
		} else {
			throwIllegalArgument(object)
		}
	}

	/**
	 * Resolves a revision string that corresponds to the given object. Can
	 * handle any of the following types:
	 * <ul>
	 *   <li>{@link org.ajoberstar.grgit.Commit}</li>
	 *   <li>{@link org.ajoberstar.grgit.Tag}</li>
	 *   <li>{@link org.ajoberstar.grgit.Branch}</li>
	 *   <li>{@link String}</li>
	 * </ul>
	 * @param object the object to resolve
	 * @return the corresponding commit
	 */
	String toRevisionString(Object object) {
		if (object instanceof Commit) {
			return object.id
		} else if (object instanceof Tag) {
			return object.fullName
		} else if (object instanceof Branch) {
			return object.fullName
		} else if (object instanceof String) {
			return object
		} else {
			throwIllegalArgument(object)
		}
	}

	private void throwIllegalArgument(Object object) {
		throw new IllegalArgumentException("Can't handle the following object (${object}) of class (${object.class})")
	}
}
