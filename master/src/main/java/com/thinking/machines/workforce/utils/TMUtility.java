package com.thinking.machines.workforce.utils;
import java.nio.file.*;
import java.util.stream.*;
import java.util.*;
import java.io.*;
public class TMUtility
{
private TMUtility(){}
static public String allTrim(String str)
{
StringBuffer sb=new StringBuffer();
int i=0;
int len=str.length();
while(i<len && (str.charAt(i)==' ' || str.charAt(i)==0))i++;
char m;
while(i<len)
{
m=str.charAt(i);
if(m=='\"')
{ 
i++;
while(i<len)
{
m=str.charAt(i);
sb.append(m);
if(m=='\"')
{ 
i++;
break;
} 
i++;
} 
continue;
} 
if(m==' ' || m==0)
{
sb.append(m);
i++;
while(i<len && (str.charAt(i)==' ' || str.charAt(i)==0)) i++;
continue;
}
sb.append(m);
i++;
} 
if(sb.length()>0)
{ 
if(str.charAt(len-1)==' ' || str.charAt(len-1)==0)
{
sb.delete(sb.length()-1,sb.length());
}
}
return sb.toString();
}
static public long bytesToLong(byte bytes[],int start,int end)
{ 
long g=0;
int i=0;
while(end>=start)
{
g=g+(((long)Math.pow(2,i))*bytes[end]);
i++;
end--;
}
return g;
}
static public byte[] longToBytes(long g)
{
byte bytes[]=new byte[64];
int i=63;
while(g>0)
{ if(g%2==0) bytes[i]=0;
else bytes[i]=1;
g=g/2;
i--;
}
return bytes;
}
public static boolean deleteFolder(String folder)
{
Path path=Paths.get(folder);
try(Stream<Path> walk=Files.walk(path))
{
walk.sorted(Comparator.reverseOrder()).map(Path::toFile).forEach(File::delete);
}catch(Exception e)
{
return false;
}
return true;
}
}