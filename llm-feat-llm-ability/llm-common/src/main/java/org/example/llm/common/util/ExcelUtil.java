package org.example.llm.common.util;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.reflect.Field;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
public class ExcelUtil {

    public static void gen(String file, String sheetName, List<String> headers, List<List<String>> data) {
        // 创建工作簿和工作表
        try (Workbook workbook = new XSSFWorkbook()) {
            Sheet sheet = workbook.createSheet(sheetName);

            // 创建表头行
            Row headerRow = sheet.createRow(0);
            //String[] headers = {"ID", "姓名", "部门", "职位", "薪水"};
            for (int i = 0; i < headers.size(); i++) {
                Cell cell = headerRow.createCell(i);
                cell.setCellValue(headers.get(i));
            }

            // 创建样式（可选）
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerFont.setFontHeightInPoints((short) 12);
            headerStyle.setFont(headerFont);
            headerStyle.setFillForegroundColor(IndexedColors.LIGHT_GREEN.getIndex());
            headerStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            for (int i = 0; i < headers.size(); i++) {
                headerRow.getCell(i).setCellStyle(headerStyle);
            }


            int rowNum = 1;
            for (List<String> rowData : data) {
                Row row = sheet.createRow(rowNum++);
                int cellNum = 0;
                for (Object field : rowData) {
                    Cell cell = row.createCell(cellNum++);
                    if (field instanceof String) {
                        cell.setCellValue((String) field);
                    } else if (field instanceof Integer) {
                        cell.setCellValue((Integer) field);
                    } else if (field instanceof Double) {
                        cell.setCellValue((Double) field);
                    }
                }
            }

            // 自动调整列宽
            for (int i = 0; i < headers.size(); i++) {
                sheet.autoSizeColumn(i);
            }

            // 写入文件
            try (FileOutputStream fileOut = new FileOutputStream(file)) {
                workbook.write(fileOut);
                System.out.println("Excel 文件已成功生成！");
            }

        } catch (IOException e) {
            log.error("导出excel异常", e);
        }
    }


    public static <T> List<T> readExcel(File file, Class<T> clazz) throws IOException, ReflectiveOperationException {
        List<T> resultList = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return resultList;
            }

            // 读取表头，建立表头与类字段的映射
            Row headerRow = sheet.getRow(0);
            Map<Integer, String> columnFieldMap = getColumnFieldMap(headerRow, clazz);

            // 从第2行(index=1)开始读取数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                T obj = clazz.getDeclaredConstructor().newInstance();
                for (Map.Entry<Integer, String> entry : columnFieldMap.entrySet()) {
                    int columnIndex = entry.getKey();
                    String fieldName = entry.getValue();

                    Cell cell = row.getCell(columnIndex);
                    Object cellValue = getCellValue(cell);

                    if (cellValue != null) {
                        setFieldValue(obj, fieldName, cellValue);
                    }
                }
                resultList.add(obj);
            }
        }
        return resultList;
    }


    public static <T> List<T> readExcelCrossRow(File file, Class<T> clazz) throws IOException, ReflectiveOperationException {
        List<T> resultList = new ArrayList<>();
        try (FileInputStream fis = new FileInputStream(file);
             Workbook workbook = new XSSFWorkbook(fis)) {

            Sheet sheet = workbook.getSheetAt(0);
            if (sheet == null) {
                return resultList;
            }

            // 读取表头，建立表头与类字段的映射
            Row headerRow = sheet.getRow(0);
            Map<Integer, String> columnFieldMap = getColumnFieldMap(headerRow, clazz);

            // 从第2行(index=1)开始读取数据
            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                Row row = sheet.getRow(i);
                if (row == null) {
                    continue;
                }

                T obj = clazz.getDeclaredConstructor().newInstance();
                for (Map.Entry<Integer, String> entry : columnFieldMap.entrySet()) {
                    int columnIndex = entry.getKey();
                    String fieldName = entry.getValue();

                    Cell cell = row.getCell(columnIndex);
                    Object cellValue = getCellValue(cell);

                    // 如果单元格为空，尝试获取上一行的值
                    if (cellValue == null && i > 1) {
                        Row previousRow = sheet.getRow(1);
                        if (previousRow != null) {
                            Cell previousCell = previousRow.getCell(columnIndex);
                            cellValue = getCellValue(previousCell);
                        }
                    }

                    if (cellValue != null) {
                        setFieldValue(obj, fieldName, cellValue);
                    }
                }
                resultList.add(obj);
            }
        }
        return resultList;
    }

    private static Map<Integer, String> getColumnFieldMap(Row headerRow, Class<?> clazz) {
        Map<Integer, String> map = new HashMap<>();
        if (headerRow == null) {
            return map;
        }

        // 获取类的所有字段
        Field[] fields = clazz.getDeclaredFields();
        Map<String, Field> fieldMap = new HashMap<>();
        for (Field field : fields) {
            fieldMap.put(field.getName().toLowerCase(), field);
        }

        // 建立列索引与字段的映射
        for (int i = 0; i <= headerRow.getLastCellNum(); i++) {
            Cell cell = headerRow.getCell(i);
            if (cell == null) {
                continue;
            }

            String headerValue = cell.getStringCellValue().trim().toLowerCase();
            if (fieldMap.containsKey(headerValue)) {
                map.put(i, fieldMap.get(headerValue).getName());
            }
        }
        return map;
    }

    private static Object getCellValue(Cell cell) {
        if (cell == null) {
            return null;
        }

        CellType cellType = cell.getCellType();
        switch (cellType) {
            case STRING:
                return cell.getStringCellValue();
            case NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    return cell.getDateCellValue();
                } else {
                    return cell.getNumericCellValue();
                }
            case BOOLEAN:
                return cell.getBooleanCellValue();
            case FORMULA:
                return cell.getCellFormula();
            case BLANK:
                return null;
            default:
                return cell.toString();
        }
    }

    private static void setFieldValue(Object obj, String fieldName, Object value)
            throws NoSuchFieldException, IllegalAccessException {
        Field field = obj.getClass().getDeclaredField(fieldName);
        field.setAccessible(true);

        // 类型转换
        Class<?> fieldType = field.getType();
        Object convertedValue = convertValue(value, fieldType);
        field.set(obj, convertedValue);
    }

    private static Object convertValue(Object value, Class<?> targetType) {
        if (value == null || targetType.isAssignableFrom(value.getClass())) {
            return value;
        }

        String strValue = value.toString().trim();
        if (strValue.isEmpty()) {
            return null;
        }

        try {
            if (targetType == String.class) {
                return strValue;
            } else if (targetType == int.class || targetType == Integer.class) {
                return Integer.parseInt(strValue);
            } else if (targetType == long.class || targetType == Long.class) {
                return Long.parseLong(strValue);
            } else if (targetType == double.class || targetType == Double.class) {
                return Double.parseDouble(strValue);
            } else if (targetType == boolean.class || targetType == Boolean.class) {
                // 支持多种布尔值表示方式
                if (strValue.equalsIgnoreCase("true") ||
                        strValue.equalsIgnoreCase("yes") ||
                        strValue.equals("1")) {
                    return true;
                } else if (strValue.equalsIgnoreCase("false") ||
                        strValue.equalsIgnoreCase("no") ||
                        strValue.equals("0")) {
                    return false;
                }
                throw new IllegalArgumentException("Invalid boolean value: " + strValue);
            } else if (targetType == Date.class) {
                // 简单日期格式处理，实际应用中可能需要更复杂的逻辑
                try {
                    return new SimpleDateFormat("yyyy-MM-dd").parse(strValue);
                } catch (ParseException e) {
                    try {
                        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(strValue);
                    } catch (ParseException ex) {
                        throw new IllegalArgumentException("Invalid date format: " + strValue);
                    }
                }
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException(
                    "Cannot convert '" + strValue + "' to " + targetType.getName(), e);
        }

        throw new IllegalArgumentException("Unsupported target type: " + targetType.getName());
    }
}
