#include <ctime>
#include <fstream>
#include <cstdlib>

using namespace std;

const int data_size = 10000000;
int data[data_size];
void swap(int data[], int n1, int n2)
{
    data[n1] = data[n1] ^ data[n2];
    data[n2] = data[n1] ^ data[n2];
    data[n1] = data[n1] ^ data[n2];
}
int main()
{
   for(int i = 0; i < data_size; i++)
       data[i] = i + 1;
   ofstream output("data");
   // 打乱数据
   srand((unsigned)time(0));
   int n1, n2;
   for(int i = 0; i < data_size; i++)
   {
       n1 = rand() % 10000000;
       n2 = rand() % 10000000;
       swap(data[n1], data[n2]);
   }
   //输出到文件
   for(int i = 0; i < data_size; i++)
       output << data[i] << endl;
   return 0;
}

