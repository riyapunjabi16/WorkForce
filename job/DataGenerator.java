import java.io.*;
import java.util.*;
class DataGenerator
{
public static void main(String gg[])
{ 
try
{
File file=new File("data.txt");
if(file.exists()) file.delete();
RandomAccessFile raf=new RandomAccessFile(file,"rw");
long j=System.nanoTime();
int x=1;
//while(x<=1147483648)
while(x<=100)
{
raf.writeBytes(UUID.randomUUID().toString()+"\r\n");
x++;
}
raf.close();
long m=System.nanoTime();
long r=m-j;
System.out.println("Time taken : "+(r/1000000)+" milli seconds.");
}catch(Exception e)
{ 
e.printStackTrace();
}
}
}