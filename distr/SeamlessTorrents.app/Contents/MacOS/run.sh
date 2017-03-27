#! /bin/sh

if [ -d "$JAVA_HOME" -a -x "$JAVA_HOME/bin/java" ]; then
	JAVACMD="$JAVA_HOME/bin/java"
else
	JAVACMD=java
fi

export APP_HOME="/Applications/SeamlessTorrents.app/Contents/Resources/Java"
export ROCOCOA_HOME="$APP_HOME/lib"
export USER_DIR="/Users/$USER"

$JAVACMD -jar $APP_HOME/slt-launcher.jar --init 2>&1

if [ $? -ne 0 ]; then
    exit
fi

$JAVACMD -Xmx64m -jar $APP_HOME/slt-launcher.jar --config=$USER_DIR/.com.github.atomashpolskiy.slt/settings.yml --daemon 2>&1