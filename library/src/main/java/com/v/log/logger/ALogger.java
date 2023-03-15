package com.v.log.logger;

import android.text.TextUtils;


import com.v.log.Printer.Printer;
import com.v.log.util.LogUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


public class ALogger implements Logger {


    private static final int JSON_INDENT = 2;

    private final List<Printer> logPrinters = new ArrayList<>();

    public ALogger() {
    }

    @Override
    public void d(String tag, Boolean save, String message, Object... args) {
        log(DEBUG, null, tag, save, message, args);
    }

    @Override
    public void e(String tag, Boolean save, String message, Object... args) {
        e(tag, save, null, message, args);
    }

    @Override
    public void e(String tag, Boolean save, Throwable throwable, String message, Object... args) {
        log(ERROR, throwable, tag, save, message, args);
    }

    @Override
    public void w(String tag, Boolean save, String message, Object... args) {
        log(WARN, null, tag, save, message, args);
    }

    @Override
    public void i(String tag, Boolean save, String message, Object... args) {
        log(INFO, null, tag, save, message, args);
    }

    @Override
    public void v(String tag, Boolean save, String message, Object... args) {
        log(VERBOSE, null, tag, save, message, args);
    }


    @Override
    public void json(String tag, Boolean save, String json) {
        if (TextUtils.isEmpty(json)) {
            e(tag, save, "Empty/Null json content");
            return;
        }
        try {
            json = json.trim();
            if (json.startsWith("{")) {
                JSONObject jsonObject = new JSONObject(json);
                String message = jsonObject.toString(JSON_INDENT);
                d(tag, save, message);
                return;
            }
            if (json.startsWith("[")) {
                JSONArray jsonArray = new JSONArray(json);
                String message = jsonArray.toString(JSON_INDENT);
                d(tag, save, message);
                return;
            }
            e(tag, save, "Invalid Json");
        } catch (JSONException e) {
            e(tag, save, "Invalid Json");
        }
    }

    @Override
    public void xml(String tag, Boolean save, String xml) {
        if (TextUtils.isEmpty(xml)) {
            e(tag, save, "Empty/Null xml content");
            return;
        }
        try {
            Source xmlInput = new StreamSource(new StringReader(xml));
            StreamResult xmlOutput = new StreamResult(new StringWriter());
            Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
            transformer.transform(xmlInput, xmlOutput);
            d(tag, save, xmlOutput.getWriter().toString().replaceFirst(">", ">\n"));
        } catch (TransformerException e) {
            e(tag, save, "Invalid xml");
        }
    }

    @Override
    public synchronized void log(int priority, String tag, Boolean save, String message, Throwable throwable) {
        if (throwable != null && message != null) {
            message += " : " + LogUtils.getStackTraceString(throwable);
        }
        if (throwable != null && message == null) {
            message = LogUtils.getStackTraceString(throwable);
        }
        if (TextUtils.isEmpty(message)) {
            message = "Empty/NULL log message";
        }

        for (Printer printer : logPrinters) {
            if (printer.isLoggable(priority, tag)) {
                printer.log(priority, tag, message, save);
            }
        }
    }

    @Override
    public void flush() {
        for (Printer printer : logPrinters) {
            printer.flush();
        }
    }

    @Override
    public void addPrinter(Printer printer) {
        logPrinters.add(printer);
    }

    @Override
    public List<Printer> getPrinters() {
        return logPrinters;
    }

    @Override
    public void clearLogPrinters() {

    }

    private synchronized void log(int priority, Throwable throwable, String tag, Boolean save, String msg, Object... args) {
        String message = createMessage(msg, args);
        log(priority, tag, save, message, throwable);
    }

    private String createMessage(String message, Object... args) {
        if (message == null) {
            return null;
        }
        return args == null || args.length == 0 ? message : String.format(message, args);
    }
}
