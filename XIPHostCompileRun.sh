#! /usr/bin/env bash
BASEDIR=$(dirname $0)
echo $BASEDIR
ant clean all run -buildfile $BASEDIR/build.xml
read -p "Press any key to continue..."