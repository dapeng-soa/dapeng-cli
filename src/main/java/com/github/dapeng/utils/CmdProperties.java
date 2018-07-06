package com.github.dapeng.utils;

public class CmdProperties {

    //cmd properties
    public static final String KEY_ARGS_METADATA = "-metadata";
    public static final String KEY_ARGS_ECHO = "-echo";
    //public static final String KEY_ARGS_CONFIG = "-c";

    //public static final String KEY_ARGS_PATH = "-dir";

    // public static final String KEY_ARGS_USER = "-u";
    // public static final String KEY_ARGS_SERVICE_CALL = "-call";
    //public static final String KEY_ARGS_PASSWD = "-p";
    //public static final String KEY_ARGS_HOST = "-h";
    //public static final String KEY_ARGS_RUNTIME = "-r";
    //public static final String KEY_ARGS_FILE = "-f";

    //review
    public static final String KEY_ARGS_SERVICE_METHOD = "-m";
    public static final String KEY_ARGS_DATA = "-d";
    public static final String KEY_ARGS_FILE_READ = "-f";
    public static final String KEY_ARGS_FILE_OUT = "-o";
    public static final String KEY_ARGS_SERVICE = "-s";
    public static final String KEY_ARGS_VERSION = "-v";

    public static final String KEY_ARGS_ZK_GET = "-get";
    public static final String KEY_ARGS_ZK_SET = "-set";
    public static final String KEY_ARGS_ZK_NODE = "-nodes";

    public static final String KEY_ARGS_LIST = "-list";
    public static final String KEY_ARGS_ROUTE = "-route";
    public static final String KEY_ARGS_RUNTIME = "-runtime";
    public static final String KEY_ARGS_CONFIG = "-config";
    public static final String KEY_ARGS_WHITELIST = "-whitelist";
    public static final String KEY_ARGS_METHOD = "-method";
    public static final String KEY_ARGS_DATE = "-date";

    public static final String KEY_SOA_ZOO_KEEPER_HOST = "soa.zookeeper.host";

    //log
    public static final String KEY_ARGS_HOSTNAME = "-hostname";
    public static final String KEY_ARGS_SESSIONTID = "-sessiontid";
    public static final String KEY_ARGS_TAG = "-tag";
    public static final String KEY_ARGS_THREADPOOL = "-threadpool";
    public static final String KEY_ARGS_SLOGTIME = "-slogtime";
    public static final String KEY_ARGS_ELOGTIME = "-elogtime";



    //zookeeper properties
    public static final String CONFIG_PATH = "/soa/config/services";
    public static final String WHITELIST_PATH = "/soa/whitelist/services";
    public static final String RUNTIME_PATH = "/soa/runtime/services";
    public static final String ROUTE_PATH = "/soa/config/routes";


    public static final String OPEN_API_TIMEOUT = "soa.service.timeout";

    public static final String DEFAULT_ZK_IP = "127.0.0.1";
    public static final String DEFAULT_ZK_PORT = "2181";
    public static final String DEFAULT_ZK_HOST = "127.0.0.1:2181";

}
