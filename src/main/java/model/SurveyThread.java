package model;

import java.util.HashMap;
import java.util.List;

import org.jgroups.JChannel;
import org.jgroups.Message;
import org.jgroups.blocks.atomic.Counter;
import org.jgroups.blocks.atomic.CounterService;

import app.Chat;
import gui.TerminalGUI;
import util.ChatUtil;

public class SurveyThread extends Thread {

	private final CounterService service;
	private final long surveyNumber;
	private final Survey survey;
	private final long sleepTime;
	private final String surveyCounters;
	private final JChannel channel;
	public SurveyThread(CounterService service, long surveyNumber, Survey survey, long sleepTimeInMinutes, String surveyCounters, JChannel channel) {
		super();
		this.service = service;
		this.surveyNumber = surveyNumber;
		this.survey = survey;
		sleepTime = sleepTimeInMinutes * 60000;
		this.surveyCounters = surveyCounters;
		this.channel = channel;
	}

	public void run() {

		try {
			sleep(sleepTime);
		} catch (InterruptedException e) {
			TerminalGUI.printLnError(e.getMessage());
			return;
		}

		HashMap<String, Long> results = new HashMap<>();

		List<String> options = survey.getOptions();

		float total = 0;

		for (int i = 0; i < options.size(); i++) {
			String name = ChatUtil.createCounterName(surveyNumber, i);
			Counter counter = service.getOrCreateCounter(name, 0);
			long votes = counter.get();
			total += votes;
			results.put(options.get(i), votes);
			service.deleteCounter(name);
		}

		Counter counter = service.getOrCreateCounter(surveyCounters, 0);
		counter.decrementAndGet();

		String result = "Resultado da enquete #" + surveyNumber + " (" + survey.getTitle() + "): \n";

		for (String key : results.keySet()) {
			float percentage = (results.get(key) / total) * 100;
			result += String.format("A opção %s recebeu %d votos (%.2f %%).\n", key, results.get(key), percentage);
		}

		try {
			channel.send(new Message(null, result));
			channel.send(new Message(null, "!Remove#" + surveyNumber));
		} catch (Exception e) {
			TerminalGUI.printLnError(e.getMessage());
		}


	}
}
