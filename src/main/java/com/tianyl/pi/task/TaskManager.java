package com.tianyl.pi.task;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import com.tianyl.pi.log.LogManager;

public class TaskManager {

	private List<Task> tasks = new ArrayList<Task>();

	public void addTask(Task task) {
		tasks.add(task);
	}

	public void start() {
		for (final Task task : tasks) {
			Timer timer = new Timer();
			timer.schedule(new TimerTask() {
				@Override
				public void run() {
					Thread.currentThread().setName("thread:" + task.getName());
					LogManager.log("start task:" + task.getName());
					try {
						task.run();
					} catch (Exception e) {
						LogManager.log(e);
					}
					LogManager.log("end task:" + task.getName());
				}
			}, 1000, 1000 * task.getPeriod());
		}
	}

}
