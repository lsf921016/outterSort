package outterSort;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Notice:
 * <BR> 1. 仅限使用以下package:
 * java.lang.*, java.io.*, java.math.*, java.text.*, java.util.*
 * <BR> 2. 请勿变更 package名，类名，outterSort()定义。
 */

public class ProgramTest {
    static final int maxSize = 1500000;//内存每次最多放500000条记录
    static final char[] maxKey = {255, 255, 255, 255, 255, 255, 255, 255, ','};

    /**
     * 请在此方法内完成代码，但可以增加自己的私有方法。
     * 数据文件inputFile的内容格式为一行一条数据，每条数据有2个字段用逗号分隔，第1个字段为排序用的Key，第二个字段为value。
     * 换行符为'\n'。
     * 数据内容举例如下:
     * abe,xmflsflmfmlsmfs
     * abc,xmlmxlkmffhf
     * 8fj3l,xxjfluu313ooo11
     * <p>
     * 注意点: 1.本次的测试数据内容都是ASCII字符，无中文汉字.所以不必考虑encoding。
     * 2.本次的测试数据中,key的最大长度8，value的最大长度32。
     * <p>
     * 排序以key的升序，使用String.compareTo()来比较key的大小。最后排序完成的数据写入outputFile。
     *
     * @param inputFile  输入文件
     * @param outputFile 输出文件
     * @param tempFile   临时文件。处理的数据量大的时候，可能会需要用到临时文件。
     * @throws Exception
     */


    public static void test(File inputFile, File outputFile, File tempFile) throws Exception {
        //TODO ====================== YOUR CODE HERE ======================

        BufferedReader bufr = new BufferedReader(new FileReader(inputFile));

        String[] heapArray = new String[maxSize];
        String line = null;//用来存放每次从缓冲区读入的一条记录
        int i = 0;//统计向缓冲区读入了记录条数
        List<File> tempFiles = new ArrayList<>();
        int heapSize = 0;
//replaceSelection begin
        while ((line = bufr.readLine()) != null) {
            heapArray[i++] = line;
            if (i == maxSize)
                break;
        }
        while (i == maxSize) {
            heapSize = maxSize;
            File newTempFile = File.createTempFile("tempFile", ".txt", tempFile);
            tempFiles.add(newTempFile);
            BufferedWriter bufw = new BufferedWriter(new FileWriter(newTempFile));
            buildHeap(heapArray, heapSize, 0);
            while (heapSize != 0) {
                bufw.write(heapArray[0]);
                bufw.newLine();
                line = bufr.readLine();
                if (line == null)
                    break;
                if (keyOf(line).compareTo(keyOf(heapArray[0])) > 0) {
                    heapArray[0] = line;
                } else {
                    heapArray[0] = heapArray[heapSize - 1];
                    heapArray[heapSize - 1] = line;
                    heapSize--;
                }
                siftDown(heapArray, 0, heapSize);
            }
            if (heapSize != 0) {//file input is completed
                i = i - heapSize;
                while (heapSize != 0) {
                    bufw.write(heapArray[0]);
                    bufw.newLine();
                    heapArray[0] = heapArray[heapSize - 1];
                    heapSize--;
                    siftDown(heapArray, 0, heapSize);
                }
            }
            bufw.close();
        }
        //continue to read the rest data in buffer
        if (i != 0) {
            heapSize = i;
            File newTempFile = File.createTempFile("tempFile.txt", ".txt", tempFile.getParentFile());
            tempFiles.add(newTempFile);
            BufferedWriter bufw = new BufferedWriter(new FileWriter(newTempFile));
            int offset = maxSize - heapSize;
            buildHeap(heapArray, heapSize, offset);
            while (heapSize != 0) {
                bufw.write(heapArray[offset]);
                bufw.newLine();
                heapArray[offset] = heapArray[offset + heapSize - 1];
                heapSize--;
                siftDown(heapArray, offset, heapSize);

            }
            bufw.close();
        }
//replaceSelection end,all data are sorted into some separate temFile,all temFile are in tempFiles list.
        //release memory
        heapArray = null;
        System.gc();
        //begin MultiWayMergeSort
        multiWayMergeSort(tempFiles, outputFile);

//delete tempFiles
        for (File file : tempFiles
                ) {
            file.delete();
        }
        //=================================================================
    }


    //TODO ====================== YOUR CODE HERE (You can add private method if need) ======================
    private static void buildHeap(String heapArray[], int size, int start) {
        for (int i = size / 2 - 1; i >= start; i--) {
            siftDown(heapArray, i, size);
        }
    }

    private static void siftDown(String[] heapArray, int i, int size) {
        int j = 2 * i + 1;
        String temp = heapArray[i];
        while (j < size) {
            if (j < size - 1 && (keyOf(heapArray[j]).compareTo(keyOf(heapArray[j + 1]))) > 0)
                ++j;
            if (keyOf(temp).compareTo(heapArray[j]) > 0) {
                heapArray[i] = heapArray[j];
                i = j;
                j = 2 * j + 1;
            } else break;
        }
        heapArray[i] = temp;
    }

    static void multiWayMergeSort(List<File> files, File outputFile) throws IOException {
        int ways = files.size();
        int length_per_run = maxSize / ways;
        Run[] runs = new Run[ways];
        for (int i = 0; i < ways; i++) {
            runs[i] = new Run(length_per_run);
        }
        List<BufferedReader> rList = new ArrayList<>();
        //read files' data into runs' buffer
        for (int i = 0; i < ways; i++) {
            BufferedReader bufr = new BufferedReader(new FileReader(files.get(i)));
            rList.add(i, bufr);
            int j = 0;
            while ((runs[i].buffer[j] = bufr.readLine()) != null) {
                ++j;
                if (j == length_per_run)
                    break;
            }
            runs[i].length = j;
            runs[i].index = 0;
        }
        //merge the files and write to outputFile
        int[] ls = new int[ways];//loser tree
        createLoserTree(ls, runs, ways);
        BufferedWriter bufw = new BufferedWriter(new FileWriter(outputFile));
        int liveRuns = ways;
        while (liveRuns > 0) {
            bufw.write(runs[ls[0]].buffer[runs[ls[0]].index++]);
            bufw.newLine();
            if (runs[ls[0]].index == runs[ls[0]].length) {
                //reload
                int j = 0;
                while ((runs[ls[0]].buffer[j] = rList.get(ls[0]).readLine()) != null) {
                    j++;
                    if (j == length_per_run) {
                        break;
                    }
                }
                runs[ls[0]].length = j;
                runs[ls[0]].index = 0;
            }
            if (runs[ls[0]].length == 0) {
                liveRuns--;
                String maxString = new String(maxKey);
                maxString += "\n";
                runs[ls[0]].buffer[runs[ls[0]].index] = maxString;
            }
            adjust(ls, runs, ways, ls[0]);
        }
        bufw.flush();
        bufw.close();
        for (BufferedReader bufr : rList
                ) {
            bufr.close();
        }

    }

    private static void createLoserTree(int[] ls, Run[] runs, int n) {
        //ways equals to the number of nodes in loserTree

        for (int i = 0; i < n; i++) {
            ls[i] = -1;
        }
        for (int i = n - 1; i >= 0; i--) {
            adjust(ls, runs, n, i);
        }
    }

    private static void adjust(int[] ls, Run[] runs, int n, int s) {
        int t = (s + n) / 2;
        int temp = 0;
        while (t != 0) {
            if (s == -1)
                break;
            if (ls[t] == -1 || (keyOf(runs[s].buffer[runs[s].index]).compareTo(keyOf(runs[ls[t]].buffer[runs[ls[t]].index]))) > 0) {
                temp = s;
                s = ls[t];
                ls[t] = temp;
            }
            t /= 2;
        }
        ls[0] = s;
    }


    static String keyOf(String str) {

        return str.substring(0, str.indexOf(","));
    }


    static class Run {
        String[] buffer;
        int length;
        int index;

        Run(int length) {
            this.length = length;
            buffer = new String[length];
        }
    }
    //=================================================================
}
