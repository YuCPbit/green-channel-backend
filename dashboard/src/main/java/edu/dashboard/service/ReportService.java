package edu.dashboard.service;

import com.alibaba.excel.EasyExcel;
import edu.dashboard.common.FieldWhiteList;
import edu.dashboard.domain.dto.CustomReportReqDTO;
import edu.dashboard.mapper.ReportMapper;
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

        response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        response.setCharacterEncoding("utf-8");
        String fileName = URLEncoder.encode("自定义报表", StandardCharsets.UTF_8).replaceAll("\\+", "%20");
        response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

        // EasyExcel 写数据
        EasyExcel.write(response.getOutputStream())
                .sheet("报表数据")
                .doWrite(data);
    }
}