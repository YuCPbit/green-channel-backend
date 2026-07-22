package edu.greenchannel.dashboard.service;

import com.alibaba.excel.EasyExcel;
import edu.greenchannel.dashboard.common.FieldWhiteList;
import edu.greenchannel.dashboard.domain.dto.CustomReportReqDTO;
import edu.greenchannel.dashboard.mapper.ReportMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

@Service
public class ReportService {

    @Autowired
    private ReportMapper reportMapper;
    @Autowired
    private FieldWhiteList fieldWhiteList;

    /**
     * FR-3.16-001: 生成动态报表数据
     */
    public List<Map<String, Object>> generateReport(CustomReportReqDTO reqDTO) {
        List<String> safeColumns = fieldWhiteList.validateAndGetColumns(reqDTO.getFields());
        return reportMapper.selectDynamicReport(safeColumns, reqDTO.getFilters());
    }

    /**
     * FR-3.16-004: 导出Excel
     */
    public void exportExcel(HttpServletResponse response, CustomReportReqDTO reqDTO) throws IOException {
        List<Map<String, Object>> data = generateReport(reqDTO);

        exportRows(response, reqDTO, data, reqDTO.getFields());
    }

    public void exportRows(HttpServletResponse response, CustomReportReqDTO reqDTO,
                           List<Map<String, Object>> data, List<String> fields) throws IOException {

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String requestedName = reqDTO.getTemplateName();
        String safeName = requestedName == null || requestedName.isBlank() ? "自定义报表" : requestedName;
        safeName = safeName.replaceAll("[\\r\\n/\\\\:*?\"<>|]", "_");
        String fileName = URLEncoder.encode(safeName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        List<List<String>> headers = fields.stream()
                .map(field -> List.of(fieldWhiteList.getChineseHeader(field)))
                .toList();
        List<List<Object>> rows = fieldWhiteList.projectRows(data, fields).stream()
                .map(row -> fields.stream().map(row::get).toList())
                .toList();

        EasyExcel.write(response.getOutputStream())
                .head(headers)
                .sheet("报表数据")
                .doWrite(rows);
    }

}
