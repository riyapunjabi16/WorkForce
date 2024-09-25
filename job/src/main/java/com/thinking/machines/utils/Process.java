package com.thinking.machines.utils;
import java.util.*;
import java.util.stream.*;
public class Process 
{
public List<String> processIt(List<String> list)
{
return list.stream().filter((j)->{return (j.charAt(0)>=48 && j.charAt(0)<=57);}).collect(Collectors.toList());
}
}