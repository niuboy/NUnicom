package com.xoes.nunicom;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.logging.*;

public class LogFactory {
    private static Logger logger = Logger.getGlobal();
    private final static String LOG_PATH = "nunicom.log";
    private final static DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
    private final static Formatter formatter = new Formatter() {
        @Override
        public String format(LogRecord record) {
            if (record.getMessage().isBlank()) return "";
            StringBuilder sb = new StringBuilder(getNow());
            sb.append(" ").append(record.getLevel().getName());
            sb.append("\t").append(record.getSourceClassName())
                    .append(".").append(record.getSourceMethodName());
            sb.append("\t信息：").append(record.getMessage()).append("\n");
            return sb.toString();
        }
    };

    static {
        logger.setUseParentHandlers(false);
        addConsoleHandler(logger, Level.ALL);
        addFileHandler(logger, Level.ALL, LOG_PATH);
    }

    public static Logger getLogger() {
        return logger;
    }

    private static void addConsoleHandler(Logger log, Level level) {
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setLevel(level);
        consoleHandler.setFormatter(formatter);
        log.addHandler(consoleHandler);
    }

    private static void addFileHandler(Logger log, Level level, String filePath) {
        try {
            FileHandler fileHandler = new FileHandler(filePath, 1024 * 1024 * 10, 1, true);
            fileHandler.setLevel(level);
            fileHandler.setFormatter(formatter);
            log.addHandler(fileHandler);
        }  catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getNow() {
        return LocalDateTime.now().format(DATE_TIME_FORMATTER);
    }
}
