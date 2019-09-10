#!/bin/bash
#This script will be used to process Validity Extension
cd ~
. .bash_profile

HOME=/home/mbas

cd $HOME/BackEndProcess/cronjobs/
today=`./today.pl`

cd $HOME/BackEndProcess/classes/

df_date=`date`

echo Log Base is $LOG_BASE

export CLASSPATH=$CLASSPATH:$JBOSS_HOME/server/mBAS/lib/log4j-api-2.0-beta8.jar
export CLASSPATH=$CLASSPATH:$JBOSS_HOME/server/mBAS/lib/log4j-core-2.0-beta8.jar
export CLASSPATH=$CLASSPATH:$JBOSS_HOME/server/mBAS/lib/commons-validator.jar
export CLASSPATH=$CLASSPATH:$JBOSS_HOME/server/mBAS/lib/mail-1.4.1.jar
export CLASSPATH=$CLASSPATH:$JBOSS_HOME/server/mBAS/lib/commons-codec.jar
export CLASSPATH=$CLASSPATH:$JBOSS_HOME/server/mBAS/lib/commons-collections.jar
export CLASSPATH=$CLASSPATH:$JBOSS_HOME/server/mBAS/lib/commons-digester-1.6.jar
export CLASSPATH=$CLASSPATH:$JBOSS_HOME/server/mBAS/lib/commons-fileupload.jar
export CLASSPATH=$CLASSPATH:$JBOSS_HOME/server/mBAS/lib/commons-httpclient.jar
export CLASSPATH=$CLASSPATH:$JBOSS_HOME/server/mBAS/lib/commons-net-2.0.jar
export CLASSPATH=$CLASSPATH:$JBOSS_HOME/server/mBAS/lib/commons-logging.jar
export CLASSPATH=$CLASSPATH:$JOSS_HOME/server/mBAS/lib/ojdbc14.jar
export CLASSPATH=$CLASSPATH:$HOME/BackEndProcess/lib/log4j-1.2.6.jar
export CLASSPATH=$CLASSPATH:$HOME/BackEndProcess/lib/ojdbc14.jar
echo your classpath is $CLASSPATH
echo Process Validity Extension for Dynamic Users  Started at $df_date
#LOG=/home/mbas/BackEndProcess/lib/log4j-1.2.6.jar
#export $LOG


echo Process Validation Extension Started at $df_date

java com/btsl/process/ValidityExtension
1>>$LOG_BASE/backEndProcessLogs/ValidityExtension_out.log.$today 2>>$LOG_BASE/backEndProcessLogs/ValidityExtension_err.log.$today

