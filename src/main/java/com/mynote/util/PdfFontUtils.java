package com.mynote.util;

import com.itextpdf.html2pdf.ConverterProperties;
import com.itextpdf.html2pdf.resolver.font.DefaultFontProvider;
import com.itextpdf.io.font.PdfEncodings;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.layout.font.FontProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;

import java.io.InputStream;

@Slf4j
public class PdfFontUtils {

    private static FontProvider fontProvider;

    public static ConverterProperties getConverterProperties() {
        ConverterProperties properties = new ConverterProperties();
        properties.setFontProvider(getFontProvider());
        return properties;
    }

    private static FontProvider getFontProvider() {
        if (fontProvider != null) {
            return fontProvider;
        }
        
        fontProvider = new DefaultFontProvider(true, true, true);
        
        try {
            // 方式1：直接通过 ClassPathResource 获取字节数组创建字体
            String[] fontPaths = {
                "fonts/SourceHanSansSC-Regular.otf",
                "fonts/SourceHanSerifSC-Regular.otf"
            };
            
            for (String path : fontPaths) {
                try (InputStream is = new ClassPathResource(path).getInputStream()) {
                    // ✅ 新版 API：直接使用 byte[] 创建字体
                    byte[] fontBytes = is.readAllBytes();
                    PdfFont font = PdfFontFactory.createFont(fontBytes, 
                                        PdfEncodings.IDENTITY_H, 
                                        PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                    fontProvider.addFont(font.getFontProgram(), PdfEncodings.IDENTITY_H);
                } catch (Exception e) {
                    log.debug("字体文件 {} 加载失败: {}", path, e.getMessage());
                }
            }
            
            // 兜底：加载系统字体
            loadSystemFallbackFonts();
            
        } catch (Exception e) {
            log.error("中文字体初始化失败", e);
            loadSystemFallbackFonts();
        }
        
        return fontProvider;
    }

    private static void loadSystemFallbackFonts() {
        try {
            // Windows: 宋体
            PdfFont font = PdfFontFactory.createFont("c:/windows/fonts/simsun.ttc,0", 
                                PdfEncodings.IDENTITY_H,
                                PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
            fontProvider.addFont(font.getFontProgram(), PdfEncodings.IDENTITY_H);
            log.info("已加载 Windows 宋体作为中文字体");
        } catch (Exception e) {
            try {
                // Linux: 文泉驿
                PdfFont font = PdfFontFactory.createFont("/usr/share/fonts/truetype/wqy/wqy-microhei.ttc",
                                    PdfEncodings.IDENTITY_H,
                                    PdfFontFactory.EmbeddingStrategy.PREFER_EMBEDDED);
                fontProvider.addFont(font.getFontProgram(), PdfEncodings.IDENTITY_H);
                log.info("已加载文泉驿字体作为中文字体");
            } catch (Exception ex) {
                log.warn("无法加载任何中文字体，PDF中文将显示为空白");
            }
        }
    }
}