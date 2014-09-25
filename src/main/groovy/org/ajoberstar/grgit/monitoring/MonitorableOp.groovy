package org.ajoberstar.grgit.monitoring
import org.eclipse.jgit.lib.NullProgressMonitor


abstract class MonitorableOp {
	
	/**
	 * Monitor instance that will be used to feedback progress for potentially
	 * time-consuming operations.
	 */
	protected ProgressMonitor monitor = new GroovyConsoleMonitor()
	
	/**
	 * Set the progress monitor to use while performing operations.
	 * @param monitor
	 */
	protected void setProgressMonitor (ProgressMonitor monitor) {
		this.monitor = monitor
	}
	
	protected feedback (String message) {
		monitor.feedback(message)
	}
}
