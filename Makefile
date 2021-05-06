clean:
	mvn clean -q

compile: clean
	mvn install -q

run: compile
	mvn exec:java -q

