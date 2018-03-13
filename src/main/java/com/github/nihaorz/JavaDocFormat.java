package com.github.nihaorz;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Nihaorz
 */
public class JavaDocFormat {

    private static Set<String> excludeSet = new HashSet<String>(Arrays.asList(new String[]{"package-frame.html", "package-summary.html", "package-tree.html"}));
    private static final String WRAP_STR = "\r\n";
    private static int classCount = 0;
    private static int methodCount = 0;
    private static int responseBodyCount = 0;
    private static int requestMappingCount = 0;

    public static void main(String[] args) throws Exception {
        String parentPath = "C:\\Users\\Nihaorz\\Desktop\\OperationCenter_doc\\com";
        File folder = new File(parentPath);
        List<String> list = new ArrayList<String>();
        getAllFile(folder, list);
        classCount = list.size();
        for (String s : list) {
            File file = new File(s);
            formatFile(file);
        }
        System.out.println("classCount:" + classCount);
        System.out.println("methodCount:" + methodCount);
        System.out.println("responseBodyCount:" + responseBodyCount);
        System.out.println("requestMappingCount:" + requestMappingCount);
    }

    /**
     * 格式化文件
     *
     * @param file
     */
    private static void formatFile(File file) throws IOException {
        String html = FileUtils.readFileToString(file, "UTF-8");
        Document doc = Jsoup.parse(html);
        doc.outputSettings().prettyPrint(false);
        Elements elements = doc.select("a[name=method.detail]");
        if (elements.size() > 0) {
            Element a = elements.get(0);
            Elements pres = a.parent().select("li.blockList pre");
            if (pres.size() > 0) {
                for (Element pre : pres) {
                    String result;
                    int sum = 0;
                    methodCount += elements.size();
                    String text = pre.text();
                    if (text.indexOf("@ResponseBody") > -1) {
                        responseBodyCount++;
                        sum++;
                    }
                    if (text.indexOf("@RequestMapping") > -1) {
                        requestMappingCount++;
                        sum++;
                    }
                    text = text.replace(" @RequestMapping", "@RequestMapping");
                    text = text.replace(" @ResponseBody", "@ResponseBody");
                    if (sum == 2) {
                        int index = text.indexOf(WRAP_STR);
                        index = text.indexOf(WRAP_STR, index + 1);
                        String str1 = text.substring(0, index);
                        String str2 = text.substring(index + WRAP_STR.length(), text.length());
                        str2 = formatMain(str2);
                        result = str1 + WRAP_STR + str2;
                    } else if (sum == 1) {
                        int index = text.indexOf(WRAP_STR);
                        String str1 = text.substring(0, index);
                        String str2 = text.substring(index + WRAP_STR.length(), text.length());
                        str2 = formatMain(str2);
                        result = str1 + WRAP_STR + str2;
                    } else {
                        result = formatMain(text);
                    }
                    if (result != null) {
                        pre.text(result);
                    }
                }
            }
        }
        writeTxtFile(doc.html(), file);
    }

    /**
     * 写文件
     * @param content
     * @param file
     * @return
     * @throws IOException
     */
    public static boolean writeTxtFile(String content, File file) throws IOException {
        RandomAccessFile raf = null;
        boolean flag = false;
        FileOutputStream o;
        try {
            o = new FileOutputStream(file);
            o.write(content.getBytes("UTF-8"));
            o.close();
            flag = true;
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (raf != null) {
                raf.close();
            }
        }
        return flag;
    }

    /**
     * 格式化方法签名
     * @param str
     * @return
     */
    public static String formatMain(String str) {
        String result;
        StringBuilder nullStr = new StringBuilder();
        str = str.replace(WRAP_STR, " ")
                .replace("  ", "")
                .replace("( @", "(@");
        int index = str.indexOf(",");
        String str3 = str.substring(0, str.indexOf("(") + 1);
        for (int i = 0; i < str3.length() / 2; i++) {
            nullStr.append(" ");
        }
        if (index > -1) {
            String str4 = str.substring(str.indexOf("(") + 1, str.lastIndexOf(")"));
            String str5 = str.substring(str.lastIndexOf(")"), str.length());
            String[] arr = str4.split(", ");
            StringBuilder sb = new StringBuilder();
            sb.append(str3).append(WRAP_STR);
            for (String s : arr) {
                sb.append(nullStr).append(s).append(", ").append(WRAP_STR);
            }
            sb.delete(sb.lastIndexOf(","), sb.length());
            sb.append(str5);
            result = sb.toString();
        } else {
            result = str;
        }
        if (result.indexOf("throws") > -1) {
            int throwsIndex = result.indexOf("throws");
            if (throwsIndex > -1) {
                result = result.substring(0, throwsIndex) + WRAP_STR + nullStr + result.substring(throwsIndex, result.length());
            }
        }
        return result;
    }


    /**
     * 获取所有文件
     *
     * @param file
     * @param resultFileName
     * @return
     */
    public static List<String> getAllFile(File file, List<String> resultFileName) {
        File[] files = file.listFiles();
        if (files == null) {
            return resultFileName;
        }
        for (File f : files) {
            if (!f.isDirectory() && !excludeSet.contains(f.getName())) {//如果不是文件夹
                resultFileName.add(f.getPath());
            } else {
                getAllFile(f, resultFileName);//如果是文件夹进行递归
            }
        }
        return resultFileName;//返回文件名的集合
    }

}
