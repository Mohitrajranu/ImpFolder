1.CREATE TABLE "T_SUB_SERVICE_INFO_DIG_REN" 
   (	"SUBSCRIPTION_ID" VARCHAR2(60 BYTE), 
	"MSISDN" VARCHAR2(100 BYTE) NOT NULL ENABLE, 
	"SERVICE_ID" VARCHAR2(30 BYTE) NOT NULL ENABLE, 
	"SERVICE_TYPE" VARCHAR2(20 BYTE) NOT NULL ENABLE, 
	"CATEGORY" VARCHAR2(30 BYTE) NOT NULL ENABLE, 
	"CONTENT_ID" VARCHAR2(30 BYTE) NOT NULL ENABLE, 
	"SUBS_TYPE" NUMBER, 
	"REGION_CODE" VARCHAR2(15 BYTE), 
	"AUTO_RENEW" CHAR(1 BYTE), 
	"OFFER_ID" VARCHAR2(32 BYTE), 
	"PLAN_ID" VARCHAR2(32 BYTE), 
	"CRM_ID" VARCHAR2(50 BYTE), 
	"CUST_SERVICE_ID" VARCHAR2(50 BYTE), 
	"BEARER_ID" VARCHAR2(10 BYTE), 
	"SERVICE_NODE" VARCHAR2(20 BYTE) NOT NULL ENABLE, 
	"TRXN_ID" VARCHAR2(60 BYTE) NOT NULL ENABLE, 
	"CUST_STATUS" CHAR(1 BYTE), 
	"LANGUAGE_ID" VARCHAR2(10 BYTE), 
	"CALLED_PARTY" VARCHAR2(20 BYTE), 
	"PRODUCT_ORDER_ID" VARCHAR2(50 BYTE), 
	"PARAM1" VARCHAR2(50 BYTE), 
	"PARAM2" VARCHAR2(50 BYTE), 
	"PARAM3" VARCHAR2(50 BYTE), 
	"PARAM4" VARCHAR2(50 BYTE), 
	"PARAM5" VARCHAR2(50 BYTE), 
	"SERVICE_GROUP" VARCHAR2(32 BYTE), 
	"RENEWAL_DATE" DATE, 
	"RETRY_COUNT" NUMBER DEFAULT NULL, 
	 CONSTRAINT "T_SUB_SERVICE_INFO_DIG_REN_PK" PRIMARY KEY ("SUBSCRIPTION_ID")
	 );

2. CREATE TABLE "T_SUB_SERVICE_INFO_REN_ERRORS" 
   (	"ERROR_MESG" VARCHAR2(255 BYTE)
   );
   
3. Insert into T_COM_CONSTANTS (CONSTANT_CODE,CONSTANT_VALUE,ENABLE,CREATED_BY,CREATED_ON,MODIFIED_ON,MODIFIED_BY,COMMENTS) values ('PROCEDURE_STATUS','0','Y','mbasAutoRenew',sysdate,sysdate,null,'NOT_STARTED');

4. create sequence seq_autoRenew minvalue 1 maxvalue 999999999999999 start with 1 increment by 1 nocache cycle;
5. CREATE TABLE "T_COM_RENEW_PROC_INFO" 
   (	"SEQ_ID" NUMBER, 
	"CONSTANT_ID" NUMBER, 
	"CONSTANT_CODE" VARCHAR2(100 BYTE) NOT NULL ENABLE, 
	"CONSTANT_VALUE" VARCHAR2(500 BYTE) NOT NULL ENABLE, 
	"STATUS" CHAR(1 BYTE) NOT NULL ENABLE, 
	"CREATED_BY" VARCHAR2(100 BYTE) NOT NULL ENABLE, 
	"CREATED_ON" DATE NOT NULL ENABLE, 
	"MODIFIED_ON" DATE, 
	"MODIFIED_BY" VARCHAR2(100 BYTE), 
	"COMMENTS" VARCHAR2(150 BYTE) NOT NULL ENABLE
   );

6. create or replace trigger AUTORENEW_PROC_CALL before update or delete on T_COM_CONSTANTS
 REFERENCING OLD as old 
 for each row      
  WHEN (old.CONSTANT_CODE = 'PROCEDURE_STATUS')
  declare
  v_renewInfo T_COM_RENEW_PROC_INFO%rowtype;
 begin
 select seq_autoRenew.nextval into v_renewInfo.SEQ_ID from dual;
 if updating then 
  v_renewInfo.CONSTANT_ID := :old.CONSTANT_ID;
  v_renewInfo.CONSTANT_CODE := :old.CONSTANT_CODE;
  v_renewInfo.CONSTANT_VALUE := :old.CONSTANT_VALUE;
  v_renewInfo.STATUS := 'Y';
  v_renewInfo.CREATED_BY := :old.CREATED_BY;
  v_renewInfo.CREATED_ON := :old.CREATED_ON;
  v_renewInfo.MODIFIED_ON := sysdate;
  v_renewInfo.MODIFIED_BY := 'UPDATE';
  v_renewInfo.COMMENTS := :old.COMMENTS;
   elsif deleting then
  v_renewInfo.CONSTANT_ID := :old.CONSTANT_ID;
  v_renewInfo.CONSTANT_CODE := :old.CONSTANT_CODE;
  v_renewInfo.CONSTANT_VALUE := :old.CONSTANT_VALUE;
  v_renewInfo.STATUS := 'Y';
  v_renewInfo.CREATED_BY := :old.CREATED_BY;
  v_renewInfo.CREATED_ON := :old.CREATED_ON;
  v_renewInfo.MODIFIED_ON := sysdate;
  v_renewInfo.MODIFIED_BY := 'DELETE';
  v_renewInfo.COMMENTS := :old.COMMENTS;
end if;
 insert into T_COM_RENEW_PROC_INFO values v_renewInfo;
end AUTORENEW_PROC_CALL;
/

7. create or replace PACKAGE AutoRenewal_Process AS
   
   PROCEDURE RenewSubscriber(recordLimit INT,maxRetryCount INT,rec_Count OUT NUMBER);
   PROCEDURE RenewStatusCnt(maxrenretry INT,renewnonretry OUT NUMBER,renewretry OUT NUMBER);
 
   END AutoRenewal_Process;
   /
   
8. create or replace PACKAGE BODY AutoRenewal_Process AS
  
   
PROCEDURE RenewSubscriber(recordLimit INT,maxRetryCount INT,rec_Count OUT NUMBER)

/*
*  Copyright(c) 2017, Mahindra Comviva Pvt. Ltd.
*  All Rights Reserved
*  -----------------------------------------------------------------------------
*  Author              Date            History
*  -----------------------------------------------------------------------------
*  Mohit Raj       04/12/2017         Data Movement of Renewal eligible records 
                                       from Live Table to temporary Table.
*  -----------------------------------------------------------------------------
*/
IS

  -- define array type of the new table
  TYPE new_table_array_type IS TABLE OF T_SUB_SERVICE_INFO_DIG_REN%ROWTYPE INDEX BY BINARY_INTEGER;

  -- define array object of new table
     new_table_array_object new_table_array_type;

  -- fetch size on  bulk operation, scale the value to tweak
  -- performance optimization over IO and memory usage
      fetch_size NUMBER := recordLimit;

      errors          NUMBER;
      error_mesg      VARCHAR2(255);
      bulk_error      EXCEPTION;
      l_cnt           NUMBER := 0;
      PRAGMA exception_init
            (bulk_error, -24381);
  -- define select statment of old table
  -- select desiered columns of OLD_TABLE to be filled in NEW_TABLE
  CURSOR old_table_cursor IS
    SELECT SI.SUBSCRIPTION_ID, SI.MSISDN, SI.SERVICE_ID, SI.SERVICE_TYPE, SI.CATEGORY, SI.CONTENT_ID, SI.SUBS_TYPE, SI.REGION_CODE, SI.AUTO_RENEW, SI.OFFER_ID, SI.PLAN_ID, SI.CRM_ID, SI.CUST_SERVICE_ID,
SI.BEARER_ID, SI.SERVICE_NODE, SI.TRXN_ID, SI.CUST_STATUS, SI.LANGUAGE_ID, SI.CALLED_PARTY, SI.PRODUCT_ORDER_ID, SI.PARAM1, SI.PARAM2,
SI.PARAM3, SI.PARAM4, SI.PARAM5, SM.SERVICE_GROUP,SI.RENEWAL_DATE,SI.RETRY_COUNT FROM T_SUB_SERVICE_INFO SI, T_D_CNF_SERVICE_MASTER SM where SI.SERVICE_ID = SM.SERVICE_ID AND SI.SERVICE_TYPE = SM.SERVICE_TYPE AND SI.AUTO_RENEW='Y'  
  AND SI.CUST_STATUS = 'A' AND SI.RENEWAL_DATE < trunc(sysdate+1) ;

BEGIN

  OPEN old_table_cursor;
  loop
    -- bulk fetch(read) operation
    FETCH old_table_cursor BULK COLLECT
      INTO new_table_array_object LIMIT fetch_size;
  
    -- bulk Insert operation
    BEGIN
    FORALL i IN INDICES OF new_table_array_object SAVE EXCEPTIONS
      INSERT INTO T_SUB_SERVICE_INFO_DIG_REN VALUES new_table_array_object(i);
    EXCEPTION
        WHEN bulk_error THEN
          errors :=
             SQL%BULK_EXCEPTIONS.COUNT;
          l_cnt := l_cnt + errors;
          FOR i IN 1..errors LOOP
            error_mesg := SQLERRM(-SQL%BULK_EXCEPTIONS(i).ERROR_CODE);
            INSERT INTO t_sub_service_info_ren_errors
            VALUES     (error_mesg);
     END LOOP;
     END;
  EXIT WHEN old_table_cursor%NOTFOUND;
  END LOOP;
  COMMIT;
  CLOSE old_table_cursor;
  SELECT COUNT(1) INTO rec_Count FROM T_SUB_SERVICE_INFO_DIG_REN where AUTO_RENEW='Y' and retry_count <= maxRetryCount;
  End RenewSubscriber;
  PROCEDURE RenewStatusCnt(maxrenretry INT,renewnonretry OUT NUMBER,renewretry OUT NUMBER)
  IS
  begin
  
   SELECT COUNT(1) INTO renewnonretry FROM T_SUB_SERVICE_INFO_DIG_REN where auto_renew <> 'Y';
   
   SELECT COUNT(1) INTO renewretry FROM T_SUB_SERVICE_INFO_DIG_REN Where AUTO_RENEW='Y' and retry_count <= maxrenretry;
   
  End RenewStatusCnt;
  
  
   END AutoRenewal_Process;
   /