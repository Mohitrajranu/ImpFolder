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
home=/usr/local/tomcat9/webapps/EssarInvoice/InvoiceImages/
 cd $home
 chmod 777 $1
 filename=`echo $1 | rev | cut -f 2- -d '.' | rev`
  case "$1" in
    *.[tT][iI][fF])  convert "$1"  "$(basename "$filename" .tif).jpg" ;;
    *.[tT][iI][fF][fF]) convert "$1"  "$(basename "$filename" .tiff).jpg";;
    *.[pP][nN][gG])  convert "$1"  "$(basename "$filename" .png).jpg" ;;
    *) ;;
 esac
 #convert "$1"  "$(basename "$filename" .tif).jpg"
 rm -rf $1
 fileList=`ls *"$filename"*  -p | grep -v / | tr '\n' ','`
 echo "$fileList"
 #echo "$filename".jpg
