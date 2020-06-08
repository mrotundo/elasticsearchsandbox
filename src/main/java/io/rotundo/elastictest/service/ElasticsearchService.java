package io.rotundo.elastictest.service;

import com.github.javafaker.Faker;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.index.IndexResponse;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import javax.management.Query;
import java.io.InputStreamReader;
import java.io.Reader;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Optional;

@Service
public class ElasticsearchService {

    protected RestHighLevelClient client;

    public ElasticsearchService(){
        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("192.168.1.175", 9200, "http")));
    }



    public JSONObject runQuery(String indexName, String query) throws Exception{

        RestClient restClient = client.getLowLevelClient();
        Request request = new Request( "POST", "/"+indexName+"/_search");
        request.setJsonEntity(query);
        Response response = restClient.performRequest(request);

        String output = "";
        try (Reader reader = new InputStreamReader(response.getEntity().getContent())) {

            int data = reader.read();
            while(data != -1){
                char dataChar = (char) data;
                data = reader.read();
                output +=dataChar;
            }
        }

        JSONParser parser = new JSONParser();
        JSONObject out = (JSONObject) parser.parse(output);

        return out;
    }

    public boolean createBulkRecords() throws Exception{

        Faker faker = new Faker();

            BulkRequest bulkRequest = new BulkRequest();

            for(int i=0;i<10000;i++){

                Integer year = 2019;
                Integer month = faker.number().numberBetween(1,12);
                String monthString = month>9?month.toString():"0"+month;
                //String indexName = "indexone-"+year+"."+monthString;
                String indexName = "indextwo";


                LocalDateTime date1 = LocalDateTime.of(year,month,faker.number().numberBetween(1,28),faker.number().numberBetween(0,23),faker.number().numberBetween(0,59),faker.number().numberBetween(0,59));
                LocalDateTime date2 = LocalDateTime.of(year,month,faker.number().numberBetween(1,28),faker.number().numberBetween(0,23),faker.number().numberBetween(0,59),faker.number().numberBetween(0,59));

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy'-'MM'-'dd'T'HH':'mm':'ss.SSS'Z'");
                String date1Text = date1.format(formatter);
                String date2Text = date2.format(formatter);


                JSONObject record = new JSONObject();
                record.put("name", faker.animal().name());
                record.put("tags", faker.artist().name());
                record.put("description", faker.aquaTeenHungerForce().character()+" "+
                        faker.beer().name()+" "+
                        faker.book().title()+" "+
                        faker.shakespeare().kingRichardIIIQuote()+" "+
                        faker.backToTheFuture().quote());
                record.put("val", faker.number().numberBetween(0,999999));
                record.put("record_date", date1Text);
                record.put("index_date", date2Text);


                IndexRequest request = new IndexRequest(indexName);
                request.source(record, XContentType.JSON);

                bulkRequest.add(request);
            }
            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            System.out.println(bulkResponse);

        return false;

    }

    @PreDestroy
    public void onShutDown() throws Exception{
        System.out.println("Shutting down...");
        client.close();
        System.out.println("shutdown");
    }
}
