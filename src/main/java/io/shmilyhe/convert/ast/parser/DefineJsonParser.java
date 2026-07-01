package io.shmilyhe.convert.ast.parser;

public class DefineJsonParser {


    /**
     * 从源码中抽取 jsonline 的初始化定义
     * @param source
     * @return
     */
   public static String findDefineJsonLine(String source) {
    int i = 0, lineStart = 0, firstNonWhite = -1;
    final String DEFINE = "#DEFINE";
    final String JSONLINE = "JSONLINE:";

    while (i < source.length()) {
        char c = source.charAt(i);
        if (c == '\r' || c == '\n') {
            // 当前行范围 [lineStart, i)
            if (firstNonWhite != -1) { // 存在非空白字符
                if (source.charAt(firstNonWhite) != '#') {
                    break; // 非 '#' 开头的有效行 → 终止
                }
                // 检查是否以 "#DEFINE" 开头（从 firstNonWhite 开始）
                if (i - firstNonWhite >= DEFINE.length() && source.startsWith(DEFINE, firstNonWhite)) {
                    int pos = firstNonWhite + DEFINE.length();
                    // 跳过 DEFINE 后的空白
                    while (pos < i && Character.isWhitespace(source.charAt(pos))) {
                        pos++;
                    }
                    if (pos + JSONLINE.length() <= i && source.startsWith(JSONLINE, pos)) {
                        return source.substring(pos + JSONLINE.length(), i).trim();
                    }
                }
            }
            // 空行或全空白行 → 不跳出，继续下一行

            // 跳过换行符（兼容 \r\n）
            if (c == '\r' && i + 1 < source.length() && source.charAt(i + 1) == '\n') {
                i++;
            }
            i++;
            lineStart = i;
            firstNonWhite = -1; // 重置新行的标志
        } else {
            // 记录第一个非空白字符的位置
            if (firstNonWhite == -1 && !Character.isWhitespace(c)) {
                firstNonWhite = i;
            }
            i++;
        }
    }

    // 处理最后一行（可能没有换行符）
    if (firstNonWhite != -1) {
        if (source.charAt(firstNonWhite) != '#') {
            return null;
        }
        if (source.startsWith(DEFINE, firstNonWhite)) {
            int pos = firstNonWhite + DEFINE.length();
            while (pos < source.length() && Character.isWhitespace(source.charAt(pos))) {
                pos++;
            }
            if (pos + JSONLINE.length() <= source.length() && source.startsWith(JSONLINE, pos)) {
                return source.substring(pos + JSONLINE.length()).trim();
            }
        }
    }
    return null;
}

    // 使用示例
    public static void main(String[] args) {
        {
        String content = "#dDEFINE JSONLINE:{\"name\":\"Alice\"} \r\n"+
                "#DEFINE     JSONLINE:{\"age\":30}\r\n"+
                "# Other comment\r\n"+
                "#DEFINE JSONLINE:{\"city\":\"NY\"}  \r\n";
            
        // 注意：上面内容中 "# Other comment" 以 # 开头但不含 DEFINE，会被跳过；
        // 但下一行以 # 开头且含 DEFINE，会继续；直到遇到非 # 行才停止。
        // 实际停止是在遇到非 # 行时，若没有，则处理完所有行。

        String jsons = findDefineJsonLine(content);
        System.out.println(jsons);
        }
        {
        String content = "#dDEFINE JSONLINE:{\"name\":\"Alice\"} \r\n"+
                "\r\n"+
                "    \r\n"+
                "#DEFINE     JSONLINE:{\"age\":30}\r\n"+
                "# Other comment\r\n"+
                "#DEFINE JSONLINE:{\"city\":\"NY\"}  \r\n";
            
        // 注意：上面内容中 "# Other comment" 以 # 开头但不含 DEFINE，会被跳过；
        // 但下一行以 # 开头且含 DEFINE，会继续；直到遇到非 # 行才停止。
        // 实际停止是在遇到非 # 行时，若没有，则处理完所有行。

        String jsons = findDefineJsonLine(content);
        System.out.println(jsons);
        }
    }
}