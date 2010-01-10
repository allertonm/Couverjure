#!/bin/bash
MACOS=`dirname $0`
LIB=$MACOS/../Resources/lib
echo $MACOS
echo $LIB
export CLASSPATH=$LIB/clojure.jar:$LIB/couverjure.jar:$LIB/jna.jar:$LIB/basiccocoa.jar
java clojure.main @/couverjure/examples/basiccocoa.clj