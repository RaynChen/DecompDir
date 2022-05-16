package jdWrapper;

import org.jd.core.v1.ClassFileToJavaSourceDecompiler;

import java.io.*;
import java.nio.file.Paths;
import java.nio.file.Files;

import java.text.SimpleDateFormat;
import java.util.Stack;
import java.util.Date;

public class DecompDir {

    public static void decompile(String path_top) throws Exception{
        Stack<String> stack = new Stack<>();
        stack.push(path_top);
        long count_class = 0;
        long count_java =0;
        Date currentTime = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("yyyyMMddHHmm");
        String time = timeFormat.format(currentTime.getTime());

        String SLASH = "\\";
        switch (System.getProperties().get("os.name").toString().substring(0,3)){
            case "Mac": SLASH = "/"; break;
        }



        String logPath = path_top+SLASH+"err_"+time+".log";
        File disFile = new File(logPath);
        if (!disFile.getParentFile().exists()) disFile.getParentFile().mkdirs();
        BufferedWriter logWriter = new BufferedWriter(new FileWriter(disFile));
        while (stack.size()>0){
            String path = stack.pop();
            File fileDir = new File(path);
            if (fileDir.exists()){
                File[] fileList = fileDir.listFiles();
                DirectoryLoader loader = new DirectoryLoader(fileDir);
                ClassFileToJavaSourceDecompiler decompiler = new ClassFileToJavaSourceDecompiler();
                if (fileList != null && fileList.length >0 ){
                    int len = fileList.length;
                    for(int i = 0;i<len;i++) {
                        File file = fileList[i];
                        if (file.isDirectory()) {
                            stack.push(file.toString());
                            continue;
                        }

                        String fileName = file.getName();
                        String originalFilePath = path + SLASH + fileName;
                        try {
                            String suffix = fileName.substring(file.getName().lastIndexOf(".") + 1);
                            String prefix = fileName.substring(0, file.getName().lastIndexOf("."));

                            if (!"class".equalsIgnoreCase(suffix))
                                continue;
                            PlainTextPrinter printer = new PlainTextPrinter();
                            count_class++;
                            decompiler.decompile(loader, printer, prefix);
                            String source = printer.toString();
                            String tmpFilePath = path + SLASH + prefix + "_tmp" + ".java";
                            String finalFilePath = path + SLASH + prefix + ".java";

                            boolean flag1 = string2File(source, tmpFilePath);
                            boolean flag2 = removeComment(tmpFilePath, finalFilePath);
                            if (flag1 && flag2) {
                                Files.deleteIfExists(Paths.get(tmpFilePath));
                                Files.deleteIfExists(Paths.get(originalFilePath));
                                System.out.println("delete:" + originalFilePath);
                                count_java++;
                            }
                        } catch (Exception e) {
                            logWriter.write("####ERROR#####" + originalFilePath);

                            logWriter.newLine();
                            logWriter.flush();
                            e.printStackTrace();
                        }
                    }
                }
            }

        }
        logWriter.close();
        System.out.println(".class file count = "+count_class +"\t.java file count="+ count_java);
        if(disFile.length()==0){
            System.out.println("No Error detected,deleting the log file"+logPath);
//            disFile.delete();
        }


    }

    private static boolean string2File(String res, String filePath){
        boolean result = false;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        try{
            File distFile = new File(filePath);
            if (!distFile.getParentFile().exists()) distFile.getParentFile().mkdirs();
            bufferedReader = new BufferedReader(new StringReader(res));
            bufferedWriter = new BufferedWriter(new FileWriter(distFile));
            char buf[] = new char[1024];
            int len;
            while ((len = bufferedReader.read(buf)) != -1){
                bufferedWriter.write(buf,0,len);
            }
            bufferedWriter.flush();
            bufferedReader.close();
            bufferedWriter.close();
            result = true;
        }catch (IOException e ){
            e.printStackTrace();
        }finally{
            if (bufferedReader != null){
                try{
                    bufferedReader.close();
                }catch(IOException e){
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    private static boolean removeComment(String fileInPath, String fileOutPath){
        boolean result = false;
        BufferedReader bufferedReader = null;
        BufferedWriter bufferedWriter = null;
        try{
           File disFile = new File(fileOutPath);
           if (!disFile.getParentFile().exists()) disFile.getParentFile().mkdirs();

           bufferedReader = new BufferedReader(new FileReader(fileInPath));
           bufferedWriter = new BufferedWriter(new FileWriter(disFile));
           String line = "";
           while ((line = bufferedReader.readLine()) != null){
               int end = line.indexOf("*/");
               try{
                   bufferedWriter.write(line.substring(end+3));
                   bufferedWriter.newLine();
               } catch(Exception e ){
                   bufferedWriter.newLine();
               }
               bufferedWriter.flush();
           }
           bufferedReader.close();
           bufferedWriter.close();
           result = true;
        }catch(IOException e){
            e.printStackTrace();
        }finally {
            if(bufferedReader != null){
                try{
                    bufferedReader.close();
                } catch(IOException e ){
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    public static void main(String[] args){
        try {
            decompile(args[0]);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
