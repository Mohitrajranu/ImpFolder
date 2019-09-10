#!/bin/bash
#This script will be used to process renewal subscription alias dos2unix="sed -i -e 's/'\"\$(printf '\015')\"'//g' "
cd ~
. .bash_profile

ORACLE_BASE=/app/oracle/product
ORACLE_HOME=/app/oracle/product/client_home1
PATH=$PATH:$ORACLE_HOME/bin
export ORACLE_BASE ORACLE_HOME PATH

HOME=/home/mbas

cd $HOME/BackEndProcess/cronjobs/
today=`./today.pl`

cd $HOME/BackEndProcess/classes/

df_date=`date`

echo Log Base is $LOG_BASE

PROPERTY_FILE=$HOME/BackEndProcess/config/Constants.properties

DB_CONNECT=$(sed '/^\#/d' $PROPERTY_FILE | grep  -i 'sqlDb_connect' | cut -f2 -d'=')

echo $DB_CONNECT
 truncate_table(){
 
    performOpt=`$ORACLE_HOME/bin/sqlplus -s ${DB_CONNECT} <<-EOF
    set feed off
    set pages 0
    BEGIN
    EXECUTE IMMEDIATE 'TRUNCATE TABLE T_SUB_SERVICE_INFO_DIG_REN DROP STORAGE';
    EXECUTE IMMEDIATE 'TRUNCATE TABLE T_SUB_SERVICE_INFO_REN_ERRORS DROP STORAGE';
    UPDATE T_COM_CONSTANTS SET constant_value = '0',created_on = sysdate ,modified_on = sysdate, comments ='NOT_STARTED' WHERE CONSTANT_CODE = 'PROCEDURE_STATUS';
    commit;
    END;
     /
    EXIT;
EOF`
   echo $performOpt
}
 AutoRenewal_Running=`ps -ef |  grep -i "AutoRenewalProcess.jar" |grep -v "grep" | awk '{print $2}' | wc -l`
    if [ $AutoRenewal_Running -ge 1 ]
    then
    echo "AutoRenewalscript is already running cannot start truncate process."
  else
    echo "AutoRenewalscript is not running, Starting temp table truncate process"
    echo "Now processing step:" $DB_CONNECT
  truncate_table
 echo Process Renewal subscription Started at $df_date
 java -jar AutoRenewalProcess.jar /home/mbas/BackEndProcess/config/AutoRenewal.properties 1>>$LOG_BASE/BackEndProcess/log/RenewalSubscriptionDynamicUsers_out.log.$today 2>>$LOG_BASE/BackEndProcess/log/RenewalSubscriptionDynamicUsers_err.log.$today
   #java com/btsl/process/RenewalProcessDynamicUsers /home/mbas/BackEndProcess/config/AutoRenewal.properties 1>>$LOG_BASE/BackEndProcess/log/RenewalSubscriptionDynamicUsers_out.log.$today 2>>$LOG_BASE/BackEndProcess/log/RenewalSubscriptionDynamicUsers_err.log.$today

  fi

  
##javac HelloWorld.java
##$ echo Main-Class: HelloWorld > MANIFEST.MF
##$ jar -cvmf MANIFEST.MF helloworld.jar HelloWorld.class 