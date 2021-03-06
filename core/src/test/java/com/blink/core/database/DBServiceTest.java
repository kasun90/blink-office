package com.blink.core.database;

import com.blink.core.database.mongodb.MongoDBService;
import com.blink.core.database.mongodb.MongoDBServiceFactory;
import com.blink.utilities.BlinkJSON;
import org.junit.Before;
import org.junit.Test;

public class DBServiceTest {

    DBService dbService;

    @Before
    public void initService() {
        MongoDBServiceFactory factory = new MongoDBServiceFactory("localhost", 27017, "test");
        dbService = factory.ofCollection("testcollection");
    }


    @Test
    public void inserTest() throws Exception {
        Person person = new Person("Kasun", 28, "Nugegoda");
        dbService.insert(person);
    }

    @Test
    public void insertOrUpdateTest() throws Exception {
        SimpleDBObject toFind = new SimpleDBObject().append("name", "Piyumal");
        dbService.insertOrUpdate(toFind, new Person("Piyumal", 22, "Walasmulla"));
    }


    @Test
    public void findAllTest() throws Exception {
        for (Person person : dbService.findAll(Person.class)) {
            System.out.println("--------------");
            System.out.println(BlinkJSON.toPrettyJSON(person));
        }
    }

    @Test
    public void findTest() throws Exception {
        SimpleDBObject object = new SimpleDBObject().append("name", "Kasun").append("city", "Nugegoda");

        for (Person person : dbService.find(object, Person.class)) {
            System.out.println(BlinkJSON.toPrettyJSON(person));
        }
    }


    @Test
    public void updateTest() throws Exception {
        SimpleDBObject object = new SimpleDBObject().append("name", "Kasun");

        dbService.update(object, new Person("Kasun Piyumal", 29, "Battaramulla"));
    }

    @Test
    public void deleteTest() throws Exception {
        SimpleDBObject object = new SimpleDBObject().append("age", 29);
        System.out.println("deleted: " + dbService.delete(object, Person.class));
    }

    @Test
    public void incrementTest() throws Exception {
        System.out.println(dbService.getCounterValue());
    }

    @Test
    public void incrementCustomTest() throws Exception {
        System.out.println(dbService.getCounterValue("hello"));
    }

}