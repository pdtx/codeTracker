package cn.edu.fudan.codetracker.util.comparison;

import java.util.*;

/**
 * description:代码比较工具（基于余弦相似度）
 *
 * @author fancying
 * create: 2020-06-06 15:48
 **/
public class CosineUtils {

    /**
     * 判断两段代码是否相似
     * @param code1 代码段1
     * @param code2 代码段2
     * @param tokenize 是否token化
     * @param threshold 相似阈值
     * @return
     */
    public static boolean isSimilarCode(String code1, String code2, boolean tokenize, double threshold){
        try {
            List<Object> tokens1 = lexer(code1, tokenize);
            List<Object> tokens2 = lexer(code2, tokenize);
            double similarity = cosineSimilarity(tokens1, tokens2);
            return similarity >= threshold;
        }catch (Exception e){
            e.printStackTrace();
        }
        return false;
    }

    /**
     * 计算token串的余弦相似度
     * @param tokensX
     * @param tokensY
     * @return
     */
    private static double cosineSimilarity(List<Object> tokensX, List<Object> tokensY){
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
}
