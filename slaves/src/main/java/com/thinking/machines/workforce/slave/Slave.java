package com.thinking.machines.workforce.slave;
import java.lang.reflect.*;
import com.google.gson.*;
import java.util.*;
import com.thinking.machines.utils.*;
import java.net.*;
import java.io.*;
public class Slave extends Thread
{
private int portNumber;
public Slave(int portNumber)
{
this.portNumber=portNumber;
if(this.portNumber==7504) throw new RuntimeException("whatever");
}
public void run()
{
try
{
ServerSocket serverSocket=new ServerSocket(portNumber);
Socket socket=serverSocket.accept();
InputStream inputStream=socket.getInputStream();
OutputStream outputStream=socket.getOutputStream();
byte bytes[]=new byte[1024];
int count=inputStream.read(bytes);
long lengthOfFile=TMUtility.bytesToLong(bytes,0,63);
byte ack[]=new byte[2];
ack[0]=65;
ack[1]=66;
String zipFileName="temp\\"+UUID.randomUUID().toString().replaceAll("-","3")+".zip";
FileOutputStream fileOutputStream;
fileOutputStream=new FileOutputStream(zipFileName);
BufferedOutputStream bos=new BufferedOutputStream(fileOutputStream);
outputStream.write(ack);
outputStream.flush();
int i=0;
int bytesRead;
while(true)
{
while(true)
{
bytesRead=inputStream.read(bytes);
if(bytesRead!=-1) break;
}
i=i+bytesRead;
bos.write(bytes,0,bytesRead);
bos.flush();
outputStream.write(ack);
outputStream.flush();
if(i==lengthOfFile) break;
}
fileOutputStream.close();
System.out.println("Task accepted as a zip file.");
System.out.println("Now exploding file to task folder");
TMZipUtility.unzipIt(zipFileName,"task");
System.out.println("Explode complete");
String dataFile="";
String className="";
String methodName="";
try
{
JsonObject jo=new Gson().fromJson(new FileReader("task\\task.json"),JsonObject.class);
dataFile=jo.get("input").getAsString();
className=jo.get("class").getAsString();
methodName=jo.get("method").getAsString();
}catch(Exception exception)
{
exception.printStackTrace();
// something to send back that, some problem exists with the job that has been submitted
}
File [] jarFiles=new File("task").listFiles(new FileFilter(){
public boolean accept(File f)
{
return f.getName().endsWith(".jar");
}
});
URL [] jarURLS=new URL[jarFiles.length];
int ii=0;
for(File jarFile:jarFiles)
{
jarURLS[ii]=jarFile.toURL();
ii++;
}
URLClassLoader urlClassLoader=new URLClassLoader(jarURLS);
//prepare Argument list
RandomAccessFile raf=new RandomAccessFile(new File("task\\"+dataFile),"rw");
List<String> argumentList=new LinkedList<>();
String line;
while(raf.getFilePointer()<raf.length())
{
line=raf.readLine();
argumentList.add(line);
}
raf.close();
List<String> resultList;
Class jobClass=Class.forName(className,true,urlClassLoader);
Method method=jobClass.getMethod(methodName,new Class[]{List.class});
Object object=jobClass.newInstance();
resultList=(List<String>)method.invoke(object,argumentList);
File resultsFolder=new File("results");
if(resultsFolder.exists()==false) resultsFolder.mkdir();
raf=new RandomAccessFile(new File("results\\result.data"),"rw");
for(String s:resultList)
{
raf.writeBytes(s+"\r\n");
}
raf.close();
TMZipUtility.zipIt(new File("results"),"temp\\result.zip");
File resultFileToSend=new File("temp\\result.zip");
lengthOfFile=resultFileToSend.length();
byte b[]=TMUtility.longToBytes(lengthOfFile);
int j;
for(j=0;j<=63;j++)
{
bytes[j]=b[j];
}
outputStream.write(bytes);
outputStream.flush();
i=-1;
while(i==-1) i=inputStream.read(ack);
int bufferSize=1024;
int numberOfBytesToWrite=bufferSize;
FileInputStream fileInputStream;
fileInputStream=new FileInputStream(resultFileToSend);
BufferedInputStream bis=new BufferedInputStream(fileInputStream);
byte contents[]=new byte[1024];
i=0;
while(i<lengthOfFile)
{
bytesRead=bis.read(contents);
if(bytesRead<0) break;
outputStream.write(contents,0,bytesRead);
outputStream.flush();
int m=-1;
while(m==-1) m=inputStream.read(ack);
i=i+bytesRead;
}
fileInputStream.close();
System.out.println("ResultFile sent complete,now wait for the task to be completed");
socket.close();
System.out.println("No I am goint to take rest");
}catch(Exception e)
{
e.printStackTrace();
}
}
}