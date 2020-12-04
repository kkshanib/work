package com.geomapping.camera;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;



import org.bson.Document;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.mongodb.MongoClient;
import com.mongodb.client.FindIterable;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.geojson.Point;
import com.mongodb.client.model.geojson.Position;

@SpringBootTest
class GeomapperApplicationTests {
	
	private MongoClient mongoClient;
    private MongoDatabase db;
    private MongoCollection<Document> collection;

    @BeforeEach
    public void setup() {
        if (mongoClient == null) {
        	mongoClient = new MongoClient();
            db = mongoClient.getDatabase("myMongoDb");
            collection = db.getCollection("places");
            collection.deleteMany(new Document());
            collection.createIndex(Indexes.geo2dsphere("location"));
            collection.insertOne(Document.parse("{'name':'Big Ben','location': {'coordinates':[-0.1268194,51.5007292],'type':'Point'}}"));
            collection.insertOne(Document.parse("{'name':'Hyde Park','location': {'coordinates': [[[-0.159381,51.513126],[-0.189615,51.509928],[-0.187373,51.502442], [-0.153019,51.503464],[-0.159381,51.513126]]],'type':'Polygon'}}"));
        }
    }

    @Test
    public void givenNearbyLocation_whenSearchNearby_thenFound() {
        Point currentLoc = new Point(new Position(-0.126821, 51.495885));
        FindIterable<Document> result = collection.find(Filters.near("location", currentLoc, 1000.0, 10.0));

        assertNotNull(result.first());
        assertEquals("Big Ben", result.first().get("name"));
    }

    @Test
    public void givenFarLocation_whenSearchNearby_thenNotFound() {
        Point currentLoc = new Point(new Position(-0.5243333, 51.4700223));
        FindIterable<Document> result = collection.find(Filters.near("location", currentLoc, 5000.0, 10.0));

        assertNull(result.first());
    }

}
