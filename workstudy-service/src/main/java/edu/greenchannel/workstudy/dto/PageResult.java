package edu.greenchannel.workstudy.dto;

import com.baomidou.mybatisplus.core.metadata.IPage;
import lombok.Data;

import java.util.List;

/**
 * 分页结果 — 将 MyBatis-Plus IPage 转换为前端统一的 Page 格式
 * 前端期望: { content, totalElements, totalPages, number, size }
 */
@Data
public class PageResult<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int number;
    private int size;

    public static <T> PageResult<T> of(IPage<T> page) {
        PageResult<T> result = new PageResult<>();
        result.setContent(page.getRecords());
        result.setTotalElements(page.getTotal());
        result.setTotalPages((int) page.getPages());
        result.setNumber((int) page.getCurrent());
        result.setSize((int) page.getSize());
        return result;
    }
}
