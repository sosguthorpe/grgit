package org.ajoberstar.grgit.monitoring

class GroovyConsoleMonitor implements ProgressMonitor {
  
  int task_counter
  int total_tasks
  String current_task_name
  int current_task_total
  int current_task_progress
  long start_time
  
  public static final String UPDATE_PREFIX = "\t\t"
  
  GroovyConsoleMonitor () {
    task_counter = 0
  }
  
  @Override
  public void feedbackUpdate (String text) {
	  feedback("${UPDATE_PREFIX}${text}")
  }
  
  @Override
  public void feedback (String text) {
	  println (text)
  }

  @Override
  public void beginTask (String task_name, int total_work) {
    current_task_name = task_name
    current_task_total = total_work
    current_task_progress = 0
    
    if (current_task_total > 0) task_counter ++
    
    feedback(
      (task_counter > 0 ? "${task_counter}/${total_tasks} " : "") +
      "${current_task_name}..." +
      (current_task_total > 0 ? " 0 of ${current_task_total}" : "")
    )
  }

  @Override
  public void endTask () {
    // Update console.
    feedbackUpdate("${current_task_name} Completed")
    
    if (task_counter == total_tasks) {
      // Add finished message.
      feedback("Process finished in ${((System.currentTimeMillis() - start_time)/1000)} seconds")
    }
  }

  @Override
  public boolean isCancelled () {
    // Always return false. Cannot be user cancelled.
    false
  }

  @Override
  public void start (int total_tasks) {
    start_time = System.currentTimeMillis()
    total_tasks = total_tasks
  }

  @Override
  public void update (int increment) {
    current_task_progress += increment
    feedbackUpdate("${current_task_name} ${current_task_progress}" + (current_task_total > 0 ? " of ${current_task_total}" : ""))
  }
}