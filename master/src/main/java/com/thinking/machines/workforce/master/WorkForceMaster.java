package com.thinking.machines.workforce.master;
import java.nio.file.*;
import com.thinking.machines.workforce.utils.*;
import java.util.*;
import java.io.*;
import java.net.*;
import com.google.gson.*;
class SlaveNode
{
private int slaveStarterPortNumber;
private String ip;
private int slavePortNumber;
public void setSlaveStarterPortNumber(int slaveStarterPortNumber) 
{
this.slaveStarterPortNumber=slaveStarterPortNumber; 
}
public int getSlaveStarterPortNumber() 
{
return this.slaveStarterPortNumber; 
}
public void setIp(String ip) 
{
this.ip=ip; 
}
public String getIp() 
{
return this.ip; 
}
public void setSlavePortNumber(int slavePortNumber) 
{
this.slavePortNumber=slavePortNumber; 
}
public int getSlavePortNumber() 
{
return this.slavePortNumber; 
}
}
public class WorkForceMaster 
{
private List<SlaveNode> slaves;
private Long clientId;
private ServerSocket serverSocket;
private HashMap<Long,RequestProcessor> map=new HashMap<>();
public WorkForceMaster() 
{
try
{
this.clientId=new Long(0);
serverSocket=new ServerSocket(9050);
populateSlaves();
waitForConnection();
}catch(Exception e) 
{
e.printStackTrace();
System.exit(0); 
}
}
private void populateSlaves()
{
slaves=new ArrayList<>();
SlaveNode slave;
slave=new SlaveNode();
slave.setIp("localhost");
slave.setSlaveStarterPortNumber(7201);
slaves.add(slave);
slave=new SlaveNode();
slave.setIp("localhost");
slave.setSlaveStarterPortNumber(7202);
slaves.add(slave);
slave=new SlaveNode();
slave.setIp("localhost");
slave.setSlaveStarterPortNumber(7203);
slaves.add(slave);
slave=new SlaveNode();
slave.setIp("localhost");
slave.setSlaveStarterPortNumber(7204);
slaves.add(slave);
}
private void waitForConnection()
{
try
{
while(true)
{
Socket socket=serverSocket.accept();
this.clientId++;
System.out.println("Client connected, Id : "+clientId);
RequestProcessor requestProcessor=new RequestProcessor(socket,clientId);
map.put(clientId,requestProcessor);
requestProcessor.start();
}
}catch(Exception e)
{
e.printStackTrace();
}
}
// inner class starts
class RequestProcessor extends Thread
{
private Socket socket;
private Long clientId;
RequestProcessor(Socket socket,Long clientId)
{
this.clientId=clientId;
this.socket=socket;
}
public void run()
{
InputStream inputStream;
OutputStream outputStream;
try
{
inputStream=socket.getInputStream();
outputStream=socket.getOutputStream();
}catch(IOException ioException)
{
ioException.printStackTrace();
return;
}
byte [] bytes=new byte[1024];
int count;
while(true)
{
try
{
while(true)
{
count=inputStream.read(bytes);
if(count!=-1) break;
}
if(getRequestType(bytes)==1) // connect request
{
outputStream.write("You are connected as guest".getBytes());
outputStream.flush();
continue;
}
if(getRequestType(bytes)==2) // submit task request
{
long lengthOfFile=TMUtility.bytesToLong(bytes,8,71);
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
System.out.println("Now waking up slaves to get the task done");
List<SlaveNode> workers=new ArrayList<>();
for(SlaveNode slave:slaves)
{
try
{
Socket socket=new Socket(slave.getIp(),slave.getSlaveStarterPortNumber());
InputStream is=socket.getInputStream();
OutputStream os=socket.getOutputStream();
bytes=new byte[1024];
bytes[0]=1;
os.write(bytes);
os.flush();
int x=-1;
while(x==-1) x=is.read(bytes);
if(x!=1024)
{
// some big logical or networking problem
socket.close();
continue;
}
if(bytes[0]==0) // unable to turn on
{
socket.close();
continue;
}
os.write(ack);
os.flush();
// let us assume that bytes[0] is 1, as there is no other case right now
int portNumber=(int)TMUtility.bytesToLong(bytes,1,16);
// Now whatever we are doing is bad, will rectify it later on
slave.setSlavePortNumber(portNumber);
System.out.printf("Slave on %s is ready to accept job request on port :%d\n",slave.getIp(),slave.getSlavePortNumber());
workers.add(slave);
socket.close();
}catch(Exception exception)
{
exception.printStackTrace();
}
} // iterator ends
int numberOfWorkers=workers.size();
// validation for existence of task.json is pending
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
if(numberOfWorkers<=1)
{
// something to send back that, right now work cannot be done as no workers are available
}
String distFolderName="task\\"+UUID.randomUUID().toString().replaceAll("-","4");
File dataFiles[]=new File[numberOfWorkers];
int xx=1;
while(xx<=numberOfWorkers)
{
File jobFolder=new File(distFolderName+"\\folder"+xx);
jobFolder.mkdirs();
dataFiles[xx-1]=new File(distFolderName+"\\folder"+xx+"\\"+dataFile);
xx++;
}
RandomAccessFile randomAccessFiles[]=new RandomAccessFile[dataFiles.length];
try
{
xx=0;
for(RandomAccessFile raf:randomAccessFiles)
{
randomAccessFiles[xx]=new RandomAccessFile(dataFiles[xx],"rw");
xx++;
}
File df=new File("task\\"+dataFile);
RandomAccessFile dfraf=new RandomAccessFile(df,"rw");
String line;
xx=0;
while(dfraf.getFilePointer()<dfraf.length())
{
line=dfraf.readLine();
randomAccessFiles[xx].writeBytes(line+"\n");
xx++;
if(xx==numberOfWorkers) xx=0;
}
for(RandomAccessFile raf:randomAccessFiles) raf.close();
dfraf.close();
}catch(Exception exception)
{
exception.printStackTrace();
// something related to some problem should go back
}
xx=1;
String sourceTaskJsonFile="task\\task.json";
List<String> taskZipFiles=new LinkedList<>();
while(xx<=numberOfWorkers)
{
String destinationTaskJsonFile=distFolderName+"\\folder"+xx+"\\task.json";
Files.copy(new File(sourceTaskJsonFile).toPath(),new
File(destinationTaskJsonFile).toPath(),StandardCopyOption.REPLACE_EXISTING);
File [] fls=new File("task").listFiles();
for(File f:fls)
{
if(f.getName().equals("task.json")) continue;
if(f.getName().equals(dataFile)) continue;
Files.copy(f.toPath(),new
File(distFolderName+"\\folder"+xx+"\\"+f.getName()).toPath(),StandardCopyOption.REPLACE_EXISTING);
}
String zipFile="temp\\"+UUID.randomUUID().toString().replaceAll("-","g")+".zip";
TMZipUtility.zipIt(new File(distFolderName+"\\folder"+xx),zipFile);
taskZipFiles.add(zipFile);
TMUtility.deleteFolder(distFolderName+"\\folder"+xx);
xx++;
}
List<Thread> threads=new LinkedList<>();
String resultsFolder;
String key="";
while(true)
{
key=UUID.randomUUID().toString().replaceAll("-","k").substring(0,10);
resultsFolder="results"+key;
if(new File(resultsFolder).exists()==false) break;
}
File resultsFolderPath=new File(resultsFolder);
resultsFolderPath.mkdir();
xx=0;
for(SlaveNode worker:workers)
{
String taskZipFileName=taskZipFiles.get(xx);
Thread t=new Thread(){
public void run()
{
try
{
System.out.println("1");
Socket socket=new Socket(worker.getIp(),worker.getSlavePortNumber());
InputStream is=socket.getInputStream();
OutputStream os=socket.getOutputStream();
File file=new File(taskZipFileName);
FileInputStream fis=new FileInputStream(file);
BufferedInputStream bis=new BufferedInputStream(fis);
long lengthOfFile=file.length();
byte bytes[]=TMUtility.longToBytes(lengthOfFile);
System.out.println("11");
os.write(bytes);
os.flush();
System.out.println("111");
byte ack[]=new byte[2];
int rr=-1;
while(rr==-1) rr=is.read(ack);
byte dataBytes[]=new byte[1024];
System.out.println("1111");
while(true)
{
rr=bis.read(dataBytes);
if(rr<=0) break;
os.write(dataBytes,0,rr);
os.flush();
rr=-1;
while(rr==-1) rr=is.read(ack);
}
System.out.println("11111");
bytes=new byte[1024];
rr=-1;
while(rr==-1) rr=is.read(bytes);
if(bytes[0]==0) {
// complete it
socket.close();
return; }
lengthOfFile=TMUtility.bytesToLong(bytes,1,64);
os.write(ack);
os.flush();
System.out.println("111111");
String fileName=resultsFolderPath.getAbsolutePath()+"\\"+UUID.randomUUID().toString().replaceAll("-","k")+".zip";
FileOutputStream fos=new FileOutputStream(fileName);
BufferedOutputStream bos=new BufferedOutputStream(fos);
while(lengthOfFile>0) {
rr=is.read(bytes);
bos.write(bytes,0,rr);
bos.flush();
os.write(ack);
os.flush();
lengthOfFile-=rr; 
}
System.out.println("111111");
fos.close();
socket.close();
}catch(Exception exception) {
// something big needs to be done over here 
}
}
};
t.start();
threads.add(t); 
}
System.out.println("1111111");

for(Thread t:threads) 
{
try{
t.join();
}catch(InterruptedException ie){}
}
outputStream.write(new String("Task complete, key to save the file is \n"+key).getBytes());
outputStream.flush();
System.out.println("11111111");
continue;
}
if(getRequestType(bytes)==3) // status request
{
continue;
}
if(getRequestType(bytes)==4) // get result request
{
continue;
}
if(getRequestType(bytes)==5) // close
{
socket.close();
break;
}
}catch(Exception exception)
{
exception.printStackTrace();
}
}
map.remove(clientId);
System.out.println("Client disconnected, Id : "+clientId);
}
private int getRequestType(byte [] bytes)
{
int requestType=0;
for(int i=0;i<=7;i++)
{
requestType=requestType+(((int)Math.pow(2,i))*bytes[7-i]);
}
return requestType;
}
}// inner class ends
}// outer class ends