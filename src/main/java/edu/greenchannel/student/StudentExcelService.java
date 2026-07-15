package edu.greenchannel.student;

import edu.greenchannel.common.BusinessException;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Component
public class StudentExcelService {
    private static final long MAX_FILE_SIZE = 20L * 1024 * 1024;
    private static final int MAX_ROWS = 50_000;
    private static final List<String> HEADERS = List.of(
            "学号", "姓名", "性别", "身份证号", "手机号", "邮箱",
            "入学年份", "学院ID", "专业ID", "班级ID", "学生类型");

    public List<ImportedRow> read(MultipartFile file) {
        validateFile(file);
        try (Workbook workbook = WorkbookFactory.create(file.getInputStream())) {
            Sheet sheet = workbook.getNumberOfSheets() == 0 ? null : workbook.getSheetAt(0);
            if (sheet == null || sheet.getPhysicalNumberOfRows() == 0) {
                throw new BusinessException(40001, "Excel 中没有数据");
            }
            validateHeaders(sheet.getRow(0));
            if (sheet.getLastRowNum() > MAX_ROWS) {
                throw new BusinessException(40001, "单次导入不能超过 " + MAX_ROWS + " 行");
            }
            DataFormatter formatter = new DataFormatter();
            List<ImportedRow> rows = new ArrayList<>();
            for (int index = 1; index <= sheet.getLastRowNum(); index++) {
                Row row = sheet.getRow(index);
                if (row == null || isEmpty(row, formatter)) {
                    continue;
                }
                try {
                    rows.add(new ImportedRow(index + 1, new StudentRequest(
                            text(row, 0, formatter), text(row, 1, formatter), gender(text(row, 2, formatter)),
                            text(row, 3, formatter), text(row, 4, formatter), text(row, 5, formatter),
                            integer(text(row, 6, formatter)), longValue(text(row, 7, formatter)),
                            longValue(text(row, 8, formatter)), nullableLong(text(row, 9, formatter)),
                            defaultText(text(row, 10, formatter), "本科")), null));
                } catch (NumberFormatException exception) {
                    rows.add(new ImportedRow(index + 1, null, "年份或组织ID必须是整数"));
                }
            }
            return rows;
        } catch (BusinessException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new BusinessException(40001, "Excel 文件无法解析，请使用标准模板");
        }
    }

    public byte[] template() {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("新生导入");
            Row header = sheet.createRow(0);
            for (int index = 0; index < HEADERS.size(); index++) {
                header.createCell(index).setCellValue(HEADERS.get(index));
                sheet.setColumnWidth(index, index == 3 ? 22 * 256 : 15 * 256);
            }
            Row sample = sheet.createRow(1);
            String[] values = {
                    "20260001", "示例学生", "女", "000000200001010021", "13800000000",
                    "student@example.invalid", "2026", "1", "1", "1", "本科"};
            for (int index = 0; index < values.length; index++) {
                sample.createCell(index).setCellValue(values[index]);
            }
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("无法生成导入模板", exception);
        }
    }

    public byte[] errorReport(List<StudentImportError> errors) {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream output = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("导入错误");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("原Excel行号");
            header.createCell(1).setCellValue("学号");
            header.createCell(2).setCellValue("错误原因");
            for (int index = 0; index < errors.size(); index++) {
                StudentImportError error = errors.get(index);
                Row row = sheet.createRow(index + 1);
                row.createCell(0).setCellValue(error.rowNumber());
                row.createCell(1).setCellValue(error.studentNo());
                row.createCell(2).setCellValue(error.reason());
            }
            sheet.setColumnWidth(0, 15 * 256);
            sheet.setColumnWidth(1, 20 * 256);
            sheet.setColumnWidth(2, 45 * 256);
            workbook.write(output);
            return output.toByteArray();
        } catch (IOException exception) {
            throw new IllegalStateException("无法生成错误报告", exception);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(40001, "请选择 Excel 文件");
        }
        String name = file.getOriginalFilename();
        if (name == null || !name.toLowerCase().endsWith(".xlsx")) {
            throw new BusinessException(40001, "仅支持 .xlsx 文件");
        }
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BusinessException(40001, "Excel 文件不能超过 20MB");
        }
    }

    private void validateHeaders(Row row) {
        if (row == null) {
            throw new BusinessException(40001, "Excel 表头缺失");
        }
        DataFormatter formatter = new DataFormatter();
        for (int index = 0; index < HEADERS.size(); index++) {
            if (!HEADERS.get(index).equals(text(row, index, formatter))) {
                throw new BusinessException(40001, "Excel 表头不正确，请下载标准模板");
            }
        }
    }

    private boolean isEmpty(Row row, DataFormatter formatter) {
        for (int index = 0; index < HEADERS.size(); index++) {
            if (!text(row, index, formatter).isBlank()) {
                return false;
            }
        }
        return true;
    }

    private String text(Row row, int index, DataFormatter formatter) {
        Cell cell = row.getCell(index, Row.MissingCellPolicy.RETURN_BLANK_AS_NULL);
        return cell == null ? "" : formatter.formatCellValue(cell).trim();
    }

    private Integer gender(String value) {
        return switch (value) {
            case "男", "1" -> 1;
            case "女", "2" -> 2;
            case "" -> null;
            default -> -1;
        };
    }

    private Integer integer(String value) {
        return value.isBlank() ? null : Integer.valueOf(value.replace(".0", ""));
    }

    private Long longValue(String value) {
        return value.isBlank() ? null : Long.valueOf(value.replace(".0", ""));
    }

    private Long nullableLong(String value) {
        return longValue(value);
    }

    private String defaultText(String value, String defaultValue) {
        return value.isBlank() ? defaultValue : value;
    }

    public record ImportedRow(int rowNumber, StudentRequest request, String parseError) {
    }
}
