package com.example.demo;

import lombok.extern.slf4j.Slf4j;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import static java.time.LocalTime.now;

@Slf4j
public class CronJob implements Job {

	@Override
	public void execute(JobExecutionContext context) throws JobExecutionException {
		log.info("[CronJob] : Execution = {}", now().toString());
	}
}
