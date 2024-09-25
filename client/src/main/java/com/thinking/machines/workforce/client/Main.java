package com.thinking.machines.workforce.client;
import com.thinking.machines.workforce.utils.*;
import java.io.*;
import java.net.*;
public class Main
{
private static Socket socket;
private static InputStream inputStream;
private static OutputStream outputStream;
public static void main(String gg[])
{ 
try
{
BufferedReader br=new BufferedReader(new InputStreamReader(System.in));
String commandLine;
while(true)
{ 
if(socket==null)
{
System.out.printf("workforce v1.0>");
} 
else
{
System.out.printf("workforce v1.0#guest>");
} 
commandLine=br.readLine();
TMUtility.allTrim(commandLine);
if(commandLine.equals("quit") || commandLine.equals("exit"))
{ 
closeConnection();
break;
}
processCommand(commandLine);
}
System.out.println("bye !");
}catch(Throwable t)
{ 
t.printStackTrace();
}
}
private static void closeConnection()
{ 
if(socket==null) return;
try
{
byte []bytes=new byte[8];
bytes[7]=1;
bytes[5]=1;
outputStream.write(bytes);
outputStream.flush();
socket.close();
}catch(Exception e)
{ 
e.printStackTrace();
}
socket=null;
}
private static void processCommand(String commandLine)
{ 
if(commandLine.equals("?"))
{
displayHelp();
return;
} 
if(commandLine.startsWith("connect "))
{
processConnectRequest(commandLine.substring(8));
return;
} 
if(commandLine.startsWith("submit "))
{
processSubmitRequest(commandLine.substring(7));
}
}
private static void processSubmitRequest(String folderPath)
{ 
if(socket==null)
{
System.out.println("You need to connect to workforce server using connect command, press ? for help");
return;
} 
try
{
File file=new File(folderPath);
if(file.exists()==false)
{
System.out.println(folderPath+" does not exist");
return;
} 
if(file.isDirectory()==false)
{
System.out.println(folderPath+" is not a directory");
return;
}
String zipFileName=java.util.UUID.randomUUID().toString().replaceAll("-","3");
System.out.println("compressing folder before uploading task");
zipFileName="temp\\"+zipFileName+".zip";
TMZipUtility.zipIt(file,zipFileName);
System.out.println("folder compressed");
System.out.println("uploading zipped version of the folder");
// code to upload the zip file starts here
byte bytes[]=new byte[1024];
bytes[6]=1;
File fileToUpload=new File(zipFileName);
long lengthOfFile=fileToUpload.length();
byte b[]=TMUtility.longToBytes(lengthOfFile);
int i,j;
for(i=8,j=0;j<=63;j++,i++)
{
bytes[i]=b[j];
}
outputStream.write(bytes);
outputStream.flush();
byte ack[]=new byte[2];
i=-1;
while(i==-1) i=inputStream.read(ack);
int bufferSize=1024;
int numberOfBytesToWrite=bufferSize;
FileInputStream fileInputStream;
fileInputStream=new FileInputStream(fileToUpload);
BufferedInputStream bis=new BufferedInputStream(fileInputStream);
byte contents[]=new byte[1024];
int bytesRead;
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
System.out.println("Upload complete,now wait for the task to be completed");
i=-1;
while(i==-1)
{ 
i=inputStream.read(bytes);
}
System.out.println(new String(bytes,0,i));
}catch(Exception exception)
{
exception.printStackTrace();
}
}
private static void processConnectRequest(String server)
{ 
// Right now the server name is insignificant
String serverIP="localhost";
int port=9050;
try
{
socket=new Socket(serverIP,port);
inputStream=socket.getInputStream();
outputStream=socket.getOutputStream();
byte bytes[]=new byte[1024];
bytes[7]=1;
outputStream.write(bytes);
outputStream.flush();
int count;
while(true)
{ 
count=inputStream.read(bytes);
if(count!=-1) break;
}
String response=new String(bytes,0,count);
System.out.println(response);
}catch(Exception exception)
{
System.out.println("unable to connect to : "+server);
}
}
private static void displayLine(int howBig)
{
for(int i=1;i<=howBig;i++) System.out.print("-");
System.out.println();
}
private static void displayHelp()
{
displayLine(60);
System.out.printf("%-40s %-35s\n","Command","Purpose");
displayLine(60);
System.out.printf("%-40s %-35s\n","?","for help");
System.out.printf("%-40s %-35s\n","quit","to exit application");
System.out.printf("%-40s %-35s\n","connect wf.simba","to connect to simba workforce server");
System.out.printf("%-40s %-35s\n","submit [folder path]","to submit an synchronous task");
System.out.printf("%-40s %-35s\n","async submit [folder path]","to submit an asynchronous task");
System.out.printf("%-40s %-35s\n","status [task-key]","to get status of task");
System.out.printf("%-40s %-35s\n","save [task-key] [result-file-path]","to download result of task");
}
}