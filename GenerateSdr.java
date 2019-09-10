package com.btsl.logger;

/**
 * @author Sumit Jagtap.
 * Create Date : 23-Jan-2016
 */


import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;

import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;

import java.text.DateFormat;
import java.text.SimpleDateFormat;

import java.util.Calendar;
import java.util.Date;
import java.util.Properties;

import javax.naming.LimitExceededException;

import org.apache.log4j.Logger;


public class GenerateSdr {

	private static Logger logger = Logger.getLogger(GenerateSdr.class);	
	public String propFile = null;
	public static final int defaultBatchSize = 1000;
	private static final String FLAG_YES = "Y";
	private static final SimpleDateFormat  sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
	private static final SimpleDateFormat fmt  = new SimpleDateFormat("yyyyMMddHH");
	private static int sleepCount = 1;
	private static String previousLogFilePath = null;
	private FileWriter writer = null;
	public static void main(String args[]) {
		logger.info("GenerateSdr :: main(), Enter");

		Properties props = new Properties();
		InputStream input = null;

		GenerateSdrVO mGenerateSdrVO = null;

		GenerateSdr mGenerateSdr = null;

		try {
			mGenerateSdr = new GenerateSdr();
			mGenerateSdrVO = new GenerateSdrVO();
			
			org.apache.log4j.PropertyConfigurator.configure(System.getProperty("user.home") + File.separator + "LoggerNew/config/log4j.properties");

			if (args.length > 0){
				input = new FileInputStream(args[0]);
				mGenerateSdr.setPropFile(args[0]);
				props.load(input);
			}
			else{
				System.out.println("main(), Property file not found in argument");
				logger.info("main(), Property file not found in argument");
				return;
			}	

			

			

			mGenerateSdr.setSdrParam(mGenerateSdrVO, props);


			logger.info("main() : fileFrequency ="+mGenerateSdrVO.getFileFrequency());

			if("1".equals(mGenerateSdrVO.getFileFrequency())){
				mGenerateSdr.timeBasedFileGeneration(mGenerateSdrVO);
			}
			else if("2".equals(mGenerateSdrVO.getFileFrequency())){
				mGenerateSdr.timeBasedAndSizeFileGeneration(mGenerateSdrVO);
			}
			else if("3".equals(mGenerateSdrVO.getFileFrequency())){
				mGenerateSdr.timeBasedAndSizeFileGeneration(mGenerateSdrVO);
			}
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally{
			logger.info("GenerateSdr :: main(), finally Exit.");
		}
	}


	public void setSdrParam (GenerateSdrVO mGenerateSdrVO, Properties props){
		if(!isNullString(props.getProperty("table_name"))){				
			mGenerateSdrVO.setTableName(props.getProperty("table_name"));
		}

		if(!isNullString(props.getProperty("fileFrequency"))){	
			mGenerateSdrVO.setFileFrequency(props.getProperty("fileFrequency"));
		}

		if(!isNullString(props.getProperty("log_files_directory"))){				
			mGenerateSdrVO.setLogFileDir(props.getProperty("log_files_directory"));
		}

		if(!isNullString(props.getProperty("file_name"))){				
			mGenerateSdrVO.setFileName(props.getProperty("file_name"));
		}

		if(!isNullString(props.getProperty("max_file_size_in_kb"))){				
			mGenerateSdrVO.setMaxFileSizeInKB(Integer.parseInt(props.getProperty("max_file_size_in_kb")));
		}

		if(!isNullString(props.getProperty("file_creation_time_in_minutes"))){				
			mGenerateSdrVO.setFileCreationTimeInMinutes(Integer.parseInt(props.getProperty("file_creation_time_in_minutes")));
		}

		if(!isNullString(props.getProperty("cust_status"))){				
			mGenerateSdrVO.setCust_status(props.getProperty("cust_status"));
		}

		if(!isNullString(props.getProperty("order_of_logging"))){				
			mGenerateSdrVO.setOrderoflogging(props.getProperty("order_of_logging"));
		}

		if(!isNullString(props.getProperty("record_type"))){				
			mGenerateSdrVO.setRecord_type(props.getProperty("record_type"));
		}

		if(!isNullString(props.getProperty("service_group"))){				
			mGenerateSdrVO.setService_group(props.getProperty("service_group"));
		}

		if(!isNullString(props.getProperty("to_date"))){				
			mGenerateSdrVO.setTo_date(props.getProperty("to_date"));
		}

		if(!isNullString(props.getProperty("from_date"))){				
			mGenerateSdrVO.setFrom_date(props.getProperty("from_date"));
		}

		if(!isNullString(props.getProperty("mode_of_application"))){				
			mGenerateSdrVO.setModeofapplication(props.getProperty("mode_of_application"));
		}

		if(!isNullString(props.getProperty("timeFormat"))){				
			mGenerateSdrVO.setTimeFormat(props.getProperty("timeFormat"));
		}

		if(!isNullString(props.getProperty("lastHour"))){				
			mGenerateSdrVO.setLastHour(props.getProperty("lastHour"));
		}

		if(!isNullString(props.getProperty("reclimit"))){				
			mGenerateSdrVO.setReclimit(Integer.parseInt(props.getProperty("reclimit")));
		}
		if(!isNullString(props.getProperty("batchSize"))){				
			mGenerateSdrVO.setBatchSize(Integer.parseInt(props.getProperty("batchSize")));
		}

		if(!isNullString(props.getProperty("orderByFlag"))){				
			mGenerateSdrVO.setOrderByFlag(props.getProperty("orderByFlag"));
		}
		
		if(!isNullString(props.getProperty("sleepTime"))){	
			mGenerateSdrVO.setSleepTime(Integer.parseInt(props.getProperty("sleepTime")));
		}
		
		if(!isNullString(props.getProperty("sleepTimeMultiplier"))){				
			mGenerateSdrVO.setSleepTimeMultiplier(Integer.parseInt(props.getProperty("sleepTimeMultiplier")));
		}
	}


	private static String getDateFormat(String dateFormat) {
		String todayDate =  null;
		Calendar cal = Calendar.getInstance();
		DateFormat dateFormatObj = new SimpleDateFormat(dateFormat);
		todayDate = dateFormatObj.format(cal.getTime());
		cal = null;
		dateFormatObj = null;
		return todayDate;
	}

	private static String getDateFormatForRecord(java.util.Date createdOn, String dateFormat) {
		String todayDate =  null;
		DateFormat dateFormatObj = new SimpleDateFormat(dateFormat);
		todayDate = dateFormatObj.format(createdOn);
		dateFormatObj = null;
		logger.info("getDateFormatForRecord() : todayDate ="+todayDate);
		return todayDate;
	}

	public void timeBasedAndSizeFileGeneration (GenerateSdrVO mGenerateSdrVO){
		FileWriter writer = null;
		Connection conn = null;
		StringBuffer sql=null;
		int j = 0;
		String logFilePath = null;

		PreparedStatement pstmt = null;
		ResultSet res = null;
		ResultSetMetaData metadata = null;
		PreparedStatement pstmtDel = null;
		PreparedStatement pstmtInt = null;
		File file = null;
		int batchSize = 0;
		File tokenFile = null;
		Statement stmt = null;
		int count = 0;
		
		while (true) {
			try {
				if(mGenerateSdrVO.getBatchSize() <=0){
					batchSize = 1000;	
				}else{
					batchSize = mGenerateSdrVO.getBatchSize();
				}
				sql = new StringBuffer();
				logger.info("timeBasedAndSizeFileDIGGeneration() : logFileDir ="+mGenerateSdrVO.getLogFileDir());

				/*if("VAS".equals(mGenerateSdrVO.getService_group())){
					logFilePath = mGenerateSdrVO.getLogFileDir()	+ File.separator + mGenerateSdrVO.getFileName() + getDateFormat(mGenerateSdrVO.getTimeFormat()) + "_VAS.cdr";
				}
				else{
					logFilePath = mGenerateSdrVO.getLogFileDir()	+ File.separator + mGenerateSdrVO.getFileName() + getDateFormat(mGenerateSdrVO.getTimeFormat()) + ".cdr";
				}

				logger.info("timeBasedAndSizeFileDIGGeneration() : logFilePath ="+logFilePath);

				file = new File(logFilePath);
				writer = new FileWriter(file);*/
				conn = ConnectionFactory.getDBConnection(0);

				sql = sql.append("select rowid, l.* from " + mGenerateSdrVO.getTableName() + " l where  SERVICE_GROUP = '"+ mGenerateSdrVO.getService_group() +"' and rec_type= '" +mGenerateSdrVO.getRecord_type()+ "' and status in ("+ mGenerateSdrVO.getCust_status() +") ");   //(PROCEED is null or PROCEED!='Y') and

				/*if(!isNullString(mGenerateSdrVO.getLastHour()) && "Y".equalsIgnoreCase(mGenerateSdrVO.getLastHour())){
					sql.append(" and CREATED_ON >= (SYSDATE - 1/24) ");
				}*/

				if(mGenerateSdrVO.getReclimit() > 0 ){
					sql.append(" and rownum <= "+ mGenerateSdrVO.getReclimit() +" ");
				}


				if(!(isNullString(mGenerateSdrVO.getFrom_date()) || isNullString(mGenerateSdrVO.getTo_date()))) {
					sql.append(" and CREATED_ON >= TO_DATE('"+ mGenerateSdrVO.getFrom_date() +"', 'YYYY/MM/DD HH24:MI:SS') and CREATED_ON <= TO_DATE('"+ mGenerateSdrVO.getTo_date() +"', 'YYYY/MM/DD HH24:MI:SS') ");
				}

				//order by flag based
				if(!isNullString(mGenerateSdrVO.getOrderByFlag()) && "Y".equalsIgnoreCase(mGenerateSdrVO.getOrderByFlag())){
					sql.append(" order by CREATED_ON  ");
				}

				logger.info("timeBasedAndSizeFileGeneration() : sql ="+sql.toString());

				pstmt = conn.prepareStatement(sql.toString());
				res = pstmt.executeQuery(sql.toString());
				metadata = res.getMetaData();
				int columnCount = metadata.getColumnCount();
				StringBuilder sb = null;
				long currentFileSizeInKB = 0l;
				StringBuilder insertsql = null;
				StringBuilder deleteSql = null;
				String orderLogging[] = null;
				boolean flag = false;
				int resultSetCounter = 0;
				
				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd HH:mm:ss");
				String dateInString = "19001010 10:20:56";
				java.util.Date firstRecDate = sdf.parse(dateInString);
				deleteSql = new StringBuilder("delete from "+ mGenerateSdrVO.getTableName() +" where rowid=?");
				 stmt=conn.createStatement();
				pstmtDel = conn.prepareStatement(deleteSql.toString());
				while (res.next()) {
					count++;
					resultSetCounter++;

					sb = new StringBuilder("");

					if(!isNullString(mGenerateSdrVO.getOrderoflogging())){
						orderLogging = mGenerateSdrVO.getOrderoflogging().split(",");

						for(int index = 0; index < orderLogging.length; index++ ){

							if (index == orderLogging.length-1)
								sb.append(process(res.getString(orderLogging[index])));
							else
								sb.append(process(res.getString(orderLogging[index]) + "|"));


						}

					}

					//file logic here so that file name can be decided
					java.util.Date createdOn = res.getTimestamp("CREATED_ON");
					createdOn = GenerateSdr.setTimeTillHour(createdOn);
					firstRecDate = GenerateSdr.setTimeTillHour(firstRecDate);
					
					flag =  fmt.format(firstRecDate).equals(fmt.format(createdOn)); // flag true if last record and this record in same hour
					logger.info("timeBasedAndSizeFileGeneration() : createdOn ="+createdOn+ ", firstRecDate="+ firstRecDate + ", flag= "+flag);
					if("VAS".equals(mGenerateSdrVO.getService_group())){
						logFilePath = mGenerateSdrVO.getLogFileDir()	+ File.separator + mGenerateSdrVO.getFileName() + getDateFormatForRecord(createdOn , mGenerateSdrVO.getTimeFormat()) + "_VAS.cdr";
					}
					else{
						logFilePath = mGenerateSdrVO.getLogFileDir()	+ File.separator + mGenerateSdrVO.getFileName() + getDateFormatForRecord(createdOn , mGenerateSdrVO.getTimeFormat()) + ".cdr";
					}

					//check if each record already in same hour, or different hour, accordingly create new file or write in old file

					logger.info("timeBasedAndSizeFileGeneration() : logFilePath ="+logFilePath + "flag ="+flag + ", count="+count);
					if(!flag && count > 1){
						//create a token file for last hour file

						String pathName = previousLogFilePath+".tok";
						//logger.info("timeBasedFileDIGGeneration() : pathName ="+ pathName+ ", previousLogFilePath="+previousLogFilePath);
						tokenFile = new File(pathName);
						boolean isCreated = tokenFile.createNewFile();
						logger.info("timeBasedAndSizeFileGeneration() : isCreated="+isCreated);

						file = new File(logFilePath);
						writer = new FileWriter(file, true);
						previousLogFilePath = logFilePath;

					}

					writer.write(sb.toString() + "\n");
					writer.flush();

					firstRecDate = createdOn;

					currentFileSizeInKB = file.length() / 1024;

					if ((currentFileSizeInKB >= mGenerateSdrVO.getMaxFileSizeInKB())) {
						//hold for next file name with next second
						Thread.sleep(1200);
						writer.close();
						if("VAS".equals(mGenerateSdrVO.getService_group())){
							logFilePath = mGenerateSdrVO.getLogFileDir() + File.separator + mGenerateSdrVO.getFileName() + getDateFormatForRecord(createdOn, mGenerateSdrVO.getTimeFormat()) + "_VAS.cdr";
						}
						else{							
							//logFilePath = mGenerateSdrVO.getLogFileDir() + File.separator + mGenerateSdrVO.getFileName() + getDateFormat(mGenerateSdrVO.getTimeFormat()+"SS");
							logFilePath = mGenerateSdrVO.getLogFileDir() + File.separator + mGenerateSdrVO.getFileName() + getDateFormatForRecord(createdOn, mGenerateSdrVO.getTimeFormat()) + ".cdr";
						}	
						logger.info("timeBasedAndSizeFileDIGGeneration() next size : logFilePath ="+logFilePath);

						String pathName = previousLogFilePath+".tok";
						logger.info("timeBasedAndSizeFileDIGGeneration() : pathName ="+ pathName+ ", previousLogFilePath="+previousLogFilePath);
						tokenFile = new File(pathName);
						boolean isCreated = tokenFile.createNewFile();
						logger.info("timeBasedAndSizeFileGeneration() : isCreated="+isCreated);

						previousLogFilePath = logFilePath;
						file = new File(logFilePath);
						writer = new FileWriter(file, true);
					}

					insertsql = new StringBuilder("insert into t_sub_Logger_info_hist values(");
					for (int i = 2; i <= columnCount; i++) {
						if (i != columnCount)	{
							//logger.info("timeBasedFileDIGGeneration() : colType =  "+metadata.getColumnType(i) +", columnCount="+columnCount + ", i ="+i);
							if(Types.CHAR == metadata.getColumnType(i) || Types.VARCHAR == metadata.getColumnType(i) ){
								insertsql.append("'");
								insertsql.append(process(res.getString(i)));
								insertsql.append("',");
							}else if(Types.NUMERIC == metadata.getColumnType(i) || Types.INTEGER == metadata.getColumnType(i) || Types.FLOAT == metadata.getColumnType(i)){
								insertsql.append((res.getInt(i)));
								insertsql.append(",");
							}else if(Types.DATE == metadata.getColumnType(i) || Types.TIMESTAMP == metadata.getColumnType(i)){
								insertsql.append("TO_DATE('");
								java.util.Date date = res.getTimestamp(i);
								String dateStr = null;
								SimpleDateFormat frmt = new SimpleDateFormat("dd-MM-yy");
								if(date != null){
									dateStr = frmt.format(date);
								}
								//logger.info("timeBasedFileDIGGeneration() : dateStr"+dateStr);
								insertsql.append(dateStr);
								insertsql.append("','dd-MM-yy'),");
							}


						}	else if (i == columnCount){
							//logger.info("timeBasedFileDIGGeneration() : i == colType CASE , colType=  "+metadata.getColumnType(i) +", columnCount="+columnCount + ", i ="+i);
							if(Types.CHAR == metadata.getColumnType(i) || Types.VARCHAR == metadata.getColumnType(i)){
								insertsql.append("',");
								insertsql.append(process(res.getString(i)));
								insertsql.append("')");
							}else if(Types.NUMERIC == metadata.getColumnType(i) || Types.INTEGER == metadata.getColumnType(i) || Types.FLOAT == metadata.getColumnType(i)){
								insertsql.append((res.getInt(i)));
								insertsql.append(")");
							}else if(Types.DATE == metadata.getColumnType(i) || Types.TIMESTAMP == metadata.getColumnType(i)){
								insertsql.append("'");
								insertsql.append(res.getTimestamp(i));
								insertsql.append("')");
							}
						}					

						/*else
							insertsql.append(process(res.getString(i)) + ", )");*/
					}
					logger.info("timeBasedFileDIGGeneration() :  insertsql = "+insertsql.toString());
					conn.setAutoCommit(false);

					//add batch 
					stmt.addBatch(insertsql.toString());
					pstmtDel.setString(1,  res.getString("rowid"));
					pstmtDel.addBatch();
					j++;
					logger.info("timeBasedFileDIGGeneration() :  deleteSql = "+deleteSql.toString() + "counter ="+ j);

					if(j >= batchSize || mGenerateSdrVO.getReclimit() == resultSetCounter){
						j=0;
						logger.info("timeBasedFileDIGGeneration() : In batch");
						int[] insert = stmt.executeBatch();
						//conn.commit();
						logger.info("timeBasedFileDIGGeneration() : Insert batch  , insert=" + insert.length );
						try{
						int[] del = pstmtDel.executeBatch();
						
						logger.info("timeBasedFileDIGGeneration() : delete batch  , dele=" + del.length );
						conn.commit();
						}catch (Exception e) {
							e.printStackTrace();
						}
						if(stmt != null)
							stmt.clearBatch();
						if(pstmtDel != null)
							pstmtDel.clearBatch();

					}
				}

			} 
			catch (BatchUpdateException b) {
				b.printStackTrace();
				continue;
			}
			catch (Exception e) {
				e.printStackTrace();

				try {
					Thread.sleep(1200);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				continue;
			} 
			finally {
				try {
					/*int[] insert = stmt.executeBatch();
					//conn.commit();
					logger.info("timeBasedFileDIGGeneration() : Insert batch  batch, insert=" + insert.length );
					int[] del = pstmtDel.executeBatch();
					logger.info("timeBasedFileDIGGeneration() : delete batch  batch, dele=" + del.length );
					conn.commit();*/
					if(stmt != null)
						stmt.clearBatch();
					if(pstmtDel != null)
						pstmtDel.clearBatch();
					
					if (writer != null){
						writer.close(); writer = null;
					}

					if(res != null){
						res.close(); res = null;
					}
					if(metadata != null){
						metadata = null;
					}
					if(pstmt != null){
						pstmt.close(); pstmt = null;
					}
					if(pstmtInt != null){
						pstmtInt.close(); pstmtInt = null;
					}
					if(pstmtDel != null){
						pstmtDel.close(); pstmtDel = null;
					}

					if(conn != null){
						conn.close(); conn = null;
					}


				} catch (Exception e2) {
					// TODO: handle exception
				}
				logger.info("timeBasedAndSizeFileDIGGeneration() : total no record updated "+j);
				j = 0;
			}	
			if("O".equals(mGenerateSdrVO.getModeofapplication())){
				break;
			}
			try {				
				Thread.sleep(mGenerateSdrVO.getFileCreationTimeInMinutes() * 60 * 1000);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}


	public void timeBasedFileGeneration(GenerateSdrVO mGenerateSdrVO){
		Connection conn = null;
		Connection connInsert = null;
		int j = 0;
		StringBuffer sql = null;
		String logFilePath = null;

		PreparedStatement pstmt = null;
		ResultSet res = null;
		ResultSetMetaData metadata = null;
		PreparedStatement pstmtDel = null;
		PreparedStatement pstmtInt = null;
		File file = null;
		File tokenFile = null;
		int batchSize = 0;
		Statement stmt = null;
		StringBuilder sb = null;
		String insertsql = null;
		StringBuilder deleteSql = null;
		int size = 0;
		String orderLogging[] = null;
		if(!isNullString(mGenerateSdrVO.getOrderoflogging())){
			orderLogging = mGenerateSdrVO.getOrderoflogging().split(",");
		}
		String dateInString = "19001010 10:20:56";
		java.util.Date firstRecDate = null;
		
		while (true) {
			try {
				logger.info("timeBasedFileDIGGeneration() : Enter ");
				if(mGenerateSdrVO.getBatchSize() <= 0){
					batchSize = defaultBatchSize;	
				}
				else{
					batchSize = mGenerateSdrVO.getBatchSize();
				}
				sql = new StringBuffer();
				logger.info("timeBasedFileDIGGeneration() : logFileDir ="+mGenerateSdrVO.getLogFileDir());
				try{
					conn = ConnectionFactory.getDBConnection(0);
					if(conn == null){
						logger.error("timeBasedFileDIGGeneration() : Could not create DB Connection");
						return;
					}
				}catch(Exception e){
					logger.error("timeBasedFileDIGGeneration() : Could not create DB Connection");
					return;
				}
				try{
					connInsert = ConnectionFactory.getDBConnection(1);
					if(connInsert == null){
						logger.error("timeBasedFileDIGGeneration() : Could not create DB Connection");
						return;
					}
				}catch(Exception e){
					logger.error("timeBasedFileDIGGeneration() : Could not create DB Connection");
					return;
				}
				pstmt = conn.prepareStatement(this.prepareSelectionQuery(mGenerateSdrVO), ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
				long runTimeForQuery = System.currentTimeMillis();
				res = pstmt.executeQuery();				
				try {
					res.last();
				    size = res.getRow();
				    res.beforeFirst();
				}
				catch(Exception ex) {
					logger.error("timeBasedFileDIGGeneration() : error in ffetching size of recordset, error="+ex.getMessage());
				}
				runTimeForQuery = System.currentTimeMillis() - runTimeForQuery;
				logger.info("timeBasedFileDIGGeneration() : Execution Time in ms for SELECT QUERY="+runTimeForQuery+"record set size="+size);
				metadata = res.getMetaData();
				boolean flag = false;
				int resultSetCounter = 0;
				if(null == firstRecDate){
					firstRecDate = GenerateSdr.sdf.parse(dateInString);
				}
				deleteSql = new StringBuilder("delete from "+ mGenerateSdrVO.getTableName() +" where rowid=?");
				stmt=connInsert.createStatement();
				pstmtDel = conn.prepareStatement(deleteSql.toString());
				
				while (res.next()) {
					resultSetCounter++;
					sb =  new StringBuilder("");

					if(orderLogging != null){
						orderLogging = mGenerateSdrVO.getOrderoflogging().split(",");

						for(int index = 0; index < orderLogging.length; index++ ){

							if (index == orderLogging.length - 1)
								sb.append(process(res.getString(orderLogging[index])));
							else
								sb.append(process(res.getString(orderLogging[index]) + "|"));
						}
					}
					
					//file logic here so that file name can be decided
					java.util.Date createdOn = res.getTimestamp("CREATED_ON");
					flag = this.createFileAndWriteData(mGenerateSdrVO, createdOn, firstRecDate, sb, resultSetCounter);
					if(!flag){
						try{
							System.exit(0);
						}
						catch(Exception e){
							
						}
					}
					firstRecDate = createdOn;

					insertsql = this.prepareInsertQuery(metadata, res );

					logger.info("timeBasedFileDIGGeneration() :  insertsql = "+insertsql.toString());
					conn.setAutoCommit(false);
					connInsert.setAutoCommit(false);
					//add batch 
					stmt.addBatch(insertsql);
					String rowId = res.getString("rowid");
					logger.info("timeBasedFileDIGGeneration() : rowId="+rowId);
					pstmtDel.setString(1,  rowId);
					pstmtDel.addBatch();
					
					logger.info("timeBasedFileDIGGeneration() :  deleteSql = "+deleteSql.toString() + "counter ="+ j + ", resultSetCounter="+resultSetCounter);
if(++j % batchSize == 0) {

				//	if(j >= batchSize || size == resultSetCounter){
						try {
							j=0;
							long runTimeForBatchInsert = System.currentTimeMillis();
							logger.info("timeBasedFileDIGGeneration() : In batch");
							int[] insert = stmt.executeBatch();
							runTimeForBatchInsert = System.currentTimeMillis() - runTimeForBatchInsert;
							logger.info("timeBasedFileDIGGeneration() : Execution Time in ms for BATCH INSERT="+runTimeForBatchInsert);
							logger.info("timeBasedFileDIGGeneration() : Insert batch  batch, insert=" + insert.length );
							long runTimeForBatchDelete = System.currentTimeMillis();
							logger.info("timeBasedFileDIGGeneration() :1");
							int[] del = null;
							try {
								logger.info("timeBasedFileDIGGeneration() :in try 2");
								del = pstmtDel.executeBatch();
								logger.info("timeBasedFileDIGGeneration() : 3");
							} catch (Exception e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							logger.info("timeBasedFileDIGGeneration() : Execution Time in ms for BATCH DELETE="+runTimeForBatchInsert);
							logger.info("timeBasedFileDIGGeneration() : delete batch  batch, dele=" + del.length );
							conn.commit();
							runTimeForBatchDelete = System.currentTimeMillis() - runTimeForBatchDelete;
							
							if(stmt != null)
								stmt.clearBatch();
							if(pstmtDel != null)
								pstmtDel.clearBatch();
						} catch (BatchUpdateException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						
					}
					
				}
			} 
			catch (BatchUpdateException b) {
				b.printStackTrace();
				continue;
			}
			catch (Exception e) {
				e.printStackTrace();
				try {
					Thread.sleep(1200);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				continue;
			} 
			finally {
				try {
					logger.info("timeBasedFileDIGGeneration() finally block entry");
					
					if (writer != null){
						writer.close();
						writer = null;
					}

					if(res != null){
						res.close(); 
						res = null;
					}
					if(metadata != null){
						metadata = null;
					}
					//
					
					if(pstmt != null){
						pstmt.close(); pstmt = null;
					}
					if(pstmtInt != null){
						pstmtInt.close(); pstmtInt = null;
					}
					if(pstmtDel != null){
						pstmtDel.close(); pstmtDel = null;
					}

					if(conn != null){
						conn.close(); conn = null;
					}
					if(connInsert != null){
						connInsert.close(); connInsert = null;
					}
				} 
				catch (Exception e2) {
					// TODO: handle exception
				}
				logger.info("timeBasedFileDIGGeneration() : total no record updated " +j);
				j = 0;
			}
			
			Properties properties = loadProperty();
			logger.info("loadProperty : MoA= " +properties.getProperty("mode_of_application"));
			if("E".equals(properties.getProperty("mode_of_application"))){
				break;
			}
			if("O".equals(mGenerateSdrVO.getModeofapplication())){
				break;
			}
			
			//Sleep process for defined time
			try {	
				if(size < mGenerateSdrVO.getReclimit()/2){
					this.sleep(mGenerateSdrVO, size);
				}
			}
			catch (Exception e) {
				e.printStackTrace();
			}

		}
	}



	public static String process(String s) {			
		s = (s == null || (s != null && s.trim().equals(""))) ? "" : s;        		
		if(!isNullString(s)){	
			s = s.replaceAll("\n|\r", " ");
		}else{
			s = "";
		}
		if(s == null) 
			s = "";
		if(!isNullString(s) && "null|".equals(s)){	
			s = "|";
		}
		return s;
	}

	public static boolean isNullString (String p_text){
		if(p_text != null && p_text.trim().length() > 0 && !"null".equalsIgnoreCase(p_text.trim())){
			return false;
		}
		else{
			return true;
		}
	}

	public static Date setTimeTillHour(Date date) { //makes mmssSSS as 00 00 000
		Calendar calendar = Calendar.getInstance();

		calendar.setTime( date );
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		return calendar.getTime();
	}
	
	public void loadProp(){
		
	}


	public String getPropFile() {
		return propFile;
	}


	public void setPropFile(String propFile) {
		this.propFile = propFile;
	}
	
	public Properties loadProperty(){
		logger.info("loadProperty():enter");
		Properties props = new Properties();
		InputStream input = null;
		try {
			input = new FileInputStream(propFile);
			props.load(input);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(input!=null)
				try {
					input.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		return props;
	}
	
	public String prepareSelectionQuery(GenerateSdrVO mGenerateSdrVO ){
		logger.info("prepareSelectionQuery(): Enter");
		StringBuilder sqlBuffer = new StringBuilder();
		if(!isNullString(mGenerateSdrVO.getOrderByFlag()) && GenerateSdr.FLAG_YES.equalsIgnoreCase(mGenerateSdrVO.getOrderByFlag())){
			sqlBuffer = sqlBuffer.append("select rowid, logger.* from (select * from ");
			sqlBuffer.append(mGenerateSdrVO.getTableName());
			sqlBuffer.append(" order by ");
			sqlBuffer.append(mGenerateSdrVO.getTableName());
			sqlBuffer.append(".CREATED_ON ) logger where SERVICE_GROUP = '");
			sqlBuffer.append(mGenerateSdrVO.getService_group() );
			sqlBuffer.append("' and rec_type= '" );
			sqlBuffer.append(mGenerateSdrVO.getRecord_type());
			sqlBuffer.append( "' and status in (");
			sqlBuffer.append( mGenerateSdrVO.getCust_status() +") ");

			if(mGenerateSdrVO.getReclimit() > 0 ){
				sqlBuffer.append(" and rownum <= "+ mGenerateSdrVO.getReclimit() +" ");
			}

			if(!(isNullString(mGenerateSdrVO.getFrom_date()) || isNullString(mGenerateSdrVO.getTo_date()))) {
				sqlBuffer.append(" and CREATED_ON >= TO_DATE('"+ mGenerateSdrVO.getFrom_date() +"', 'YYYY/MM/DD HH24:MI:SS') and CREATED_ON <= TO_DATE('"+ mGenerateSdrVO.getTo_date() +"', 'YYYY/MM/DD HH24:MI:SS') ");
			}
		}else{
			sqlBuffer = sqlBuffer.append("select rowid, logger.* from ") ;
			sqlBuffer.append(mGenerateSdrVO.getTableName());
			sqlBuffer.append(" logger where SERVICE_GROUP = '");
			sqlBuffer.append(mGenerateSdrVO.getService_group() );
			sqlBuffer.append("' and rec_type= '" );
			sqlBuffer.append(mGenerateSdrVO.getRecord_type());
			sqlBuffer.append( "' and status in (");
			sqlBuffer.append( mGenerateSdrVO.getCust_status() +") ");
		}
		
		logger.info("prepareSelectionQuery(): Exit , Select Query="+sqlBuffer.toString());
		return sqlBuffer.toString();
		
	}
	
	public boolean createFileAndWriteData(GenerateSdrVO mGenerateSdrVO, java.util.Date createdOn,java.util.Date firstRecDate, StringBuilder sb, int recordNumber){
		boolean result = false;
		String logFilePath = null;
		
		java.util.Date createdOnCompare =  createdOn;
		createdOnCompare = GenerateSdr.setTimeTillHour(createdOnCompare);
		firstRecDate = GenerateSdr.setTimeTillHour(firstRecDate);
		SimpleDateFormat fmt = new SimpleDateFormat("yyyyMMddHH");
		File tokenFile = null;
		File file = null;
		boolean fileCreate = false;
		boolean writeFlag = false;
		boolean flag =  fmt.format(firstRecDate).equals(fmt.format(createdOnCompare)); // flag true if last record and this record in same hour
		logger.info("createFileAndWriteData() : createdOn ="+createdOn+ ", firstRecDate="+ firstRecDate + ", flag= "+flag);
		if("VAS".equals(mGenerateSdrVO.getService_group())){
			logFilePath = mGenerateSdrVO.getLogFileDir()	+ File.separator + mGenerateSdrVO.getFileName() + getDateFormatForRecord(createdOn , mGenerateSdrVO.getTimeFormat()) + "_VAS.cdr";
		}
		else{
			logFilePath = mGenerateSdrVO.getLogFileDir()	+ File.separator + mGenerateSdrVO.getFileName() + getDateFormatForRecord(createdOn , mGenerateSdrVO.getTimeFormat()) + ".cdr";
		}
		
		
		if(!flag){
			//create a token file for last hour file
			String pathName = previousLogFilePath+".tok";
			logger.info("timeBasedFileDIGGeneration() : pathName ="+ pathName+ ", previousLogFilePath="+previousLogFilePath + "logFilePath="+logFilePath);
			
			try {
				tokenFile = new File(pathName);
				boolean isCreated = tokenFile.createNewFile();
				logger.info("createFileAndWriteData() : isCreated="+isCreated);
				file = new File(logFilePath);
				writer = new FileWriter(file, true);
				fileCreate = true;
			} catch (IOException e) {
				logger.error("createFileAndWriteData() :File creation failure  ERROR="+e.getMessage());
				e.printStackTrace();
				fileCreate =  false;
			}
			previousLogFilePath = logFilePath;

		}
		logger.info("createFileAndWriteData() : writer="+writer+ "condition="+(file!= null && file.exists()));
		if ((file!= null && file.exists()) || writer != null) {
			fileCreate = true;
		}
		//write data
		if(fileCreate){
			writeFlag = writeContentToFile(writer, sb.toString());
			if(!writeFlag){
				try {
					if (writer!=null)
						writer.close();
					System.exit(1);
				} catch (IOException e) {
					
					e.printStackTrace();
				}
			}else{
				result = true;
			}
		}else{
			try {
				if (writer!=null)
					writer.close();
				System.exit(1);
			} catch (IOException e) {
				
				e.printStackTrace();
			}
		}
		
		logger.info("createFileAndWriteData() : result="+result);
		
		return result;
	}
	
	public String prepareInsertQuery(ResultSetMetaData metadata, ResultSet  res){
		StringBuilder insertsql = null;
		int columnCount = 0;
		try {
			columnCount = metadata.getColumnCount();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}	
		insertsql = new StringBuilder("insert into t_sub_Logger_info_hist values(");
		for (int i = 2; i <= columnCount; i++) {
			try {
				if (i != columnCount)	{
					//logger.info("timeBasedFileDIGGeneration() : colType =  "+metadata.getColumnType(i) +", columnCount="+columnCount + ", i ="+i);
					if(Types.CHAR == metadata.getColumnType(i) || Types.VARCHAR == metadata.getColumnType(i) ){
						insertsql.append("'");
						insertsql.append(process(res.getString(i)));
						insertsql.append("',");
					}else if(Types.NUMERIC == metadata.getColumnType(i) || Types.INTEGER == metadata.getColumnType(i) || Types.FLOAT == metadata.getColumnType(i)){
						insertsql.append((res.getInt(i)));
						insertsql.append(",");
					}else if(Types.DATE == metadata.getColumnType(i) || Types.TIMESTAMP == metadata.getColumnType(i)){
						insertsql.append("TO_DATE('");
						java.util.Date date = res.getTimestamp(i);
						String dateStr = null;
						SimpleDateFormat frmt = new SimpleDateFormat("dd-MM-yy");
						if(date != null){
							dateStr = frmt.format(date);
						}
						//logger.info("timeBasedFileDIGGeneration() : dateStr"+dateStr);
						insertsql.append(dateStr);
						insertsql.append("','dd-MM-yy'),");
					}


				}	else if (i == columnCount){
					//logger.info("timeBasedFileDIGGeneration() : i == colType CASE , colType=  "+metadata.getColumnType(i) +", columnCount="+columnCount + ", i ="+i);
					if(Types.CHAR == metadata.getColumnType(i) || Types.VARCHAR == metadata.getColumnType(i)){
						insertsql.append("',");
						insertsql.append(process(res.getString(i)));
						insertsql.append("')");
					}else if(Types.NUMERIC == metadata.getColumnType(i) || Types.INTEGER == metadata.getColumnType(i) || Types.FLOAT == metadata.getColumnType(i)){
						insertsql.append((res.getInt(i)));
						insertsql.append(")");
					}else if(Types.DATE == metadata.getColumnType(i)){
						insertsql.append("'");
						insertsql.append(res.getTimestamp(i));
						insertsql.append("')");
					}
				}
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	finally{
				
			}				

			/*else
				insertsql.append(process(res.getString(i)) + ", )");*/
		}
		
		return insertsql.toString();
	}
	
	
	public boolean writeContentToFile( FileWriter writer, String content){
		logger.info("writeContentToFile() : enter");
		boolean writeFlag = false;
		try {
			writer.write(content+ "\n");
			writer.flush();
			writeFlag = true;
			logger.info("writeContentToFile() : writeFlag="+writeFlag);
			return writeFlag;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			writeFlag = false;
			//retrying thrice for writing in file, IF NOT, exit application
			for(int count = 0 ; count <3 && !writeFlag; count++ ){
				writeFlag = writeContentToFile(writer,content);
				if(writeFlag)
					break;
			}

		}finally{
			/*try {
				writer.close();
			} catch (IOException e) {
				
				e.printStackTrace();
			}*/
		}
		
		return writeFlag;
	}
	

	public void sleep(GenerateSdrVO mGenerateSdrVO, int size){
		logger.info("sleep() : enter");
		
		try {
			int sleepTime = mGenerateSdrVO.getSleepTime();
			int sleepMultiplier = mGenerateSdrVO.getSleepTimeMultiplier();
			
			int finalSleepTime = 0;
			 if(sleepMultiplier == -1){
				 finalSleepTime = sleepTime;
			 }else{
				 finalSleepTime = sleepTime * sleepCount; 
			 }
			if(sleepCount == sleepMultiplier)
				sleepCount = 0;
			
			try {
				Thread.sleep(finalSleepTime);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			sleepCount++;
			logger.info("sleep() : exit, thread going to sleep, finalSleepTime="+finalSleepTime);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		
	}
	
}
