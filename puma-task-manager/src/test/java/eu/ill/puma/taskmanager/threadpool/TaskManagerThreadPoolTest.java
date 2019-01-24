/*
 * Copyright 2019 Institut Laue–Langevin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package eu.ill.puma.taskmanager.threadpool;

import eu.ill.puma.taskmanager.TaskManager;
import eu.ill.puma.taskmanager.TaskPriority;
import eu.ill.puma.taskmanager.TaskState;
import eu.ill.puma.taskmanager.threadpool.tasks.*;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestExecutionListeners;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.springframework.test.context.support.DependencyInjectionTestExecutionListener;
import org.springframework.test.context.support.DirtiesContextTestExecutionListener;

import java.util.ArrayList;
import java.util.List;

@ActiveProfiles("test")
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration( locations={
		"classpath:/applicationContext-test.xml"
})
@TestExecutionListeners( {
		DependencyInjectionTestExecutionListener.class,
		DirtiesContextTestExecutionListener.class})
public class TaskManagerThreadPoolTest {

	private static final Logger log = LoggerFactory.getLogger(TaskManagerThreadPoolTest.class);

	@Autowired
	private TaskManager taskManager;

	private void runMultiTasks(List<Long> results) throws Exception {
		long index1 = 5;
		long index2 = 2;

		SleepingWithNextTestTask testTask1 = new SleepingWithNextTestTask(index1, (long data) -> {
			results.add(data);
		});
		SleepingWithNextTestTask testTask2 = new SleepingWithNextTestTask(index2, (long data) -> {
			results.add(data);
		});

		log.info("Before running test");
		taskManager.executeTask(testTask1);
		taskManager.executeTask(testTask2);


		// Test we have the correct return values
		Assert.assertEquals(index1, testTask1.get().longValue());
		Assert.assertEquals(index2, testTask2.get().longValue());

		// Wait for all chained tasks to complete
		while (testTask1.getNextTask() != null) {
			index1--;
			testTask1 = (SleepingWithNextTestTask)testTask1.getNextTask();
			Assert.assertEquals(index1, testTask1.get().longValue());
		}

		log.info("After running test");
	}

	@Test
	public void testParallelThreads() throws Exception {
		this.taskManager.reset(2);

		List<Long> results = new ArrayList<>();
		this.runMultiTasks(results);

		long[] expected ={2,1,5,4,3,2,1};
		Assert.assertArrayEquals(expected, results.stream().mapToLong(l -> l).toArray());
	}

	@Test
	public void testSingleThread() throws Exception {
		this.taskManager.reset(1);

		List<Long> results = new ArrayList<>();
		this.runMultiTasks(results);

		long[] expected ={5,2,4,1,3,2,1};
		Assert.assertArrayEquals(expected, results.stream().mapToLong(l -> l).toArray());
	}

	@Test
	public void testFailingTask() throws Exception {
		this.taskManager.reset();

		FailingTestTask failingTask = new FailingTestTask();
		this.taskManager.executeTask(failingTask);

		// Synchronise
		Exception caughtException = null;
		try {
			failingTask.get();

		} catch (Exception e) {
			caughtException = e;
		}

		Assert.assertNotNull(caughtException);
		Assert.assertEquals(TaskState.FAILED, failingTask.getState());
	}

	@Test
	public void testRepeatingTask() throws Exception {
		this.taskManager.reset();

		long numberOfRuns = 10240;
		long numberOfRunsDone = 0;
		RepeatingTestTask task = new RepeatingTestTask(numberOfRuns);

		this.taskManager.executeTask(task);
		do {
			try {
				numberOfRunsDone = task.get();
			} catch (Exception e) {
				System.err.println(e);
			}
		} while (task.doRepeatRun());
		numberOfRunsDone = task.get();

		Assert.assertEquals(numberOfRuns, numberOfRunsDone);
	}

	@Test
	public void testTaskDispatcherData() throws Exception {
		this.taskManager.reset(2);
		Assert.assertEquals(2, this.taskManager.getPoolSize());

		SleepingTestTask task1 = new SleepingTestTask(4);
		SleepingTestTask task2 = new SleepingTestTask(4);
		SleepingTestTask task3 = new SleepingTestTask(2);

		this.taskManager.executeTask(task1);
		Thread.sleep(1000);
		Assert.assertEquals(1, this.taskManager.getActiveNumberOfThreads());

		this.taskManager.executeTask(task2);
		Thread.sleep(1000);
		Assert.assertEquals(2, this.taskManager.getActiveNumberOfThreads());
		Assert.assertEquals(2, this.taskManager.getCurrentNumberOfThreads());

		this.taskManager.executeTask(task3);
		Thread.sleep(1000);
		Assert.assertEquals(1, this.taskManager.getNumberOfPendingTasks());

		// Wait for termination
		task1.get();
		task2.get();
		task3.get();

		// Small sleep to ensure active threads are all removed (fixes test failure on CI machine)
		Thread.sleep(1000);

		Assert.assertEquals(2, this.taskManager.getCurrentNumberOfThreads());
		Assert.assertEquals(0, this.taskManager.getActiveNumberOfThreads());
	}

	@Test
	public void testShutdown() throws Exception {
		this.taskManager.reset(2);
		Assert.assertEquals(2, this.taskManager.getPoolSize());

		SleepingTestTask task1 = new SleepingTestTask(4);
		SleepingTestTask task2 = new SleepingTestTask(4);
		SleepingTestTask task3 = new SleepingTestTask(2);

		// Run tasks: 1 and 2 should run, 3 put into queue
		this.taskManager.executeTask(task1);
		this.taskManager.executeTask(task2);
		this.taskManager.executeTask(task3);

		// Shutdown after 1 second : should fail 1 and 2 and interrupt 3
		new Thread(new ShutdownRunnable(this.taskManager)).start();

		// Verify that we do not block
		Exception task1Exception = null;
		Exception task2Exception = null;
		try {
			task1.get();
		} catch (Exception e) {
			task1Exception = e;
		}

		try {
			task2.get();
		} catch (Exception e) {
			task2Exception = e;
		}

		task3.get();

		Assert.assertNotNull(task1Exception);
		Assert.assertNotNull(task2Exception);
		Assert.assertNotNull(task2.getCaughtException());
		Assert.assertNull(task3.getCaughtException());

		Assert.assertEquals(TaskState.FAILED, task1.getState());
		Assert.assertEquals(TaskState.FAILED, task2.getState());
		Assert.assertEquals(TaskState.INTERRUPTED, task3.getState());
		Assert.assertEquals(0, this.taskManager.getNumberOfPendingTasks());
		Assert.assertEquals(0, this.taskManager.getActiveNumberOfThreads());
	}


	@Test
	public void verifyPriorities() throws Exception {
		this.taskManager.reset(1);
		List<Long> results = new ArrayList<>();

		// Create low priority test
		SleepingWithCallbackTask lastDefaultPriorityTask = null;
		SleepingWithCallbackTask lastHighPriorityTask = null;
		for (int i = 0; i < 100; i++) {
			SleepingWithCallbackTask task = new SleepingWithCallbackTask(i, (long data) -> {
				results.add(data);
			});
			task.setPriority(TaskPriority.DEFAULT);
			lastDefaultPriorityTask = task;
			taskManager.executeTask(task);
		}

		// create high priority
		for (int i = 100; i < 200; i++) {
			SleepingWithCallbackTask task = new SleepingWithCallbackTask(i, (long data) -> {
				results.add(data);
			});
			task.setPriority(TaskPriority.HIGH);
			lastHighPriorityTask = task;
			taskManager.executeTask(task);
		}

		// Synchronise
		lastHighPriorityTask.get();
		lastDefaultPriorityTask.get();

		// verify that high priorities aren't last to run
		Assert.assertEquals(99l, results.get(results.size() - 1).longValue());
	}

	private class ShutdownRunnable implements Runnable {

		private TaskManager taskManager;

		public ShutdownRunnable(TaskManager taskManager) {
			this.taskManager = taskManager;
		}

		@Override
		public void run() {
			try {
				Thread.sleep(2000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			this.taskManager.shutdown();
		}
	}

}
