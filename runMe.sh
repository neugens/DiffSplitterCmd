#!/bin/sh
rm -rf bin
mkdir bin
javac -d bin src/com/redhat/java/tools/diff/DiffSplitter.java
java -cp bin com.redhat.java.tools.diff.DiffSplitter $@
