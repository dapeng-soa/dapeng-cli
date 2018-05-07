package com.github.dapeng.test;

import com.github.dapeng.metadata.MetadataClient;
import com.github.dapeng.openapi.cache.ZkBootstrap;
import com.github.dapeng.utils.ServiceUtils;

public class ServiceInvokeTest {

    public static void main(String[] args) throws Exception {


        MetadataClient client = new MetadataClient("com.isuwang.soa.user.service.UserService","1.0.0");
        String data = client.getServiceMetadata();

//        FileWriter fw = null;
//        try {
//            File file = new File("/home/jack/tmp/userService.xml");
//            fw = new FileWriter(file);
//            fw.write(data);
//            fw.flush();
//        } catch (Exception e) {
//            System.out.println(" Failed to generate xml file");
//        } finally {
//            fw.close();
//        }

        ZkBootstrap zkBootstrap = new ZkBootstrap();
        zkBootstrap.init();

        Thread.sleep(10000);

        String params = ServiceUtils.readFromeFile("/home/jack/tmp/request.json");
        System.out.println(" param: ");
        System.out.println(params);

        ServiceUtils.post("com.isuwang.soa.user.service.UserService", "1.0.0", "createUser", params);

       // System.out.println(data);
    }
}
