package com.v.log;


import com.v.log.Printer.DiskLogPrinter;
import com.v.log.Printer.Printer;

import java.util.List;

/**
 * @author dongyk on 2019/2/22
 */
public class CommonInfoWriter {

    private CommonInfoWriter(){

    }

    private static class CommonInfoWriterHolder{
        private static final CommonInfoWriter instance = new CommonInfoWriter();
    }

    public static CommonInfoWriter getInstance(){
        return CommonInfoWriterHolder.instance;
    }

    public void writeCommonInfo(){
        List<Printer> printers = VLog.getLogger().getPrinters();
        if (printers == null || printers.size() == 0) return;
        for (int i = 0; i < printers.size(); i++) {
            Printer printer = printers.get(i);
            if (printer != null && printer instanceof DiskLogPrinter){
                ((DiskLogPrinter) printer).writeCommonInfo();
            }
        }
    }

}
