#!/bin/bash
#############################################################
#                                                           #
#       Image Conversion Script                             #
#       Mohit Raj                                           #
#       mohitraj.ranu@gmail.com                             #
#       01/04/2019                                          #
#                                                           #
#                                                           #
#############################################################
home=/home/ubuntu/generationTomcat/apache-tomcat-8.5.41/webapps/ROOT/DocumentTracking/
 cd $home
 filename=$1
 #convert "$1"  "$(basename "$filename" .tif).jpg"
 cp -p /home/ubuntu/generationTomcat/apache-tomcat-8.5.41/webapps/ROOT/N2000HZ118-19-0149639-4.jpg $filename
 #fileList=`ls *"$filename"*  -p | grep -v / | tr '\n' ','`
 echo "$filename"
 #echo "$filename".jpg