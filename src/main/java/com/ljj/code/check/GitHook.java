/**
 * 文件名称:          		YixinCodeRevice.java
 * 版权所有@ 2019-2020 		易鑫集团，保留所有权利
 * 编译器:           		JDK1.8
 */

package com.ljj.code.check;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import net.sourceforge.pmd.PMD;

/**
 * p3c集成PMD、Git在代码提交之前进行代码规范检查
 * 
 * Version 1.0.0
 * 
 * @author liangjinjing
 * 
 * Date 2019-09-10 16:15
 * 
 */
public class GitHook {

    /**
     * 
     */
    private String codeReviewWorkDir;
    /**
     * 
     */
    private static final String FILE_LIST_NAME = "checkstyle.filelist";

    /**
     * 
     */
    private void start(String[] args) {
        codeReviewWorkDir = System.getProperty("user.home") + File.separatorChar + "CheckStyleWorkDir";
        File file = new File(codeReviewWorkDir);
        if (!file.exists()) {
            file.mkdirs();
        }
        String reviewCodeFileListPath = codeReviewWorkDir + File.separatorChar + FILE_LIST_NAME;
        String outFilePath = codeReviewWorkDir + File.separatorChar + "check_results.txt";
        if (buildFileList(reviewCodeFileListPath)) {
            
            List<String> runArgs = new ArrayList<String>();
            // check文件列表
            runArgs.add("-filelist");
            runArgs.add(reviewCodeFileListPath);
            // check规则
            runArgs.add("-R");
            runArgs.add("rulesets/java/ali-comment.xml," + "rulesets/java/ali-concurrent.xml,"
                + "rulesets/java/ali-constant.xml," + "rulesets/java/ali-exception.xml,"
                + "rulesets/java/ali-flowcontrol.xml," + "rulesets/java/ali-naming.xml," + "rulesets/java/ali-oop.xml,"
                + "rulesets/java/ali-orm.xml," + "rulesets/java/ali-other.xml," + "rulesets/java/ali-set.xml");
            // 输出格式
            runArgs.add("-f");
            runArgs.add("text");
            // 输出文件
            runArgs.add("-r");
            runArgs.add(outFilePath);
            // 不用缓存
            runArgs.add("-no-cache");
            // 合并运行参数
            int paramLen = args != null ? args.length + runArgs.size() : runArgs.size();
            String[] param = new String[paramLen];
            int index = 0;
            if (args != null) {
                System.arraycopy(args, 0, param, 0, args.length);
                index += args.length;
            }
            for (String s : runArgs) {
                param[index++] = s;
            }
            //
            PMD.run(param);
            // 是否存在不符合规范的代码
            File outFile = new File(outFilePath);
            if (outFile.exists()) {
                try {
                    FileInputStream in = new FileInputStream(outFile);
                    if (in.available() > 0) {
                        // 此处输出需要与pre-commit脚本中ERROR_MESSAGE保持一致
                        System.out.println("Code is not standardized, Detail file path: " + outFilePath);
                    } else {
                        in.close();
                        outFile.delete();
                    }
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * 
     * @return
     */
    private boolean buildFileList(String reviewCodeFileListPath) {
        // 根目录
        String basePath = new File("").getAbsolutePath();
        System.out.println("basePath = " + basePath);
        // 是否有修改的代码
        File diffFileList = new File(basePath + File.separatorChar + "target" + File.separatorChar + FILE_LIST_NAME);
        if (!diffFileList.exists()) {
            System.out.println("Unmodified code");
            return false;
        }
        File reviewCodeFile = new File(reviewCodeFileListPath);
        boolean isWin = System.getProperty("os.name").toLowerCase().startsWith("win");
        boolean hasDiffFile = false;
        BufferedReader reader = null;
        BufferedWriter writer = null;
        try {
            reader = new BufferedReader(new FileReader(diffFileList));
            writer = new BufferedWriter(new FileWriter(reviewCodeFile));
            String line;
            while ((line = reader.readLine()) != null) {
                hasDiffFile = true;
                if (isWin) {
                    line = line.replace("/", "\\");
                }
                writer.write(basePath + File.separatorChar + line+ ",");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        finally {
            try {
                if (reader != null) {
                    reader.close();
                }
                if (writer != null) {
                    writer.close();
                }
            } catch (IOException e) {
            }
        }
        return hasDiffFile;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        GitHook hook = new GitHook();
        hook.start(args);
    }

}
