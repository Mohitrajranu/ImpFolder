#!/bin/bash
# script will be used to process provisioning call back
#!/bin/bash
cd ~
. .bash_profile

HOME=/home/mbas

cd $HOME/BackEndProcess/cronjobs/
today=`./today.pl`

cd $HOME/BackEndProcess/classes/

df_date=`date`


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
export CLASSPATH=$CLASSPATH:$HOME/BackEndProcess/lib/commons-io-2.4.jar
export CLASSPATH=$CLASSPATH:$HOME/BackEndProcess/lib/log4j-1.2.6.jar
export CLASSPATH=$CLASSPATH:$HOME/BackEndProcess/lib/ojdbc14.jar
export CLASSPATH=$CLASSPATH:/home/mbas/apache-tomcat-7.0.35/lib/servlet-api.jar

echo your classpath is $CLASSPATH
echo Process Started $df_date
#LOG=/home/mbas/BackEndProcess/lib/log4j-1.2.6.jar
#export $LOG

java  com/btsl/process/PlanbillChangeFileProcess 1>>$LOG_BASE/backEndProcessLogs/PlanbillChangeFileProcess_out.log.$today 2>>$LOG_BASE/backEndProcessLogs/PlanbillChangeFileProcess_err.log.$today
