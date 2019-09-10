#!/bin/bash
#############################################################
#                                                           #
#       Mail Tracking Script                                #
#       Mohit Raj                                           #
#       mohitraj.ranu@gmail.com                             #
#       01/04/2019                                          #
#                                                           #
#                                                           #
#############################################################
home=/usr/local/tomcat9/logs
currdate=$(date '+%Y-%m-%d')
logfilename="localhost_access_log."$currdate".txt"
 cd $home
 filename=$1
 #convert "$1"  "$(basename "$filename" .tif).jpg"
 #cp -p /home/ubuntu/generationTomcat/apache-tomcat-8.5.41/webapps/ROOT/N2000HZ118-19-0149639-4.jpg $filename
 fileList=`grep -E '200' $logfilename | grep -E '*$filename*' | awk '{print $7 " IP " $1 " Response " $9}'| cut -d ' ' -f3 | grep -v / | tr '\n' ','`
 IpList=`ls *"$filename"*  -p | grep -v / | tr '\n' '_'`
 echo -e "{\"hostname\":\""$IpList"\", \"response\":\"200\", \"status\":\"open\"}"
 #printf '{"hostname":"%s","response":"%s","status":"%s"}\n' "$IpList" "200" "open"
 #echo "$IpList"
 #echo "$filename".jpg
 egrep "2018-04-12 14:44:01.000|2018-04-12 14:46:00.000" logfile