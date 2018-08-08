package com.blink.common;

import com.blink.core.database.DBService;
import com.blink.core.database.Filter;
import com.blink.core.database.SimpleDBObject;
import com.blink.core.database.SortCriteria;
import com.blink.core.exception.BlinkRuntimeException;
import com.blink.core.file.FileService;
import com.blink.core.service.BaseService;
import com.blink.shared.common.Entity;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.Base64;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public abstract class CommonHelper<T extends Entity> {
    BaseService service;
    FileService fileService;
    DBService entityDB;
    String entityBase;
    private Class<T> entityType;

    public CommonHelper(BaseService service, String entityCollectionName, String entityBaseName, Class<T> entityType) {
        this.service = service;
        this.entityDB = service.getContext().getDbServiceFactory().ofCollection(entityCollectionName);
        this.fileService = service.getContext().getFileService();
        this.entityBase = service.getContext().getFileService().newFileURI(entityBaseName).build();
        this.entityType = entityType;
    }

    public boolean isKeyAvailable(String key) throws Exception {
        if (key == null)
            throw new BlinkRuntimeException("Key cannot be null");
        return getEntity(key) == null;
    }

    T getEntity(String key) throws Exception {
        return entityDB.find(new SimpleDBObject().append("key", key), entityType).first();
    }

    public int getEntityCount() throws Exception {
        return Math.toIntExact(entityDB.count(entityType));
    }

    public void saveEntity(T entity) throws Exception {
        if (entity.getKey() == null) {
            service.error("Key cannot be null");
            throw new BlinkRuntimeException("Key cannot be null");
        }
        entityDB.insertOrUpdate(new SimpleDBObject().append("key", entity.getKey()), entity);
    }

    public List<T> getEntities(long timestamp, boolean less, int limit) throws Exception {
        List<T> entities = new LinkedList<>();
        int current = 0;
        if (timestamp == 0L) {
            Iterator<T> iterator = entityDB.findAll(entityType, SortCriteria.descending("timestamp")).iterator();
            while (iterator.hasNext() && current < limit) {
                entities.add(fillEntity(iterator.next()));
                current++;
            }
        } else {
            SimpleDBObject toFind = new SimpleDBObject();
            if (less)
                toFind.append("timestamp", timestamp, Filter.LT);
            else
                toFind.append("timestamp", timestamp, Filter.GT);

            Iterator<T> iterator = entityDB.find(toFind, entityType).iterator();
            while (iterator.hasNext() && current < limit) {
                entities.add(fillEntity(iterator.next()));
                current++;
            }
        }

        return entities;
    }

    public abstract T fillEntity(T entity) throws Exception;

    public T getDetailsEntity(String key) throws Exception {
        T entity = getEntity(key);
        if (entity == null)
            return null;
        return fillEntity(entity);
    }

    void uploadFile(String path, String fileContent) throws Exception {
        String localFile = fileService.createLocalFile(path);

        String substring = fileContent.substring(fileContent.indexOf(",") + 1);
        byte[] decode = Base64.getDecoder().decode(substring);
        try (OutputStream output = new BufferedOutputStream(new FileOutputStream(localFile))) {
            output.write(decode);
        }

        fileService.upload(path);
        service.info("File uploaded {}", path);
    }

    public boolean deleteEntity(String key) throws Exception {
        T entity = getEntity(key);

        if (entity == null) {
            service.error("No entity to delete [key={}]", key);
            return true;
        }

        entityDB.delete(new SimpleDBObject().append("key", key), entityType);
        cleanupEntityGarbage(key);
        return true;
    }

    public abstract void cleanupEntityGarbage(String key) throws Exception;
}
