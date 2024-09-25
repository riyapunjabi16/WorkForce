package com.thinking.machines.workforce.slave;
import com.thinking.machines.utils.*;
import java.net.*;
import java.io.*;
public class SlaveStarter {
public static void main(String gg[]) {
try{
int portNumber=Integer.parseInt(gg[0]);
int slavePortNumber=Integer.parseInt(gg[1]);
ServerSocket serverSocket=new ServerSocket(portNumber);
Socket socket;
byte bytes[];
byte ack[]=new byte[2];
int cnt;
while(true) {
socket=serverSocket.accept();
InputStream is=socket.getInputStream();
OutputStream os=socket.getOutputStream();
bytes=new byte[1024];
cnt=-1;
while(cnt==-1) cnt=is.read(bytes);
if(bytes[0]==1) {
try{
Slave slave=new Slave(slavePortNumber);
slave.start();
bytes[0]=1;
byte b[]=TMUtility.longToBytes(slavePortNumber);
int j=1;
for(int i=48;i<b.length;i++,j++) bytes[j]=b[i];
}catch(Exception exception) {
bytes[0]=0; }
os.write(bytes);
os.flush();
int rr=-1;
while(rr==-1) rr=is.read(ack); }}
}catch(Exception e) {
e.printStackTrace(); }}
}