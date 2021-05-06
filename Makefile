clean:
	mvn clean -q

compile: clean
	mvn install -q

run: compile
	mvn exec:java -Dexec.args="$(survey_time_out)" -q

