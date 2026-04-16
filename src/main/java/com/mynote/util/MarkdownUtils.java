package com.mynote.util;

import com.vladsch.flexmark.html.HtmlRenderer;
import com.vladsch.flexmark.parser.Parser;
import com.vladsch.flexmark.util.data.MutableDataSet;

public class MarkdownUtils {
    
    private static final Parser PARSER;
    private static final HtmlRenderer RENDERER;
    
    static {
        MutableDataSet options = new MutableDataSet();
        options.set(Parser.EXTENSIONS, java.util.Arrays.asList(
            com.vladsch.flexmark.ext.tables.TablesExtension.create(),
            com.vladsch.flexmark.ext.gfm.strikethrough.StrikethroughExtension.create(),
            com.vladsch.flexmark.ext.gfm.tasklist.TaskListExtension.create(),
            com.vladsch.flexmark.ext.emoji.EmojiExtension.create(),
            com.vladsch.flexmark.ext.autolink.AutolinkExtension.create()
        ));
        
        PARSER = Parser.builder(options).build();
        RENDERER = HtmlRenderer.builder(options).build();
    }
    
    /**
     * Markdown 转 HTML，包装为完整文档样式
     */
    public static String markdownToHtml(String markdown, String title) {
        String bodyHtml = RENDERER.render(PARSER.parse(markdown));
        
        return "<!DOCTYPE html>\n" +
               "<html>\n" +
               "<head>\n" +
               "    <meta charset=\"UTF-8\"/>\n" +
               "    <title>" + escapeHtml(title) + "</title>\n" +
               "    <style>\n" +
               "        body { font-family: 'Source Han Sans SC', 'SimSun', '宋体', sans-serif; margin: 40px; line-height: 1.6; }\n" +
               "        h1 { border-bottom: 2px solid #eee; padding-bottom: 10px; }\n" +
               "        h2 { margin-top: 24px; border-bottom: 1px solid #eee; padding-bottom: 8px; }\n" +
               "        pre { background-color: #f5f5f5; padding: 15px; border-radius: 4px; overflow-x: auto; }\n" +
               "        code { background-color: #f5f5f5; padding: 2px 6px; border-radius: 3px; font-family: 'Courier New', monospace; }\n" +
               "        blockquote { border-left: 4px solid #ddd; margin: 0; padding-left: 20px; color: #666; }\n" +
               "        table { border-collapse: collapse; width: 100%; margin: 16px 0; }\n" +
               "        th, td { border: 1px solid #ddd; padding: 8px 12px; text-align: left; }\n" +
               "        th { background-color: #f5f5f5; }\n" +
               "        img { max-width: 100%; height: auto; }\n" +
               "    </style>\n" +
               "</head>\n" +
               "<body>\n" +
               "    <h1>" + escapeHtml(title) + "</h1>\n" +
               bodyHtml + "\n" +
               "</body>\n" +
               "</html>";
    }
    
    private static String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }
}