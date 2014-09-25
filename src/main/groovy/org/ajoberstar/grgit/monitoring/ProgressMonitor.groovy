package org.ajoberstar.grgit.monitoring;

interface ProgressMonitor extends org.eclipse.jgit.lib.ProgressMonitor {
	public void feedbackUpdate (String text)
	public void feedback (String text)
}
