package com.huawei.java.main;
import java.io.*;
import java.util.*;

/**
 * @author: 303014439
 * @date: 2021/3/10 19:41
 * @description:本版本采用图的映射与拼凑，期望舍弃运行时间以得到更优的价格搭配
 *
 */
public class Main {
    static int N,M,T,ID=1;
    //虚拟机增加队列List[天数]<String[型号][ID]>
    static List<String[]>[] reqAddList;
    //虚拟机删除队列List[天数]<String[ID]>
    static List<String[]>[] reqDelList;
    //服务器-虚拟机映射(服务器ID，该服务器上的虚拟机ID)(这里是否可以构造有序数列？)
    static Map<Integer,List> vmserverMap = new HashMap();
    //虚拟机-服务器映射(虚拟机ID，服务器ID[0],服务器节点[1])
    static Map<Integer,String[]> vmAddress = new HashMap<>();
    //已购入的服务器信息(服务器ID，服务器类型)
    static Map<Integer,String> serverType = new HashMap<>();
    //已购入的服务器资源使用情况(服务器ID，A点剩余CPU、B点剩余CPU、A点剩余内存、B点剩余内存)
    static Map<Integer,int[]> serverSource = new HashMap<>();
    //已获取的虚拟机信息(虚拟机ID，虚拟机类型)
    static Map<Integer,String> vmType = new HashMap<>();
    //维护一张某日服务器购入表(天数，服务器类型[0]，服务器数量[1])
    static Map<Integer,String[]> hadServer = new HashMap<>();
    //服务器信息读入
    public static Map<String,int[]> getServerInfos(Scanner scanner ,List<String[]> CPUServerList,List<String[]> MemoryServerList){
        //读入可以采购的服务器类型数量
        N = Integer.valueOf(scanner.nextLine());
        //创建服务器种类信息map
        Map<String,int[]> serverInfos = new HashMap();
        for(int i = 0;i < N;i++) {
            String Line = scanner.nextLine();
            //去掉头尾括号
            Line = Line.substring(1,Line.length()-1);
            String[] serverTxt = Line.split(",");
            //计算服务器性价比
            float Cost = Integer.parseInt(serverTxt[1].trim()) / (Integer.parseInt(serverTxt[1].trim()) + Integer.parseInt(serverTxt[2].trim()));
            //将服务器型号与属性存入map（key为服务器类型，value为（A点CPU数、B点CPU数、A点内存大小、B点内存大小、硬件成本。每日能耗成本））
            serverInfos.put(serverTxt[0],new int[]{Integer.parseInt(serverTxt[3].trim())/2,Integer.parseInt(serverTxt[1].trim())/2,Integer.parseInt(serverTxt[2].trim())/2,Integer.parseInt(serverTxt[2].trim())/2,Integer.parseInt(serverTxt[3].trim()),Integer.parseInt(serverTxt[4].trim())});
            //判断服务器类型
            if(Integer.parseInt(serverTxt[1].trim()) >= Integer.parseInt(serverTxt[2].trim())){
                //若为CPU密集型,将其添加至CPU密集型服务器类型列表
                CPUServerList.add(new String[]{serverTxt[0],serverTxt[1].trim(),serverTxt[2].trim(),String.valueOf(Cost)});
            }else{
                //若为内存密集型，将其添加到内存密集型服务器列表
                MemoryServerList.add(new String[]{serverTxt[0],serverTxt[1].trim(),serverTxt[2].trim(),String.valueOf(Cost)});
            }
        }
        //使用性价比对CPU密集型服务器列表与内存密集型服务器列表进行升序排序(这里的性价比计算公式算出的是倒数)
        Collections.sort(CPUServerList, new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                if(Float.valueOf(o1[3]) - Float.valueOf(o2[3]) > 0){
                    return 0;
                }else {
                    return 1;
                }
            }
        });
        Collections.sort(MemoryServerList, new Comparator<String[]>() {
            @Override
            public int compare(String[] o1, String[] o2) {
                if(Float.valueOf(o1[3]) - Float.valueOf(o2[3]) > 0){
                    return 0;
                }else {
                    return 1;
                }
            }
        });
        //此时得到了经过排序的两种服务器列表，以及一个服务器总表
        return serverInfos;
    }


    //该方法得到虚拟机的种类
    public static Map<String,int[]> getvmInfos(Scanner scanner){
        //读入虚拟机种类数量
        M = Integer.valueOf(scanner.nextLine());
        //我们考虑采用map集合存储虚拟机的种类信息
        //创建虚拟机种类信息map(key为虚拟机类型，value为(虚拟机cpu、虚拟机内存、双节点))
        Map<String,int[]> vmInfos = new HashMap();
        for(int i = 1;i <= M;i++){
            String Line = scanner.nextLine();
            Line = Line.substring(1,Line.length()-1);
            String[] vmTxt = Line.split(",");
            vmInfos.put(vmTxt[0],new int[]{Integer.parseInt(vmTxt[1].trim()),Integer.parseInt(vmTxt[2].trim()),Integer.parseInt(vmTxt[3].trim())});
        }
        return vmInfos;
    }


    //该方法得到用户请求增加/删除虚拟机的信息
    public static void getUserRequest(Scanner scanner){
        //读入用户请求数据数
        T = Integer.valueOf(scanner.nextLine());
        //初始化增加、删除队列,为了与请求天数匹配，从[1]记至[T]
        reqAddList = new ArrayList[T + 1];
        reqDelList = new ArrayList[T + 1];
        for(int i = 1 ; i <= T; i++){
            //初始化本天的增加、删除队列
            reqAddList[i] = new ArrayList<>();
            reqDelList[i] = new ArrayList<>();
            //得到本天的请求数
            int num = Integer.valueOf(scanner.nextLine());
//            System.out.println(num);
            int m = 0;
            while(m < num){
                String thisLine = scanner.nextLine();
                thisLine = thisLine.substring(1,thisLine.length()-1);
                String[] reqTxt = thisLine.split(", ");
                if(reqTxt[0].equals("add")){
                    //添加CPU请求数量,虚拟机的数量
                    reqAddList[i].add(new String[]{reqTxt[1],reqTxt[2]});
                }else{
                    reqDelList[i].add(new String[]{reqTxt[1]});
                }
                m++;
            }
        }
    }


    //本方法用于递归，根据给出的虚拟机ID，从服务器列表中进行遍历凑内存
//    public static void knock(Integer vid,Map<String,int[]> vmInfos,List<String[]> CPUServerList){
//        for (int id = 0; id < vmserverMap.size(); id++) {
//            //如果某台cpu的内存可以直接放入该台虚拟机，那么直接将其放入
//            //若为双节点，判断AB两点的值是否同时满足vm/2的资源数
//            if(vmInfos.get(vmType.get(vid))[2] == 1 && serverSource.get(Integer.valueOf(id))[0] > vmInfos.get(vmType.get(vid))[0]/2 && serverSource.get(Integer.valueOf(id))[1] > vmInfos.get(vmType.get(vid))[0]/2 && serverSource.get(Integer.valueOf(id))[2] > vmInfos.get(vmType.get(vid))[1]/2 && serverSource.get(Integer.valueOf(id))[3] > vmInfos.get(vmType.get(vid))[1]/2){
//                //若满足，则直接在这里存储该虚拟机，将三张资源表更新
//                //服务器-虚拟机映射表
//                List list = vmserverMap.get(Integer.valueOf(id));
//                list.add(vid);
//                vmserverMap.put(Integer.valueOf(id),list);
//                //服务器资源余裕表
//                int[] ints = serverSource.get(Integer.valueOf(id));
//                ints[0] -= vmInfos.get(vmType.get(vid))[0]/2;
//                ints[1] -= vmInfos.get(vmType.get(vid))[0]/2;
//                ints[2] -= vmInfos.get(vmType.get(vid))[1]/2;
//                ints[3] -= vmInfos.get(vmType.get(vid))[1]/2;
//                serverSource.put(Integer.valueOf(id),ints);
//                //虚拟机-服务器地址表
//                vmAddress.put(vid,new String[]{String.valueOf(id),"AB"});
//                //已存入且未操作其他虚拟机，直接退出函数
//                break;
//            }else if(vmInfos.get(vmType.get(vid))[2] != 1 && serverSource.get(Integer.valueOf(id))[0] > vmInfos.get(vmType.get(vid))[0] && serverSource.get(Integer.valueOf(id))[2] > vmInfos.get(vmType.get(vid))[1]){
//                //若不为双节点，且A点的资源充足，则在A点插入，将三张资源表更新
//                List list = vmserverMap.get(Integer.valueOf(id));
//                list.add(vid);
//                vmserverMap.put(Integer.valueOf(id),list);
//                int[] ints = serverSource.get(Integer.valueOf(id));
//                ints[0] -= vmInfos.get(vmType.get(vid))[0];
//                ints[2] -= vmInfos.get(vmType.get(vid))[1];
//                serverSource.put(Integer.valueOf(id),ints);
//                vmAddress.put(vid,new String[]{String.valueOf(id),"A"});
//                break;
//            }else if(vmInfos.get(vmType.get(vid))[2] != 1 && serverSource.get(Integer.valueOf(id))[1] > vmInfos.get(vmType.get(vid))[0] && serverSource.get(Integer.valueOf(id))[3] > vmInfos.get(vmType.get(vid))[1]){
//                //若不为双节点，且B点的资源充足，则在B点插入
//                List list = vmserverMap.get(Integer.valueOf(id));
//                list.add(vid);
//                vmserverMap.put(Integer.valueOf(id),list);
//                int[] ints = serverSource.get(Integer.valueOf(id));
//                ints[1] -= vmInfos.get(vmType.get(vid))[0];
//                ints[3] -= vmInfos.get(vmType.get(vid))[1];
//                serverSource.put(Integer.valueOf(id),ints);
//                vmAddress.put(vid,new String[]{String.valueOf(id),"B"});
//                break;
//            }else{
//                //若无法直接插入，则进行凑数
//                //判断是否为双节点虚拟机
//                if(vmInfos.get(vmType.get(vid))[2] == 1){
//                    //若为双节点。则从本ID服务器里选出双节点虚拟机进行凑数
//                    //得到该服务器上的虚拟机IDlist
//                    List<Integer> vmlist = vmserverMap.get(Integer.valueOf(id));
//                    // 增强的for循环遍历List
//                    for(Integer vmid : vmlist){
//                        if(vmAddress.get(vmid)[1].equals("AB")){
//                            //若本台机器为双节点，则计算资源空隙能否更小,因为两节点变化的资源数是一样的，所以仅看A节点即可判断
//                            Integer CPUbalance = serverSource.get(Integer.valueOf(id))[0];
//                            Integer Memorybalance = serverSource.get(Integer.valueOf(id))[2];
//                            if((CPUbalance + vmInfos.get(vmType.get(vmid))[0]/2 - vmInfos.get(vmType.get(vid))[0]/2) > 0 && (Memorybalance + vmInfos.get(vmType.get(vmid))[1]/2 - vmInfos.get(vmType.get(vid))[1]/2) > 0){
//                                //则可以挤走该虚拟机，更新维护的三张表
//                                List list = vmserverMap.get(id);
//                                list.remove(Integer.valueOf(vmid));
//                                list.add(Integer.valueOf(vid));
//                                vmserverMap.put(id,list);
//                                vmAddress.put(vid,new String[]{String.valueOf(id),"AB"});
//                                serverSource.get(Integer.valueOf(id))[0] = serverSource.get(Integer.valueOf(id))[0] + vmInfos.get(vmType.get(vmid))[0]/2 - vmInfos.get(vmType.get(vid))[0]/2;
//                                serverSource.get(Integer.valueOf(id))[1] = serverSource.get(Integer.valueOf(id))[1] + vmInfos.get(vmType.get(vmid))[0]/2 - vmInfos.get(vmType.get(vid))[0]/2;
//                                serverSource.get(Integer.valueOf(id))[2] = serverSource.get(Integer.valueOf(id))[2] + vmInfos.get(vmType.get(vmid))[1]/2 - vmInfos.get(vmType.get(vid))[1]/2;
//                                serverSource.get(Integer.valueOf(id))[3] = serverSource.get(Integer.valueOf(id))[3] + vmInfos.get(vmType.get(vmid))[1]/2 - vmInfos.get(vmType.get(vid))[1]/2;
//                                //递归分配被赶走的虚拟机
//                                knock(vmid,vmInfos,CPUServerList);
//                                break;//跳出增强for循环
//                            }//若没有成功凑出，则进行下一轮循环
//                        }
//                    }
//                }else{
//                    //从单节点中寻找虚拟机进行凑数,被挤走的虚拟机
//                    //得到该服务器上的虚拟机list
//                    List<Integer> vmlist = vmserverMap.get(Integer.valueOf(id));
//                    //增强的for循环进行遍历
//                    for(Integer vmid : vmlist){
//                        if(!vmAddress.get(vmid)[1].equals("AB")){
//                            if(vmAddress.get(vmid)[1].equals("A")){
//                                //使用A点资源进行判断
//                                Integer CPUbalance = serverSource.get(Integer.valueOf(id))[0];
//                                Integer Memorybalance = serverSource.get(Integer.valueOf(id))[2];
//                                if((CPUbalance + vmInfos.get(vmType.get(vmid))[0] - vmInfos.get(vmType.get(vid))[0]) > 0 && (Memorybalance + vmInfos.get(vmType.get(vmid))[1] - vmInfos.get(vmType.get(vid))[1]) > 0){
//                                    //更新分配表
//                                    List list = vmserverMap.get(id);
//                                    list.remove(Integer.valueOf(vmid));
//                                    list.add(Integer.valueOf(vid));
//                                    vmserverMap.put(id,list);
//                                    vmAddress.put(vid,new String[]{String.valueOf(id),"A"});
//                                    serverSource.get(Integer.valueOf(id))[0] = serverSource.get(Integer.valueOf(id))[0] + vmInfos.get(vmType.get(vmid))[0] - vmInfos.get(vmType.get(vid))[0];
//                                    serverSource.get(Integer.valueOf(id))[2] = serverSource.get(Integer.valueOf(id))[2] + vmInfos.get(vmType.get(vmid))[1] - vmInfos.get(vmType.get(vid))[1];
//                                    knock(vmid,vmInfos,CPUServerList);
//                                    break;//跳出增强for循环
//                                }
//                            }else if(vmAddress.get(vmid)[1].equals("B")){
//                                //使用B点资源进行判断
//                                Integer CPUbalance = serverSource.get(Integer.valueOf(id))[1];
//                                Integer Memorybalance = serverSource.get(Integer.valueOf(id))[3];
//                                if((CPUbalance + vmInfos.get(vmType.get(vmid))[0] - vmInfos.get(vmType.get(vid))[0]) > 0 && (Memorybalance + vmInfos.get(vmType.get(vmid))[1] - vmInfos.get(vmType.get(vid))[1]) > 0){
//                                    //更新分配表
//                                    List list = vmserverMap.get(id);
//                                    list.remove(Integer.valueOf(vmid));
//                                    list.add(Integer.valueOf(vid));
//                                    vmserverMap.put(id,list);
//                                    vmAddress.put(vid,new String[]{String.valueOf(id),"B"});
//                                    serverSource.get(Integer.valueOf(id))[1] = serverSource.get(Integer.valueOf(id))[1] + vmInfos.get(vmType.get(vmid))[0] - vmInfos.get(vmType.get(vid))[0];
//                                    serverSource.get(Integer.valueOf(id))[3] = serverSource.get(Integer.valueOf(id))[3] + vmInfos.get(vmType.get(vmid))[1] - vmInfos.get(vmType.get(vid))[1];
//                                    knock(vmid,vmInfos,CPUServerList);
//                                    break;//跳出增强for循环
//                                }
//                            }
//                        }
//                    }
//                }
//            }//若没有成功凑出，则进行下一轮循环
//            //若最后一台虚拟机都无法分配出新的空间，则开启新的虚拟机
//            if(id == vmserverMap.size()){
//                vmserverMap.put(new Integer(0), new ArrayList());
//                serverType.put(new Integer(0), CPUServerList.get(0)[0]);
//                serverSource.put(new Integer(0), new int[]{Integer.valueOf(CPUServerList.get(0)[1]) / 2, Integer.valueOf(CPUServerList.get(0)[1]) / 2, Integer.valueOf(CPUServerList.get(0)[2]) / 2, Integer.valueOf(CPUServerList.get(0)[2]) / 2});
//                //进入最后一次循环
//            }
//        }
//    }


    //该方法使用图的匹配凑出最合适的虚拟机搭配，按天执行
    public static void distribute(int day,List<String[]> reqAddList[] ,List<String[]> reqDelList[],Map<String,int[]> vmInfos, List<String[]> CPUServerList, List<String[]> MemoryServerList) {
        //维护一张分配指令表,存储所有今天加载的虚拟机的分配指令
        String[] vmdis = new String[reqAddList[day].size()];
        int flag = 0;//flag为1时表示CPU更多
        int CPU = 0, memory = 0;
        //当天购入的虚拟机数量
        int buynum = 1;
        //分配输出信息集
        String[] purchase = new String[reqAddList[day].size()];
        //调度输出信息集
        String[] migration = new String[reqAddList[day].size()];
        int size = reqAddList[day].size();
//        System.out.println(size);
        //判断今天的虚拟机请求队列需要CPU更多还是内存更多
        for (int i = 0; i < size; i++) {
            CPU += vmInfos.get(reqAddList[day].get(i)[0])[0];
            memory += vmInfos.get(reqAddList[day].get(i)[0])[1];
        }
        if (CPU > memory) flag = 1;
        //如果虚拟机列表为空，则直接增加虚拟机，记录编号为1
        if (vmserverMap.size() == 0) {
            if (flag == 1) {
                vmserverMap.put(new Integer(0), new ArrayList());
                serverType.put(new Integer(0), CPUServerList.get(0)[0]);
                serverSource.put(new Integer(0), new int[]{Integer.valueOf(CPUServerList.get(0)[1]) / 2, Integer.valueOf(CPUServerList.get(0)[1]) / 2, Integer.valueOf(CPUServerList.get(0)[2]) / 2, Integer.valueOf(CPUServerList.get(0)[2]) / 2});
                hadServer.put(1,new String[]{CPUServerList.get(0)[0],"1"});
            } else {
                vmserverMap.put(new Integer(0), new ArrayList());
                serverType.put(new Integer(0), MemoryServerList.get(0)[0]);
                serverSource.put(new Integer(0), new int[]{Integer.valueOf(MemoryServerList.get(0)[1]) / 2, Integer.valueOf(MemoryServerList.get(0)[1]) / 2, Integer.valueOf(MemoryServerList.get(0)[2]) / 2, Integer.valueOf(MemoryServerList.get(0)[2]) / 2});
                hadServer.put(1,new String[]{MemoryServerList.get(0)[0],"1"});
            }
        }
        //对于当前天某一请求的虚拟机
        for(int i = 0; i < size; i++){
            //虚拟机类型映射表
            vmType.put(Integer.valueOf(reqAddList[day].get(i)[1]),reqAddList[day].get(i)[0]);
            //从ID为0的服务器开始，逐个遍历，尝试使得资源空闲更小
//            knock(Integer.valueOf(reqAddList[day].get(i)[1]),vmInfos,CPUServerList);
            /*
            中和代码，暂时不凑虚拟机，而是进行遍历寻找插入可能
             */
            Integer vid =Integer.valueOf(reqAddList[day].get(i)[1]);
            for (int id = 0; id < vmserverMap.size(); id++) {
                //如果某台cpu的内存可以直接放入该台虚拟机，那么直接将其放入
                //若为双节点，判断AB两点的值是否同时满足vm/2的资源数
                if (vmInfos.get(vmType.get(vid))[2] == 1 && serverSource.get(Integer.valueOf(id))[0] > vmInfos.get(vmType.get(vid))[0]/2+1 && serverSource.get(Integer.valueOf(id))[1] > vmInfos.get(vmType.get(vid))[0]/2+1 && serverSource.get(Integer.valueOf(id))[2] > vmInfos.get(vmType.get(vid))[1]/2+1 && serverSource.get(Integer.valueOf(id))[3] > vmInfos.get(vmType.get(vid))[1]/2+1) {
                    //若满足，则直接在这里存储该虚拟机，将三张资源表更新
                    //服务器-虚拟机映射表
                    List list = vmserverMap.get(Integer.valueOf(id));
                    list.add(vid);
                    vmserverMap.put(Integer.valueOf(id), list);
                    //服务器资源余裕表
                    int[] ints = serverSource.get(Integer.valueOf(id));
                    //测试代码
//                    System.out.println(ints[0]);
                    ints[0] -= vmInfos.get(vmType.get(vid))[0] / 2;
                    ints[1] -= vmInfos.get(vmType.get(vid))[0] / 2;
                    ints[2] -= vmInfos.get(vmType.get(vid))[1] / 2;
                    ints[3] -= vmInfos.get(vmType.get(vid))[1] / 2;
                    serverSource.put(Integer.valueOf(id), ints);
                    //虚拟机-服务器地址表
                    vmAddress.put(vid, new String[]{String.valueOf(id), "AB"});
                    vmdis[i] = "(" + id + ")";
                    //已存入且未操作其他虚拟机，直接退出此次循环
                    break;
                } else if (vmInfos.get(vmType.get(vid))[2] != 1 && serverSource.get(Integer.valueOf(id))[0] > vmInfos.get(vmType.get(vid))[0]+1 && serverSource.get(Integer.valueOf(id))[2] > vmInfos.get(vmType.get(vid))[1]+1) {
                    //若不为双节点，且A点的资源充足，则在A点插入，将三张资源表更新
                    List list = vmserverMap.get(Integer.valueOf(id));
                    list.add(vid);
                    vmserverMap.put(Integer.valueOf(id), list);
                    int[] ints = serverSource.get(Integer.valueOf(id));
                    //测试代码
//                    System.out.println(ints[0]);
                    ints[0] -= vmInfos.get(vmType.get(vid))[0];
                    ints[2] -= vmInfos.get(vmType.get(vid))[1];
                    serverSource.put(Integer.valueOf(id), ints);
                    vmAddress.put(vid, new String[]{String.valueOf(id), "A"});
                    vmdis[i] = "(" + id + ", A" +")";
                    break;
                } else if (vmInfos.get(vmType.get(vid))[2] != 1 && serverSource.get(Integer.valueOf(id))[1] > vmInfos.get(vmType.get(vid))[0]+1 && serverSource.get(Integer.valueOf(id))[3] > vmInfos.get(vmType.get(vid))[1]+1) {
                    //若不为双节点，且B点的资源充足，则在B点插入
                    List list = vmserverMap.get(Integer.valueOf(id));
                    list.add(vid);
                    vmserverMap.put(Integer.valueOf(id), list);
                    int[] ints = serverSource.get(Integer.valueOf(id));
                    //测试代码
//                    System.out.println(ints[1]);
                    ints[1] -= vmInfos.get(vmType.get(vid))[0];
                    ints[3] -= vmInfos.get(vmType.get(vid))[1];
                    serverSource.put(Integer.valueOf(id), ints);
                    vmAddress.put(vid, new String[]{String.valueOf(id), "B"});
                    vmdis[i] = "(" + id + ", B" +")";
                    break;
                }
                //若资源不足，则分配新的虚拟机
                if(id == vmserverMap.size()-1){
                    if(hadServer.get(day) == null){
                        //没有购入任何类型虚拟机时，第一台可以随意购入
                        vmserverMap.put(new Integer(id + 1), new ArrayList());
                        serverType.put(new Integer(id + 1), CPUServerList.get(0)[0]);
                        serverSource.put(new Integer(id + 1), new int[]{Integer.valueOf(CPUServerList.get(0)[1]) / 2, Integer.valueOf(CPUServerList.get(0)[1]) / 2, Integer.valueOf(CPUServerList.get(0)[2]) / 2, Integer.valueOf(CPUServerList.get(0)[2]) / 2});
                        hadServer.put(day,new String[]{CPUServerList.get(0)[0],String.valueOf(buynum)});
                        buynum ++;
                    }else{
                        //为了保证数据结构的稳定性，需要和第一台购入的类型一样
                        vmserverMap.put(new Integer(id + 1), new ArrayList());
                        serverType.put(new Integer(id + 1), hadServer.get(day)[0]);
                        serverSource.put(new Integer(id + 1), new int[]{Integer.valueOf(CPUServerList.get(0)[1]) / 2, Integer.valueOf(CPUServerList.get(0)[1]) / 2, Integer.valueOf(CPUServerList.get(0)[2]) / 2, Integer.valueOf(CPUServerList.get(0)[2]) / 2});
                        if(day == 1){
                            hadServer.put(day,new String[]{hadServer.get(day)[0],String.valueOf(buynum + 1)});
                            buynum ++;
                        }else{
                            hadServer.put(day,new String[]{hadServer.get(day)[0],String.valueOf(buynum )});
                            buynum ++;
                        }
                        buynum ++;
                    }
                }
            }
            //如果最后一台虚拟机分配结束
            if(i == size-1){
                //输出今天的购买，分配信息
                if(hadServer.get(day) != null){
                    System.out.println("(purchase, 1)");
                    System.out.println("(" + hadServer.get(day)[0] + ", " + hadServer.get(day)[1] + ")");
                }else{
                    System.out.println("(purchase, 0)");
                }
                System.out.println("(migration, 0)");
                for(int m =0;m < size;m++){
                    System.out.println(vmdis[m]);
                }
            }
        }
        //进行删除操作,
        for(int i = 0; i < reqDelList[day].size() -1; i++){
            //寻找到对应服务器ID与节点
//            System.out.println(reqDelList[day]);
//            System.out.println("ID :" + Integer.valueOf(reqDelList[day].get(i)[0]));
//            System.out.println("i" + i + "day "  + day + "size" + reqDelList[day].size());
//            System.out.println(vmAddress.get(Integer.valueOf(reqDelList[day].get(i)[0])));
            //得到服务器的ID
            int ID = Integer.valueOf(vmAddress.get(Integer.valueOf(reqDelList[day].get(i)[0]))[0]);
//            System.out.println(reqDelList[day].get(i)[0]);
//            System.out.println(ID);
            String point = vmAddress.get(Integer.valueOf(reqDelList[day].get(i)[0]))[1];
//            System.out.println(point);
            //更新维护的资源表
            List list = vmserverMap.get(ID);
            //这里删除的数据是Integer类型的
            list.remove(Integer.valueOf(reqDelList[day].get(i)[0]));
            vmserverMap.put(ID,list);
            //这里删除的是Integer类型
            vmAddress.remove(Integer.valueOf(reqDelList[day].get(i)[0]));
            if(point.equals("AB")){
                //如果要释放的是双节点虚拟机
                serverSource.get(ID)[0] += vmInfos.get(vmType.get(Integer.valueOf(reqDelList[day].get(i)[0])))[0]/2;
//                System.out.println(vmInfos.get(vmType.get(Integer.valueOf(reqDelList[day].get(i)[0])))[0]/2);
                serverSource.get(ID)[1] += vmInfos.get(vmType.get(Integer.valueOf(reqDelList[day].get(i)[0])))[0]/2;
                serverSource.get(ID)[2] += vmInfos.get(vmType.get(Integer.valueOf(reqDelList[day].get(i)[0])))[1]/2;
                serverSource.get(ID)[3] += vmInfos.get(vmType.get(Integer.valueOf(reqDelList[day].get(i)[0])))[1]/2;
            }else if(point.equals("A")){
                serverSource.get(ID)[0] += vmInfos.get(vmType.get(Integer.valueOf(reqDelList[day].get(i)[0])))[0];
                serverSource.get(ID)[2] += vmInfos.get(vmType.get(Integer.valueOf(reqDelList[day].get(i)[0])))[1];
            }else if(point.equals("B")){
                serverSource.get(ID)[1] += vmInfos.get(vmType.get(Integer.valueOf(reqDelList[day].get(i)[0])))[0];
                serverSource.get(ID)[3] += vmInfos.get(vmType.get(Integer.valueOf(reqDelList[day].get(i)[0])))[1];
            }
        }
    }


    //调度算法,按天执行
    public static void dispatch(){
        System.out.println("(migration, 0)");
    }


    public static void main(String[] args) throws FileNotFoundException {
        //服务器种类信息（型号，A点CPU[0]、B点CPU[1]、A点内存[2]、B点内存[3]、硬件成本[4]、能耗成本[5]、性价比[6]、CPU密集型[7]）
        Map<String,int[]> serverInfos;
        //虚拟机种类信息(key为虚拟机类型，value为(虚拟机cpu、虚拟机内存、双节点))
        Map<String,int[]> vmInfos;
        //所有时间的用户请求信息
        //创建CPU密集型服务器类型列表<型号[0]，CPU数[1]，内存数[2]，性价比[3]>
        List<String[]> CPUServerList = new ArrayList<>();
        //创建内存密集型服务器类型列表<型号[0]，CPU数[1]，内存数[2]，性价比[3]>
        List<String[]> MemoryServerList = new ArrayList<>();
//        FileInputStream file = new FileInputStream("D:\\大三下\\华为精英挑战赛\\官方文件\\training-data\\training-1.txt");
//        System.setIn(file);
        Scanner scanner = new Scanner(System.in);
        //获得服务器类型总表，CPU密集型表，内存密集型表
        serverInfos = getServerInfos(scanner,CPUServerList,MemoryServerList);
        //获得虚拟机类型表
        vmInfos = getvmInfos(scanner);
        //得到虚拟机请求队列
        getUserRequest(scanner);
        //进行服务器的购买与虚拟机的分配，按天执行
        for(int i = 1 ; i <= T ; i++){
            distribute(i,reqAddList,reqDelList,vmInfos,CPUServerList,MemoryServerList);
        }
        //输出每日分配

//        System.out.println(vmserverMap.toString());
    }
}
