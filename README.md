<<<<<<< HEAD
#一.涉及知识
1. 堆排序，归并排序, 选择置换，多路归并，败者树
2. 文件io操作
3. 对内存的理解

#二.问题描述
现实中，当需要对一个很大的文件中的记录进行排序，内存无法一次装下全部数据，就需要借助磁盘空间作为数据中转，即从n个中转文件中（中转文件内的数据先要在内存排好序），每次取出N/n（N为最大内存可用空间）长度的顺串（runs）在内存中排序，然后写入输出文件直到归并完成，中转文件数量为n，即是n路归并，以此来解决内存不足的问题，所以原来的问题就分解成了两个子问题：
1. 生成顺串
2. 归并顺串

本例使用**选择置换**来生成顺串，**多路归并**来归并顺串，**败者树**来达到最优归并

##1.为什么使用选择置换和多路归并：
再考虑效率的问题时，我们假设有8路顺串等待归并，如果每次归并2个，则需要归并4+2+1次，共进行了3趟归并，每个数据也就被io操作了3次，如果8路一起归并，则每个数据只会被io操作一次。因此，减少归并趟数可以大大减少系统io的开销。为了减少归并躺数，我们可以从两方面着手：

1. 生成尽可能大的顺串：假设内存一次只能对m条数据进行排序，则选择置换可以每次生成大于m小于2m条有序数据。
2. 采用多路归并。

##2.为什么使用败者树
如果当前有k路，m个顺串需要归并，则每输出一条数据需要进行k-1次比较，则时间复杂度为O(n),使用败者树只有在初始化的时候需要比较k-1次，此后每次只需要logkM次，时间复杂度威O（logk）。原理就像分组比赛，每个人不用和其他所有都比一次，而是两两分组，胜者只和其他组的胜者比较。
![这里写图片描述](http://img.blog.csdn.net/20170322051650011?watermark/2/text/aHR0cDovL2Jsb2cuY3Nkbi5uZXQvbHNmOTIxMDE2/font/5a6L5L2T/fontsize/400/fill/I0JBQkFCMA==/dissolve/70/gravity/SouthEast)
（截图自coursera，北京大学高级数据结构与算法公开课，侵权删。）

多路归并与败者树算法描述：

```
//建立败者树
void createLoserTree(){
        b[k]=MINKEY;
        for (int i = 0; i < k; i++) {
            ls[i]=-1;
        }
        for (int i = k-1; i >=0 ; i--) {
            Adjust(i);
        }
    }   
}
```

```
//调整败者树
void adjust(int s){
        for (t=(s+k)/2; t >0 ; t/=2) {
            if (b[s]>b[ls[t]]){
                swap(s,ls[t]);
            }
        }
        ls[0]=s;
    }
```

```
//k路归并
void K_merge(){
        for (int i = 0; i < k; i++) {
            input(i)//第i路输入一个元素到b[i]
        }
        createLoserTree(ls);
        while (b[ls[0]]!=MAXKEY){
            q=ls[0];
            output(b[q]);
            input(q);
            adjust(q);
        }
    }
```


最后附上我写的完整代码（可运行），对8000万条记录（2G大小）进行排序，并输出，耗时657.211s。
代码github地址：https://github.com/lsf921016

```
package outterSort;

import java.io.*;
import java.util.ArrayList;
import java.util.List;


public class ProgramTest {
    static final int maxSize = 1500000;//内存每次最多放500000条记录
    static final char[] maxKey = {255, 255, 255, 255, 255, 255, 255, 255, ','};


    public static void Zhihu(File inputFile, File outputFile, File tempFile) throws Exception {

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

```

生成数据的代码：

```
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

```
代码github地址：https://github.com/lsf921016
=======
# outterSort
外排序算法，对8000万条记录进行排序，耗时657211ms，算法思想：置换选择排序+最优多路归并-败者树

>>>>>>> origin/master
