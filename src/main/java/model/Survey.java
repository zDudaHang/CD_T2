package model;

import java.io.Serializable;
import java.util.List;

public class Survey implements Serializable {
	private final String title;
	private List<String> options;

	public Survey(String title, List<String> options) {
		this.title = title;
		this.options = options;
	}

	@Override public String toString() {
		String s = "Enquete: " + title + "\n";
		for (String op : options) {
			s += "[" + options.indexOf(op) + "] " + op + "\n";
		}
		return s;
	}

	public String getTitle() {
		return title;
	}

	public List<String> getOptions() {
		return options;
	}
}
