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
package eu.ill.puma.core.utils.throttle;

import org.joda.time.DateTime;
import org.joda.time.Period;
import org.joda.time.PeriodType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class Throttle {

	private static final Logger log = LoggerFactory.getLogger(Throttle.class);
	public static int DEFAULT_THROTTLE_TIME_MILLIS = 20000;
	private ScheduledThreadPoolExecutor scheduledThreadPoolExecutor = new ScheduledThreadPoolExecutor(1);

	private DateTime lastThrottleDate = null;
	private String name;
	private boolean isThrottling = false;

	public Throttle(String name) {
		this.name = name;
	}

	public static void setDefaultDelay(int delay) {
		DEFAULT_THROTTLE_TIME_MILLIS = delay;
	}

	public synchronized void throttle() {
		this.throttle(DEFAULT_THROTTLE_TIME_MILLIS);
	}

	// Return false if busy
	public boolean throttleOrBusy(Integer throttleTimeMillis) {
		if (!this.isThrottling) {
			this.throttle(throttleTimeMillis);

			return true;
		}
		return false;
	}

	public synchronized void throttle(Integer throttleTimeMillis) {
		this.isThrottling = true;
		int millisecondsDelay = this.getThrottleDelayMillis(throttleTimeMillis);

		if (millisecondsDelay > 0) {
			DateTime startDate = new DateTime();

			ScheduledFuture future = this.scheduledThreadPoolExecutor.schedule(new Runnable() {
				public void run() {
					// do nothing
				}
			}, millisecondsDelay, TimeUnit.MILLISECONDS);

			try {
				log.debug("Throttling (" + this.name + ")... ");
				future.get();

				DateTime endDate = new DateTime();
				log.debug("Throttled  (" + this.name + ") for " + new Period(startDate, endDate, PeriodType.millis()).getValue(0) + "ms");

			} catch (Exception e) {
				// Fail... oh well
				log.warn("Scheduled future failed");
			}
		}

		// Update last date;
		this.lastThrottleDate = new DateTime();
		this.isThrottling = false;
	}

	private int getThrottleDelayMillis(Integer throttleTimeMillis) {
		if (this.lastThrottleDate != null) {
			if (throttleTimeMillis == null) {
				throttleTimeMillis = DEFAULT_THROTTLE_TIME_MILLIS;
			}

			DateTime startDate = new DateTime();
			DateTime earliestDate = this.lastThrottleDate.plusMillis(throttleTimeMillis);

			if (earliestDate.isAfter(startDate)) {
				Period period = new Period(startDate, earliestDate, PeriodType.millis());
				int millisecondsDelay = period.getValue(0);

				return millisecondsDelay;
			}
		}

		return 0;
	}

}
