package com.blink.core.service;

import com.blink.core.database.DBServiceFactory;
import com.blink.core.exception.BlinkRuntimeException;
import com.blink.core.file.FileService;
import com.blink.core.log.LoggerFactory;
import com.blink.core.messaging.MessagingService;
import com.blink.core.transport.Bus;
import com.blink.core.transport.BusService;

import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public final class Context {
    private Configuration configuration;
    private BusService busService;
    private LoggerFactory loggerFactory;
    private DBServiceFactory dbServiceFactory;
    private FileService fileService;
    private MessagingService messagingService;
    private Map<String, DerivedService> derivedServiceMap;

    private Context(ContextBuilder builder) {
        this.configuration = builder.configuration;
        this.busService = builder.busService;
        this.loggerFactory = builder.loggerFactory;
        this.dbServiceFactory = builder.dbServiceFactory;
        this.fileService = builder.fileService;
        this.messagingService = builder.messagingService;
        derivedServiceMap = new HashMap<>();
    }

    public BusService getBusService() {
        return busService;
    }

    public Configuration getConfiguration() {
        return configuration;
    }

    public LoggerFactory getLoggerFactory() {
        return loggerFactory;
    }

    public DBServiceFactory getDbServiceFactory() {
        return dbServiceFactory;
    }

    public FileService getFileService() {
        return fileService;
    }

    public MessagingService getMessagingService() {
        return messagingService;
    }

    public void registerDerivedService(Class<? extends DerivedService> clazz, DerivedService service) {
        if (derivedServiceMap.containsKey(clazz.getName()))
            throw new BlinkRuntimeException(MessageFormat.format("Derived service already defined: {0}", clazz.getName()));
        else
            derivedServiceMap.put(clazz.getName(), service);
    }

    public <T extends DerivedService> T getDerivedService(Class<T> clazz) {
        DerivedService derivedService = derivedServiceMap.get(clazz.getName());
        return clazz.cast(derivedService);
    }

    public static class ContextBuilder {
        private Configuration configuration;
        private BusService busService;
        private LoggerFactory loggerFactory;
        private DBServiceFactory dbServiceFactory;
        private FileService fileService;
        private MessagingService messagingService;

        public ContextBuilder setBusService(BusService busService) {
            this.busService = busService;
            return this;
        }

        public ContextBuilder setConfiguration(Configuration configuration) {
            this.configuration = configuration;
            return this;
        }

        public ContextBuilder setLoggerFactory(LoggerFactory loggerFactory) {
            this.loggerFactory = loggerFactory;
            return this;
        }

        public ContextBuilder setDbServiceFactory(DBServiceFactory dbServiceFactory) {
            this.dbServiceFactory = dbServiceFactory;
            return this;
        }

        public ContextBuilder setFileService(FileService fileService) {
            this.fileService = fileService;
            return this;
        }

        public ContextBuilder setMessagingService(MessagingService messagingService) {
            this.messagingService = messagingService;
            return this;
        }

        public Context build() {
            return new Context(this);
        }
    }

}
