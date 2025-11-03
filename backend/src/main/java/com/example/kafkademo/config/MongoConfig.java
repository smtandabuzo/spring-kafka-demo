package com.example.kafkademo.config;

import com.example.kafkademo.model.EventDocument;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.MongoTransactionManager;
import org.springframework.data.mongodb.config.AbstractMongoClientConfiguration;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.CompoundIndexDefinition;
import org.springframework.data.mongodb.core.index.IndexOperations;
import com.mongodb.client.model.IndexOptions;
import java.util.concurrent.TimeUnit;

@Configuration
public class MongoConfig extends AbstractMongoClientConfiguration {

    @Override
    protected String getDatabaseName() {
        return "user-events";
    }

    @Value("${spring.data.mongodb.uri}")
    private String mongoUri;

    @Override
    @Bean
    public MongoClient mongoClient() {
        return MongoClients.create(mongoUri);
    }

    @Bean
    public MongoTemplate mongoTemplate() {
        MongoTemplate mongoTemplate = new MongoTemplate(mongoClient(), getDatabaseName());
        createIndexes(mongoTemplate);
        return mongoTemplate;
    }

    @Bean
    public MongoTransactionManager transactionManager(MongoDatabaseFactory dbFactory) {
        return new MongoTransactionManager(dbFactory);
    }

    private void createIndexes(MongoTemplate mongoTemplate) {
        IndexOperations indexOps = mongoTemplate.indexOps(EventDocument.class);
        
        // Compound index for common query patterns
        indexOps.ensureIndex(new CompoundIndexDefinition(
                new org.bson.Document()
                        .append("userId", 1)
                        .append("eventType", 1)
        ));

        // TTL index for automatic data expiration (30 days)
        org.springframework.data.mongodb.core.index.Index ttlIndex = new org.springframework.data.mongodb.core.index.Index()
                .on("createdAt", org.springframework.data.domain.Sort.Direction.ASC)
                .expire(30L, TimeUnit.DAYS);
        indexOps.ensureIndex(ttlIndex);
    }
}
