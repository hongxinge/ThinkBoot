package com.thinkboot.web.utils;

import cn.hutool.poi.excel.ExcelReader;
import cn.hutool.poi.excel.ExcelUtil;
import cn.hutool.poi.excel.ExcelWriter;
import org.springframework.web.multipart.MultipartFile;

import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

public class ExcelUtils {

    public static <T> List<T> readExcel(MultipartFile file, Class<T> clazz) {
        try (InputStream is = file.getInputStream()) {
            ExcelReader reader = ExcelUtil.getReader(is);
            return reader.readAll(clazz);
        } catch (IOException e) {
            throw new RuntimeException("读取 Excel 失败", e);
        }
    }

    public static List<Map<String, Object>> readExcelAsMap(MultipartFile file) {
        try (InputStream is = file.getInputStream()) {
            ExcelReader reader = ExcelUtil.getReader(is);
            return reader.readAll();
        } catch (IOException e) {
            throw new RuntimeException("读取 Excel 失败", e);
        }
    }

    public static void writeExcel(HttpServletResponse response, List<?> data, String fileName) {
        try (OutputStream os = response.getOutputStream()) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedName + ".xlsx");

            ExcelWriter writer = ExcelUtil.getWriter(true);
            writer.write(data, true);
            writer.flush(os);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("导出 Excel 失败", e);
        }
    }

    public static void writeExcel(HttpServletResponse response, List<?> data, List<String> headers, String fileName) {
        try (OutputStream os = response.getOutputStream()) {
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            String encodedName = URLEncoder.encode(fileName, StandardCharsets.UTF_8).replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + encodedName + ".xlsx");

            ExcelWriter writer = ExcelUtil.getWriter(true);
            for (int i = 0; i < headers.size(); i++) {
                writer.addHeaderAlias(String.valueOf(i), headers.get(i));
            }
            writer.write(data, true);
            writer.flush(os);
            writer.close();
        } catch (IOException e) {
            throw new RuntimeException("导出 Excel 失败", e);
        }
    }
}