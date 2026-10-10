package com.zaynali.acetuner;
final class JSONObjectQuote {
    private JSONObjectQuote() {}
    static String quote(String s) {
        if (s == null) return "\"\"";
        StringBuilder b = new StringBuilder("\"");
        for (int i=0;i<s.length();i++) { char c=s.charAt(i); switch(c) {
            case '"': b.append("\\\""); break; case '\\': b.append("\\\\"); break; case '\n': b.append("\\n"); break;
            case '\r': b.append("\\r"); break; case '\t': b.append("\\t"); break;
            default: if (c < 32) b.append(String.format("\\u%04x", (int)c)); else b.append(c);
        }}
        return b.append('"').toString();
    }
}
