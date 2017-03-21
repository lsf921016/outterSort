package outterSort;

import java.io.File;

/**
 * Created by lenovo on 2017/3/19.
 */
public class test {
    public static void main(String[] args) throws Exception {
        long start=System.currentTimeMillis();
        File inputFile=new File("E:\\javaStudy\\src\\outterSort\\myInputFile.txt");
        File outputFile=new File("E:\\javaStudy\\src\\outterSort\\outputFile.txt");
        File tempFile=new File("E:\\javaStudy\\src\\outterSort\\tempFile");
        if (outputFile.exists())
            outputFile.delete();
        ProgramTest.test(inputFile,outputFile,tempFile);
        long end=System.currentTimeMillis();
        System.out.println(end-start);
    }
}
