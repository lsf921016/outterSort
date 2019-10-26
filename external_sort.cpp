#include <fstream>
#include <iostream>
#include <cstdlib>
#include <ctime>

using namespace std;
#define MAX_INT 0x7fffffff
#define MIN_INT -1

const int kMaxSize = 100000000;
const int kMaxWay = 10;

int buffer[kMaxSize];   //假设内存只能放1000000个整型
int heap_size;
int num_of_runs;

struct Run
{
    int *buffer;       // 每个顺串的缓冲区
    int length;     // 缓冲区内元素个数
    int idx;        // 当前所读元素下标
};
int ls[kMaxWay];        //败者树,ls[0]是最小值的位置，其余是各败者的位置
Run *runs[kMaxWay];     //顺串结构

void Swap(int *arr, int i, int j)
{
    int tmp = arr[i];
    arr[i] = arr[j];
    arr[j] = tmp;
}

void Siftdown(int *heap, int pos, int size)
{
    while(pos < size/2)
    {
        int tmp = 2 * pos + 1;
        int rc = 2* pos + 2;
        if((rc < size) && heap[tmp] > heap[rc])
            tmp = rc;
        if(heap[pos] < heap[tmp])
            return;
        Swap(heap, pos, tmp);
        pos = tmp;
    }
}

void BuildHeap(int *heap, int size)
{
    for(int i = size/2-1; i >= 0; i--)
        Siftdown(heap, i, size);
}

// 返回生成顺串的数量
int GenerateRuns(const char *in_file)
{
    ifstream in(in_file);
    char output_file[20];
    ofstream out;
    int i = 0, count = 0;
    int num;
    while(!in.eof())
    {
        in >> buffer[i];
        if(++i == kMaxSize)
            break;
    }
    while(i == kMaxSize)
    {
        heap_size = kMaxSize;
        count++;
        out.close();
        sprintf(output_file, "%d", count);
        out.open(output_file);
        BuildHeap(buffer, heap_size);
        while(heap_size != 0 && !in.eof())
        {
            out << buffer[0] << endl;
            in >> num;
            if(num > buffer[0])
            {
                buffer[0] = num;
            }
            else
            {
                buffer[0] = buffer[heap_size-1];
                buffer[heap_size-1] = num;
                heap_size--;
            }
            Siftdown(buffer, 0, heap_size);
        }
        if(heap_size != 0)    //输入缓冲区已空
        {
            i = i - heap_size;
            while(heap_size != 0)
            {
                out << buffer[0] << endl;
                buffer[0] = buffer[--heap_size];
                Siftdown(buffer, 0, heap_size);
            }
        }
    }
    // 处理buffer中剩余数据
    if(i != 0)
    {
        heap_size = i;
        count++;
        out.close();
        sprintf(output_file, "%d", count);
        out.open(output_file);
        int offset = kMaxSize - heap_size;
        BuildHeap(buffer+offset, heap_size);
        while(heap_size != 0)
        {
            out << buffer[offset] << endl;
            buffer[offset] = buffer[--heap_size+offset];
            Siftdown(buffer+offset, 0, heap_size);
        }
        out.close();
    }
    return count;

}

void Adjust(Run **runs, int n, int s)
{
    //首先根据s计算出对应的ls中哪一个下标
    int t = (s + n) / 2;
    int tmp;

    while(t != 0)
    {
        if(s == -1)
            break;
        if(ls[t] == -1 || runs[s]->buffer[runs[s]->idx] >
                          runs[ls[t]]->buffer[runs[ls[t]]->idx])
        {
            tmp = s;
            s = ls[t];
            ls[t] = tmp;
        }
        t /= 2;
    }
    ls[0] = s;
}
void CreateLoserTree(Run **runs, int n)
{
    for(int i = 0; i < n; i++)
    {
        ls[i] = -1;
    }
    for(int i = n-1; i >= 0; i--)
    {
        Adjust(runs, n, i);
    }
}

int MergeSort(Run **runs, int num_of_runs, const char* file_out)
{
    // 初始化Run
    if(num_of_runs > kMaxWay)
        num_of_runs = kMaxWay;
    int length_per_run = kMaxSize / num_of_runs;
    for(int i = 0; i < num_of_runs; i++)
        runs[i]->buffer = buffer + i * length_per_run;

    ifstream in[kMaxWay];
    char file_name[20];
    for(int i = 0; i< num_of_runs; i++)
    {
        sprintf(file_name, "%d", i+1);
        in[i].open(file_name);
    }
    // 将顺串文件的数据读到缓冲区中
    for(int i = 0; i < num_of_runs; i++)
    {
        int j = 0;
        while(in[i] >> runs[i]->buffer[j])
        {
            j++;
            if(j == length_per_run)
                break;
        }
        runs[i]->length = j;
        runs[i]->idx = 0;
    }

    CreateLoserTree(runs, num_of_runs);
    ofstream out(file_out);
    int live_runs = num_of_runs;
    while(live_runs > 0)
    {
        out << runs[ls[0]]->buffer[runs[ls[0]]->idx++] << endl;
        if(runs[ls[0]]->idx == runs[ls[0]]->length)
        {
            int j = 0;
            while(in[ls[0]] >> runs[ls[0]]->buffer[j])
            {
                j++;
                if(j == length_per_run)
                    break;
            }
            runs[ls[0]]->length = j;
            runs[ls[0]]->idx = 0;
        }
        if(runs[ls[0]]->length == 0)
        {
            runs[ls[0]]->buffer[runs[ls[0]]->idx] = MAX_INT;
            live_runs--;
        }
        Adjust(runs, num_of_runs, ls[0]);
    }
}
int main(int argc, char **argv)
{
    char *in_file = "data";
    clock_t t;
    cout << "生成顺串..." << endl;
    num_of_runs = 6;
    t = clock();
    num_of_runs = GenerateRuns(in_file);
    t = clock() - t;
    cout << "顺串生成成功， 数量:" << num_of_runs << endl;
    cout << "耗时: " << (double)t / CLOCKS_PER_SEC << "s" << endl;
    cout << "归并开始..." << endl;
    t = clock();
    for(int i = 0; i < num_of_runs; i++)
        runs[i] = new Run();
    MergeSort(runs, num_of_runs, "sorted");
    t = clock() - t;
    cout << "归并成功." << endl;
    cout << "耗时: " << (double)t / CLOCKS_PER_SEC << "s" << endl;
}
