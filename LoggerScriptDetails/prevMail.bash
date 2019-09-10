#!/bin/bash
#############################################################
#                                                           #
#       Previous Mail Tracking Script                                #
#       Mohit Raj                                           #
#       mohitraj.ranu@gmail.com                             #
#       01/04/2019                                          #
#                                                           #
#                                                           #
#############################################################
home=/usr/local/tomcat9/logs
filename=$1
logFile=$2
# this variable you could customize, important is convert to seconds.
# e.g 5days=$((5*24*3600))
x=$((5*60))   #here we take 5 mins as example

cd $home
# this line get the timestamp in seconds of last line of your logfile
last=`tail -n1 $logFile|awk -F'[][]' '{ gsub(/\//," ",$2); sub(/:/," ",$2); "date +%s -d \""$2"\""|getline d; print d;}' `
#echo $last
range1=`date -d @"$last" '+%d/%b/%Y:%H:%M:%S'`
currdate=`date -d @"$last" '+%d/%b/%Y'`
#echo $range1
#echo $currdate
#echo $currdate$2
from="["$currdate":"$3
to="["$range1
#echo $from
#echo $to
#sed -n '/09\/Sep\/2019:17:27:52/,/$range1/p' $logFile
#this awk will give you lines you needs:
#echo | awk -v variable=$from 'END{print variable}'
#echo | awk -v variable=$to 'END{print variable}'

output=`echo |awk -v fromdate="$from" -v todate="$to"  '$4 > fromdate && $4 < todate' $logFile|grep -E '200'| grep -E 'GET'|grep -E $filename | awk '{print $7 " IP " $1 " Response " $9}'| cut -d ' ' -f3 | grep -v / | tr '\n' '#'`
#IpList=`grep -E '200' $output| grep -E 'GET' | grep -E $filename | awk '{print $7 " IP " $1 " Response " $9}'| cut -d ' ' -f3 | grep -v / | tr '\n' '#'`
if [ -z "$output" ]
 then
       echo -e "{\"hostname\":\"$output\", \"response\":\"200\", \"status\":\"notOpen\",\"datetime\":\"$dateTime\"}"
 else
       echo -e "{\"hostname\":\"$output\", \"response\":\"200\", \"status\":\"open\",\"dateTime\":\"$dateTime\"}"
 fi

#output=`awk -F'[][]' -v last=$last -v x=$x '{ gsub(/\//," ",$2); sub(/:/," ",$2); "date +%s -d \""$2"\""|getline d; if (last-d<=x)print $0 }' $logFile`
dateTime=`echo |awk -v fromdate="$from" -v todate="$to"  '$4 > fromdate && $4 < todate' $logFile| grep -E '200'| grep -E 'GET' | grep -E $filename | awk '{print $4 " IP " $4 " Response " $9}'| cut -d ' ' -f1|tr '\n' '#'|tr -d '[],'|tr -d '/,'`