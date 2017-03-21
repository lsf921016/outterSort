package outterSort;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Random;

public class generateData {
    public static void main(String[] args) throws IOException {
        final int MAX=80000000;
        File f=new File("E:\\javaStudy\\src\\outterSort\\myInputFile.txt");
        if (f.exists())
            f.delete();
        BufferedWriter bufw=new BufferedWriter(new FileWriter(f));
        for (int i=0;i<MAX;++i){
            bufw.write(getRandomString());
            bufw.newLine();
        }
        bufw.flush();
        bufw.close();
    }
    public static String getRandomString(){
        StringBuilder sb=new StringBuilder();
        Random random=new Random();
        for (int i = 0; i < 8; i++) {
            sb.append((char)(random.nextInt(26)+97));

        }
        sb.append(',');
        for (int i = 0; i <16 ; i++) {
            sb.append((char)(random.nextInt(26)+97));
        }

        return sb.toString();
    }
}
