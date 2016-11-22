
rkccrack : dist/rkccrack.jar

dist/rkccrack.jar : classes/rkccrack/CodeCracker.class classes/rkccrack/RKCCrack.class classes/rkccrack/StreamCrack.class Manifest
	mkdir -p dist
	jar cfm dist/rkccrack.jar Manifest -C classes rkccrack res
	
classes/rkccrack/CodeCracker.class classes/rkccrack/RKCCrack.class classes/rkccrack/StreamCrack.class : src/rkccrack/CodeCracker.java src/rkccrack/RKCCrack.java src/rkccrack/StreamCrack.java
	mkdir -p classes
	javac -d classes src/rkccrack/CodeCracker.java src/rkccrack/RKCCrack.java src/rkccrack/StreamCrack.java


clean :
	rm -rf dist classes