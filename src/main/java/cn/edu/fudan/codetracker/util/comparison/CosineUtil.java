package cn.edu.fudan.codetracker.util.comparison;

import java.util.*;

/**
 * description:代码比较工具（基于余弦相似度）
 *
 * @author fancying
 * create: 2020-06-06 15:48
 **/
public class CosineUtil {


    public static double codeSimilarity(List<Object> tokens1, String code2, boolean tokenize){
        try {
            List<Object> tokens2 = lexer(code2, tokenize);
            return cosineSimilarity(tokens1, tokens2) ;
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 判断两段代码是否相似
     * @param code1 代码段1
     * @param code2 代码段2
     * @return threshold 相似阈值
     */
    public static double cosineSimilarity(String code1, String code2){
        try {
            List<Object> tokens1 = lexer(code1, true);
            List<Object> tokens2 = lexer(code2, true);
            return cosineSimilarity(tokens1, tokens2);
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 判断两段代码是否相似
     * @param code1 代码段1
     * @param code2 代码段2
     * @return threshold 相似阈值
     */
    public static double cosineSimilarityWithoutTokenize(String code1, String code2){
        try {
            List<Object> tokens1 = lexer(code1, false);
            List<Object> tokens2 = lexer(code2, false);
            return cosineSimilarity(tokens1, tokens2);
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }


    /**
     * 计算token串的余弦相似度
     * @param tokensX
     * @param tokensY
     * @return
     */
    public static double cosineSimilarity(List<Object> tokensX, List<Object> tokensY){
        List<Object> allTokens = new ArrayList<>();
        allTokens.addAll(tokensX);
        allTokens.addAll(tokensY);
        Set<Object> tokenSet = new HashSet<>(allTokens);
        Map<Object, Integer> tokenMapX = new HashMap<>();
        Map<Object, Integer> tokenMapY = new HashMap<>();
        for (Object b: tokensX) {
            tokenMapX.put(b, tokenMapX.getOrDefault(b, 0) + 1);
        }
        for (Object b: tokensY) {
            tokenMapY.put(b, tokenMapY.getOrDefault(b, 0) + 1);
        }
        List<Integer> vecX = new ArrayList<>();
        List<Integer> vecY = new ArrayList<>();
        for (Object b: tokenSet) {
            vecX.add(tokenMapX.getOrDefault(b, 0));
            vecY.add(tokenMapY.getOrDefault(b, 0));
        }

        long x=0, y=0, xy=0;
        for (int i=0; i<tokenSet.size(); i++) {
            xy += vecX.get(i) * vecY.get(i);
            x += vecX.get(i) * vecX.get(i);
            y += vecY.get(i) * vecY.get(i);
        }
        return xy/(Math.sqrt(x) * Math.sqrt(y));
    }

    /**
     * 代码token化方法
     * @param stat
     * @return
     */
    public static List<Object> lexer(String stat, boolean tokenize){
        int index = 0;
        List<Object> res = new ArrayList<>();
        String token = "";
        while (index < stat.length()){
            char c = stat.charAt(index);
            if (Character.isSpaceChar(c)){
                index++;
                continue;
            }
            if (Character.isDigit(c)){
                while (Character.isDigit(c)){
                    token += c;
                    if (++index >= stat.length()) {
                        break;
                    }
                    c = stat.charAt(index);
                }
                if (tokenize) {
                    res.add(str2hash(token));
                }else{
                    res.add(token);
                }
                token = "";
                continue;
            }
            if (Character.isLetter(c) || c == '_'){
                while (Character.isLetterOrDigit(c) || c == '_'){
                    token += c;
                    if (++index >= stat.length()) {
                        break;
                    }
                    c = stat.charAt(index);
                }
                if (tokenize) {
                    res.add(str2hash(token));
                }else{
                    res.add(token);
                }
                token = "";
                continue;
            }
            index++;
        }
        return res;
    }


    /**
     * 模糊匹配 一个标识符后三个字符是一样的，那么这么标识符就会映射到同一个token
     * 哈希函数，将字符串映射到[-128,-3]u[125,127]字节空间
     * @param str
     * @return
     */
    private static byte str2hash(String str) {
        str = str.toLowerCase();
        if (str.length() < 2) {
            int h = str.toCharArray()[str.length() - 1];
            h <<= 1;
            return (byte) (-3 - (h & 0x7f));
        } else {
            int h1 = str.toCharArray()[str.length() - 1];
            int h2 = str.toCharArray()[str.length() - 2];
            h1 <<= 1;
            int h = h1 ^ h2;
            return (byte) (-3 - (h & 0x7f));
        }
    }

//    /**
//     * 基于token 在父字符串中 删除 子字符串
//     * @param token1 父token
//     * @param token2 子token
//     */
//    public static List<Object> diff(List<Object> token1, List<Object> token2) {
//        String delimiter = ",";
//        StringBuilder s1 = new StringBuilder();
//        StringBuilder s2 = new StringBuilder();
//        token1.forEach(s -> s1.append(s).append(delimiter));
//        token2.forEach(s -> s2.append(s).append(delimiter));
//
//        String p = s1.toString();
//        String c = s2.toString();
//        String d = p.replace(c,"");
//
//        return Arrays.asList(d.split(","));
//    }

//    public static List<Object> diffToken(String code) {
//        String delimiter = ",";
//        List<Object> token1 = lexer(code, true);
//        StringBuilder s1 = new StringBuilder();
//        token1.forEach(s -> s1.append(s).append(delimiter));
//        return Arrays.asList(s1.toString().split(","));
//    }

    public static String diffBody(String code) {
        code = removeComment(code);
        String delimiter = ",";
        List<Object> token1 = lexer(code, true);
        StringBuilder s1 = new StringBuilder();
        token1.forEach(s -> s1.append(s).append(delimiter));
        return s1.toString();
    }

    public static String diff(String set, String subset) {
        return set.replace(subset, "");
    }

    /**
     * * 处理注释 groovy代码
     * @param code
     * @return String
     * */
    public static String removeComment(String code) {
        return code.replaceAll("(?<!:)\\/\\/.*|\\/\\*(\\s|.)*?\\*\\/", "");
    }

}
