package com.lmy.iconcapturer.utils;

import android.content.Context;
import android.util.Log;

import com.elvishew.xlog.LogConfiguration;
import com.elvishew.xlog.LogLevel;
import com.elvishew.xlog.XLog;
import com.elvishew.xlog.flattener.ClassicFlattener;
import com.elvishew.xlog.printer.AndroidPrinter;
import com.elvishew.xlog.printer.ConsolePrinter;
import com.elvishew.xlog.printer.Printer;
import com.elvishew.xlog.printer.file.FilePrinter;
import com.elvishew.xlog.printer.file.backup.NeverBackupStrategy;
import com.elvishew.xlog.printer.file.clean.FileLastModifiedCleanStrategy;
import com.elvishew.xlog.printer.file.naming.DateFileNameGenerator;

import java.io.File;

public class LoggerUtil {

    private static final long MAX_TIME = 1000 * 3600 * 24;

    public static void LoggerInit(Context context){
        String logPath = context.getFilesDir().getPath();
        File file = new File(logPath + File.separator + "log");
        if (!file.exists()){
            file.mkdirs();
        }

        ClassicFlattener classicFlattener = new ClassicFlattener();

        Log.d("qfh", "日志保存路径为: "+ file.getPath());
        LogConfiguration config = new LogConfiguration.Builder()
                .logLevel(LogLevel.ALL)             // 指定日志级别，低于该级别的日志将不会被打印，默认为 LogLevel.ALL
                .tag("qfh")                                         // 指定 TAG，默认为 "X-LOG"
                .build();

        Printer androidPrinter = new AndroidPrinter(true);         // 通过 android.util.Log 打印日志的打印器
        Printer consolePrinter = new ConsolePrinter();             // 通过 System.out 打印日志到控制台的打印器
        Printer filePrinter = new FilePrinter                      // 打印日志到文件的打印器
                .Builder(file.getPath())                             // 指定保存日志文件的路径
                .cleanStrategy(new FileLastModifiedCleanStrategy(MAX_TIME))     // 指定日志文件清除策略，默认为 NeverCleanStrategy()
                .fileNameGenerator(new DateFileNameGenerator())        // 指定日志文件名生成器，默认为 ChangelessFileNameGenerator("log")
                .backupStrategy(new NeverBackupStrategy())             // 指定日志文件备份策略，默认为 FileSizeBackupStrategy(1024 * 1024)
                .flattener(classicFlattener)
                .build();

        XLog.init(                                                 // 初始化 XLog
                config,                                                // 指定日志配置，如果不指定，会默认使用 new LogConfiguration.Builder().build()
                androidPrinter,                                        // 添加任意多的打印器。如果没有添加任何打印器，会默认使用 AndroidPrinter(Android)/ConsolePrinter(java)
                consolePrinter,
                filePrinter);
    }
}
