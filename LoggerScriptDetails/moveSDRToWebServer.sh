#!/bin/bash
#Author:Mohit Raj

      # varDate=`./prevoius60mins.p1`
      # echo $varDate
      # FILE_PATTERN="SDRSE00_"$varDate
      # echo $FILE_PATTERN
       source "/home/mbas/BackEndProcess/config/loggerConstants.txt"
       echo "$elapseTime" #test
       elapsedtime="$elapseTime"
       currentAppdirectory="$appSDRFilePath"
       backupAppdirectory="$sdrBackupLocation"
       webServerPasswd="$webServerPasswdScp"
       destinationWebdir="$webServerFilePath"
       SOURCE_DIR="$appSDRDirFilecheck"
       files=("$SOURCE_DIR")
      # if [ ${#files[@]} -gt 0 ];
      if [ "$(ls -A $currentAppdirectory)" ];
       then
       echo "File present in the Source directory is a non empty file"
       echo "$appSDRFilePath"
       cd "$currentAppdirectory"
       for i in ` find . -maxdepth 1 -type f -name '*SDRSE00*.cdr' -printf "%f\n"`;do
      # for i in `ls *$FILE_PATTERN*`;do
       if [ ! -s $i ];
       then
       continue
       fi
        NAME=$i
        current=`date +%s`
        last_modified=`stat -c "%Y" $i`
        if [ $(($current-$last_modified)) -lt $elapsedtime ]; then
        echo "Wait for the current file:$i to get updated as file is still under process and is Incomplete.";
        else
        chmod 777 $i
        str1="${i##*/}"
        echo "$str1" >> /home/mbas/BackEndProcess/loggerScript1.log
        if [ -s $i ];
        then
        echo "$i"  >> /home/mbas/BackEndProcess/loggerScript1.log
        mv $NAME $NAME.PART
	sshpass -p "$webServerPasswd"  scp -r  $NAME.PART mbas@10.135.25.55:"$destinationWebdir"
	if [ $? -ne 0 ];
        then
        echo "File $str1.PART transfer failed from App Server to Web server" >> /home/mbas/BackEndProcess/loggerScript1.log
	mv $NAME.PART $NAME
        else
        echo "File $str1.PART is transferred successfully from App Server to Web server" >> /home/mbas/BackEndProcess/loggerScript1.log
       # ssh $user@$server "for FILENAME in '$srcFolder'/*.txt; do mv \"\$FILENAME\" \"\$FILENAME.$DATE\"; done"
        sshpass -p "dbillse@123" ssh mbas@10.135.25.55 mv "$destinationWebdir""$str1.PART" "$destinationWebdir""$str1"
        mv $NAME.PART $NAME
        mv -u $NAME $backupAppdirectory
       # sshpass -p "$webServerPasswd" sftp mbas@10.135.25.55
        fi
        else
        echo " File $i is an empty file"  >> /home/mbas/BackEndProcess/loggerScript1.log
	fi
        fi
        done
        else
        echo "directory is empty no more files are present"
        fi
