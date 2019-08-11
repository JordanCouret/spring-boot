package com.example.demo;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import lombok.AllArgsConstructor;
import org.quartz.JobBuilder;
import org.quartz.JobDataMap;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.quartz.SimpleScheduleBuilder;
import org.quartz.SimpleTrigger;
import org.quartz.TriggerBuilder;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import static org.quartz.SimpleScheduleBuilder.simpleSchedule;

@Component
@AllArgsConstructor
public class DataGenerator implements ApplicationRunner {

	private final Scheduler scheduler;

	private final AtomicInteger BASIC = new AtomicInteger(0);

	@Override
	public void run(ApplicationArguments args) throws Exception {
		cronJob();
		dynamicJob("SHORT", 15, 10000);
		dynamicJob("MEDIUM", 600, 10000);
		dynamicJob("LONG", 3600, 10000);
	}

	private void dynamicJob(String name, int interval, int nb) throws SchedulerException {
		for (int i = 0; i < nb; i++) {
			JobDataMap map = new JobDataMap();
			map.put("DATA", "DATA");
			String id = String.valueOf(BASIC.incrementAndGet());
			JobDetail jobDetail = JobBuilder.newJob(DynamicJob.class)
					.withIdentity(id, "J-".concat(name))
					.usingJobData(map)
					.build();

			java.util.Date start = Date.from(LocalDateTime.now()
					.plusSeconds(interval * i)
					.atZone(ZoneId.systemDefault())
					.toInstant());

			SimpleTrigger sampleTrigger = TriggerBuilder.newTrigger()
					.forJob(jobDetail)
					.withIdentity(id, "T-".concat(name))
					.withSchedule(simpleSchedule())
					.startAt(start)
					.build();

			scheduler.scheduleJob(jobDetail, sampleTrigger);
		}
	}

	private void cronJob() throws SchedulerException {
		String name = String.valueOf(BASIC.incrementAndGet());
		JobDetail jobDetail = JobBuilder.newJob(CronJob.class)
				.withIdentity(name, "J-CRON")
				.usingJobData("name", "Cron")
				.build();
		SimpleScheduleBuilder scheduleBuilder = simpleSchedule()
				.withIntervalInMinutes(1)
				.repeatForever();

		SimpleTrigger sampleTrigger = TriggerBuilder.newTrigger()
				.forJob(jobDetail)
				.withIdentity(name,"T-CRON")
				.withSchedule(scheduleBuilder)
				.build();

		scheduler.scheduleJob(jobDetail, sampleTrigger);
	}
}
