package com.thinking.machines.utils;
import java.io.*;
import java.util.zip.*;
import java.util.*;
public class TMZipUtility
{
static public void zipDirectory(File directory,ZipOutputStream zos,String prefix)
{
try{
List<File> directories=new LinkedList<>();
File [] files=directory.listFiles();
byte[] bytes;
FileInputStream fis;
for(File file:files) 
{
if(file.isDirectory()) 
{
directories.add(file); 
}
else 
{
bytes=new byte[1024];
fis=new FileInputStream(file);
zos.putNextEntry(new ZipEntry(prefix+file.getName()));
int count;
while(true) 
{
count=fis.read(bytes);
if(count==-1) break;
zos.write(bytes,0,count); 
}
fis.close();
zos.closeEntry(); 
}
}
for(File subDirectory:directories) 
{
zipDirectory(subDirectory,zos,prefix+"/"+subDirectory.getName()+"/"); 
}
}catch(Exception exception) 
{
exception.printStackTrace(); 
}
}
public static void zipIt(File folder,String zipFileName) 
{
try{
String targetZipPath=zipFileName;
File zipFile=new File(targetZipPath);
if(zipFile.exists()) zipFile.delete();
FileOutputStream fos=new FileOutputStream(zipFile);
ZipOutputStream zipOutputStream=new ZipOutputStream(fos);
zipDirectory(folder,zipOutputStream,"/");
zipOutputStream.close();
}catch(Exception e) 
{
e.printStackTrace(); 
}
}
public static void unzipIt(String zipFilePath,String destinationDirectory) 
{
File directory=new File(destinationDirectory);
if(!directory.exists()) directory.mkdirs();
FileInputStream fileInputStream;
byte[] buffer=new byte[1024];
try {
fileInputStream=new FileInputStream(zipFilePath);
ZipInputStream zipInputStream=new ZipInputStream(fileInputStream);
ZipEntry zipEntry=zipInputStream.getNextEntry();
while(zipEntry!=null) 
{
String fileName=zipEntry.getName();
File newFile=new File(destinationDirectory+File.separator+fileName);
new File(newFile.getParent()).mkdirs();
FileOutputStream fileOutputStream=new FileOutputStream(newFile);
int length;
while(true) 
{
length=zipInputStream.read(buffer);
if(length<=0) break;
fileOutputStream.write(buffer,0,length); 
}
fileOutputStream.close();
zipInputStream.closeEntry();
zipEntry=zipInputStream.getNextEntry(); 
}
zipInputStream.closeEntry();
zipInputStream.close();
fileInputStream.close();
}catch(IOException e) 
{
e.printStackTrace(); 
}
}
}