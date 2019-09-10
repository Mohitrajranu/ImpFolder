#!/bin/bash
#############################################################
#                                                           #
#       Tomcat Process Check                                #
#       Mohit Raj                                           #
#       mohitraj.ranu@gmail.com                             #
#       12/04/2019                                          #
#                                                           #
#                                                           #
#############################################################

TOMCAT_PID=$(ps -ef | awk '/[t]omcat/{print $2}')
path=/usr/local/tomcat9/logs
home=/usr/local/tomcat9/bin
if [ -z "$TOMCAT_PID" ]
then
    echo "TOMCAT NOT RUNNING"
	cd $home
	cat /dev/null > $path/catalina.out
	
    sudo /usr/local/tomcat9/bin/startup.sh
else
   echo TOMCAT PROCESSID $TOMCAT_PID
   echo "TOMCAT RUNNING"
   fi