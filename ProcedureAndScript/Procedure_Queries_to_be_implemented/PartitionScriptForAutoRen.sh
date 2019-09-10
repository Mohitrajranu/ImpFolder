#!/bin/bash

ORACLE_BASE=/app/oracle/product
ORACLE_HOME=/app/oracle/product/11204/client_home
PATH=$PATH:$ORACLE_HOME/bin
export ORACLE_BASE ORACLE_HOME PATH

path=/home/mbas/scripts
sqlplus -s <userName>/<password>@mbas<<EOF > $path/DynamicAutoRenewal.txt
set linesize 1000
set pages 0
set serveroutput on
set feedback off
declare
get_long_val varchar2(4000);
get_value_change varchar2(20);
get_partion_name varchar2(50);
check_date varchar2(20);
cursor s1 is select high_value,partition_name from user_tab_partitions where table_name='T_SUB_RENEWAL_API_CALL_STATUS' and partition_name <> 'REN_P0';
begin
open s1;
loop
fetch s1 into get_long_val,get_partion_name;
exit when s1%NOTFOUND;
get_value_change:=SUBSTR(get_long_val,10,12);
if (To_DATE(get_value_change,'YYYY-MM-DD') < sysdate - 1 ) then
dbms_output.put_line ('alter table reliance.T_SUB_RENEWAL_API_CALL_STATUS drop partition '||get_partion_name||' update indexes;');
end if;
get_long_val:='';
get_partion_name:='';
end loop;
end;
/
exit;
EOF

echo "exit;" >>$path/DynamicAutoRenewal.txt
sqlplus -s <userName>/<password>@mbas @$path/DynamicAutoRenewal.txt

