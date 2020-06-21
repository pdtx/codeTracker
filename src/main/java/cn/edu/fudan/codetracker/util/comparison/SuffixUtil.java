package cn.edu.fudan.codetracker.util.comparison;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * description:代码比较工具（基于后缀数组）
 *
 * @author fancying
 * create: 2020-06-06 15:47
 **/
public class SuffixUtil {

    /**
     * 判断两端代码是否相似
     * @param code1 代码段1
     * @param code2 代码段2
     * @return
     */
    public static double suffixSimilarity(String code1, String code2){
        try{
            //token化
            List<Byte> tokens1 = lexer(code1);
            List<Byte> tokens2 = lexer(code2);

            //标记片段的token边界
            List<Fragment> fragments = new ArrayList<>();
            fragments.add(new Fragment(0, tokens1.size() - 1));
            fragments.add(new Fragment(tokens1.size(), tokens1.size() + tokens2.size() - 1));

            //后缀数组检测重叠片段
            List<Byte> tokens = new ArrayList<>();
            tokens.addAll(tokens1);
            tokens.addAll(tokens2);
            SuffixArray suffixArray = new SuffixArray();
            suffixArray.init(tokens);
            List<Integer> result = suffixArray.process();

            //处理检测结果
            List<ClonePair> clonePairs = new ArrayList<>();
            for (int i = 0; i < result.size() / 3; i++) {
                if (result.get(3 * i) == 0) {
                    continue;
                }
                int x1 = result.get(3 * i);
                int x2 = result.get(3 * i + 1);
                int cloneLen = result.get(3 * i + 2);
                int firstFrom = searchFragment(fragments, x1);
                int secondFrom = searchFragment(fragments, x2);
                if (firstFrom == secondFrom){
                    continue;
                }
                clonePairs.add(new ClonePair(x1, x2, cloneLen));
            }

            //处理克隆对，计算重叠长度
            Collections.sort(clonePairs, new Comparator<ClonePair>() {
                @Override
                public int compare(ClonePair o1, ClonePair o2) {
                    if (o1.first < o2.first){
                        return -1;
                    }else if (o1.first == o2.first){
                        return 0;
                    }else{
                        return 1;
                    }
                }
            });

            int overlapping = calculateOverlapping(clonePairs);
            int fragment1Size = fragments.get(0).end - fragments.get(0).start + 1;
            int fragment2Size = fragments.get(1).end - fragments.get(1).start + 1;
            double similarity = overlapping * 1d / Math.max(fragment1Size, fragment2Size);
            return similarity;
        }catch (Exception e){
            e.printStackTrace();
        }
        return 0;
    }

    /**
     * 搜索片段的索引
     * @param fragments
     * @param startIndex
     * @return
     */
    private static int searchFragment(List<Fragment> fragments, int startIndex){
        int index = -1;
        for (int i=0; i<fragments.size(); i++) {
            if (startIndex >= fragments.get(i).start && startIndex <= fragments.get(i).end) {
                index = i;
                break;
            }
        }
        return index;
    }

    /**
     * 计算克隆片段的重叠长度
     * @param pairs
     * @return
     */
    private static int calculateOverlapping(List<ClonePair> pairs){
        int index = 0;
        int startToken = 0;
        int size = 0;
        int totalSize = 0;

        while (index < pairs.size()){
            if (index == 0){
                startToken = pairs.get(index).first;
                size = pairs.get(index).size;
                index++;
                continue;
            }
            if (startToken + size >= pairs.get(index).first) {
                if (startToken + size >= pairs.get(index).first + pairs.get(index).size){
                }else{
                    size = pairs.get(index).first - startToken + pairs.get(index).size - 1;
                }
                index++;
            }else{
                totalSize += size;
                startToken = pairs.get(index).first;
                size = pairs.get(index).size;
                index++;
            }
        }
        return Math.max(totalSize, size);
    }

    /**
     * 代码token化方法
     * @param stat
     * @return
     */
    public static List<Byte> lexer(String stat){
        int index = 0;
        List<Byte> res = new ArrayList<>();
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
                res.add(str2hash(token));
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
                res.add(str2hash(token));
                token = "";
                continue;
            }
            res.add(str2hash(c+""));
            index++;
        }
        return res;
    }


    /**
     * 后缀数组工具类
     */
    public static class SuffixArray{
        private List<Byte> tokens;
        private int[] sa;
        private int[] height;

        /**
         * 初始化token列表
         * @param tokens
         */
        public void init(List<Byte> tokens){
            this.tokens = tokens;
            sa = new int[tokens.size()];
            height = new int[tokens.size()];
        }

        /**
         * 构建后缀数组
         */
        private void buildSuffixArray(){
            //获取所有后缀
            List<List<Byte>> tokensList = new ArrayList<>();
            for (int i=0; i<tokens.size(); i++){
                tokensList.add(tokens.subList(i, tokens.size()));
            }

            //对所有后缀排序
            Collections.sort(tokensList, new Comparator<List<Byte>>() {
                @Override
                public int compare(List<Byte> o1, List<Byte> o2) {
                    int size = Math.min(o1.size(), o2.size());
                    int result = (o1.size() < o2.size())? -1: 1;

                    for (int i=0; i<size; i++){
                        if (o1.get(i) < o2.get(i)){
                            result = -1;
                            break;
                        }else if (o1.get(i) > o2.get(i)){
                            result = 1;
                            break;
                        }
                    }
                    return result;
                }
            });

            //计算后缀数组
            for (int i=0; i<tokensList.size(); i++){
                List<Byte> suffix = tokensList.get(i);
                sa[i] = tokens.size() - suffix.size();
            }
            tokensList.clear();
        }

        /**
         * 计算高度数组
         */
        private void calculateHeight(){
            for (int i=1; i<sa.length; i++){
                List<Byte> pre = tokens.subList(sa[i - 1], tokens.size());
                List<Byte> cur = tokens.subList(sa[i], tokens.size());
                int cnt = 0;
                int size = Math.min(pre.size(), cur.size());
                for (int j=0; j<size; j++){
                    if (!pre.get(j).equals(cur.get(j))){
                        break;
                    }
                    cnt++;
                }
                height[i] = cnt;
            }
        }

        /**
         * 获取检测结果
         * @return
         */
        public List<Integer> process(){
            buildSuffixArray();
            calculateHeight();
            List<Integer> results = new ArrayList<>();
            for (int i=1; i<height.length; i++){
                results.add(sa[i - 1]);
                results.add(sa[i]);
                results.add(height[i]);
            }
            return results;
        }
    }

    /**
     * 克隆片段
     */
    public static class Fragment{
        public int start;
        public int end;

        public Fragment(int start, int end){
            this.start = start;
            this.end = end;
        }
    }

    /**
     * 克隆检测结果
     */
    public static class ClonePair{
        public int first;
        public int second;
        public int size;

        public ClonePair(int first, int second, int size) {
            this.first = first;
            this.second = second;
            this.size = size;
        }
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
