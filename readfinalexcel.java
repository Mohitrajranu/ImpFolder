package com.excel;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Iterator;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.DateUtil;
import org.apache.poi.ss.usermodel.FormulaEvaluator;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.JSONObject;;

public class readfinalexcel {
	
	private static String Empty = "";

	@SuppressWarnings("deprecation")
	public static void main(String[] args) {
		// readexcel method will take file path as a parameter
		readfinalexcel.readExcel("1Invoice3onlytransform2.xlsx");
	}

	@SuppressWarnings("deprecation")
	public static JSONObject readExcel(String fileName) {

		JSONObject finaldataobject = new JSONObject();
		
		String contents = null;

		try {
			
			Iterator<Row> rowIterator = getReadWorkbook(fileName).rowIterator();
			
			while ( rowIterator.hasNext() ) {
				
				Iterator<Cell> cellIterator = rowIterator.next().cellIterator();
				
				while (cellIterator.hasNext()) 
				{
				
					Cell cell = cellIterator.next();
					
					switch (cell.getCellTypeEnum()) {
						case STRING:
							output(cell.getRichStringCellValue().getString());
							break;
						case NUMERIC:
							if (DateUtil.isCellDateFormatted(cell)) {
								output(cell.getDateCellValue()+Empty);
							} else {
								output(cell.getNumericCellValue()+Empty);
							}
							break;
						case BOOLEAN:
							output(cell.getBooleanCellValue()+ Empty);
							break;
						case FORMULA:
				
							switch (cell.getCachedFormulaResultType()) {
								case Cell.CELL_TYPE_NUMERIC:
									if (cell.getCellStyle() != null && cell.getCellStyle().getDataFormatString() != null) {
										contents = new DataFormatter().formatRawCellContents(cell.getNumericCellValue(),
												cell.getCellStyle().getDataFormat(), cell.getCellStyle().getDataFormatString());
										output("Content s: " + contents);
									}
									output("0. case Cell.CELL_TYPE_NUMERIC --> Last evaluated as: "
											+ cell.getNumericCellValue());
									break;
								case Cell.CELL_TYPE_STRING:
									output("4. case Cell.CELL_TYPE_STRING --> Last evaluated as \""
											+ cell.getRichStringCellValue() + "\"");
									break;
								case Cell.CELL_TYPE_ERROR:
									output("5. case Cell.CELL_TYPE_ERROR --> ");
									break;
								}
							break;
						case BLANK:
							output("");
							break;
						default:
							output("default");
						}
				
				  }
			 }
		}
		catch (IOException ioException) {

			System.out.println("Exception in reading excel : " + ioException.getMessage());
		}
		catch (Exception exception) {

			System.out.println("Exception ex : " + exception.getMessage());
		}
		return finaldataobject;

	}
	
	private static XSSFSheet getReadWorkbook(String fileName) throws IOException {
		InputStream ExcelFileToRead = new FileInputStream(fileName);
		Workbook workBook = new XSSFWorkbook(ExcelFileToRead);
		setFormulaEvaluator(workBook);
		
		return getSheet(workBook);
	}
	
	private static void setFormulaEvaluator(Workbook workBook) {
		FormulaEvaluator evaluator = workBook.getCreationHelper().createFormulaEvaluator();
		workBook.setForceFormulaRecalculation(true);
	}
	
	private static XSSFSheet getSheet(Workbook workBook) {
		return (XSSFSheet) workBook.getSheetAt(0);
	}
	
	private static void output(String value) {
		System.out.println(value);
	}
}