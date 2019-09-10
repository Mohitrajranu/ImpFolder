#!/bin/bash
#############################################################
#                                                           #
#       Offline Compaction Script                           #
#       Mohit Raj                                           #
#       mohitraj.ranu@gmail.com                             #
#       25/03/2019                                          #
#                                                           #
#                                                           #
#############################################################

SEGMENTSSTORE_DIR=/usr/local/tomcat8/apache-tomcat-8.5.35/bin/sling/_scorpio/repository/segmentstore
SLING_DIR=/usr/local/tomcat8/apache-tomcat-8.5.35/bin/sling/_scorpio
TOMCAT_DIR=/usr/local/tomcat8/apache-tomcat-8.5.35/
OAKRUN=$TOMCAT_DIR/bin/oak-run-1.6.9.jar
LOG_DIR=/var/log/compact

TODAY_DATE="$(date +'%d-%m-%Y')"
LOG_FILE=$LOG_DIR/"compact-$TODAY_DATE.log"

## Shutdown Tomecat
$TOMCAT_DIR/bin/shutdown.sh
TODAY_DATE="$(date +'%d-%m-%Y %H:%M:%S')"
echo "$TODAY_DATE : Tomcat Shutdown" >> $LOG_FILE

# Find old checkpoints
TODAY_DATE="$(date +'%d-%m-%Y %H:%M:%S')"
echo "$TODAY_DATE : Finding old checkpoints" >> $LOG_FILE
java -Dtar.memoryMapped=true -jar $OAKRUN checkpoints $SEGMENTSSTORE_DIR >> $LOG_FILE

# Delete unreferenced checkpoints
TODAY_DATE="$(date +'%d-%m-%Y %H:%M:%S')"
echo "$TODAY_DATE : Deleting unreferenced checkpoints" >> $LOG_FILE
java -Dtar.memoryMapped=true -jar $OAKRUN checkpoints $SEGMENTSSTORE_DIR rm-unreferenced >> $LOG_FILE

# Run compaction
TODAY_DATE="$(date +'%d-%m-%Y %H:%M:%S')"
echo "$TODAY_DATE : Running compaction" >> $LOG_FILE
java -Dtar.memoryMapped=true -jar $OAKRUN compact $SEGMENTSSTORE_DIR >> $LOG_FILE

# Start Tomcat
TODAY_DATE="$(date +'%d-%m-%Y %H:%M:%S')"
$TOMCAT_DIR/bin/startup.sh
echo "$TODAY_DATE : Tomcat started" >> $LOG_FILE

#Schedule Script after every 4th hour in a day.            
#0 */4 * * * path_to_the_script			  