/*
 * Copyright 2012-2019 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.boot.actuate.scheduling;

import java.util.List;

import org.quartz.Scheduler;
import org.quartz.SchedulerException;

import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.ReadOperation;
import org.springframework.util.Assert;

/**
 * Web {@link Endpoint} that provides access to {@link Scheduler} group.
 *
 * @author Jordan Couret
 * @since 2.2.0
 */
@Endpoint(id = "quartzgroup")
public class QuartzGroupEndpoint {

	private final Scheduler scheduler;

	public QuartzGroupEndpoint(Scheduler scheduler) {
		Assert.notNull(scheduler, "Scheduler must not be null");
		this.scheduler = scheduler;
	}

	@ReadOperation
	public GroupDescriptor findAllGroup() throws SchedulerException {
		List<String> jobGroupNames = this.scheduler.getJobGroupNames();
		List<String> triggerGroupNames = this.scheduler.getTriggerGroupNames();
		return new GroupDescriptor(jobGroupNames, triggerGroupNames);
	}

	public static final class GroupDescriptor {

		private final List<String> jobGroups;

		private final List<String> triggerGroups;

		private GroupDescriptor(List<String> jobGroups, List<String> triggerGroups) {
			this.jobGroups = jobGroups;
			this.triggerGroups = triggerGroups;
		}

		public List<String> getJobGroups() {
			return this.jobGroups;
		}

		public List<String> getTriggerGroups() {
			return this.triggerGroups;
		}

	}

}
