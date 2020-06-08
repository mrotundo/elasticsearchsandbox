package io.rotundo.elastictest.controller;
import com.github.javafaker.Faker;
import io.rotundo.elastictest.service.ElasticsearchService;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Optional;


@RestController
public class MainController {

    @Autowired
    public ElasticsearchService elasticsearchService;

    @GetMapping(value="/query",produces={"application/json"})
    public JSONObject query(@RequestParam("index") Optional<String> index,@RequestParam("query") Optional<String> query) throws Exception{

        return elasticsearchService.runQuery(index.orElse("*"),query.orElse("{}"));
    }

    @GetMapping("/test")
    public String indexData(@RequestParam("year") Optional<Integer> year, @RequestParam("month") Optional<Integer> month,@RequestParam("numBatches") Optional<Integer> numBatches) throws Exception{

        Faker faker = new Faker();

        RestHighLevelClient client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost("192.168.1.175", 9200, "http")));

        String result = "";
        int numRecords = 0;


        for(int x=0;x<numBatches.orElse(10);x++){
            BulkRequest bulkRequest = new BulkRequest();
            Date startDate = new Date(year.orElse(2020),month.orElse(1),1,0,0,0);
            Date endDate = new Date(year.orElse(2020),month.orElse(1),28,23,59,59);
            for(int i=0;i<10000;i++){
                String monthString = month.orElse(1)>9?month.get().toString():"0"+month.orElse(1);
                String indexName = "indexone-"+year.orElse(2020)+"."+monthString;
                JSONObject record = new JSONObject();
                record.put("name", faker.animal().name());
                record.put("tags", faker.artist().name());
                record.put("description", faker.aquaTeenHungerForce().character()+" "+
                        faker.beer().name()+" "+
                        faker.book().title()+" "+
                        faker.shakespeare().kingRichardIIIQuote()+" "+
                        faker.backToTheFuture().quote());
                record.put("val", faker.number().numberBetween(0,999999));
                record.put("record_date", faker.date().between(startDate,endDate));
                record.put("index_date", faker.date().between(startDate,endDate));
                IndexRequest request = new IndexRequest(indexName);
                request.source(record, XContentType.JSON);
                //request.id();
                bulkRequest.add(request);
                numRecords++;
            }
            BulkResponse bulkResponse = client.bulk(bulkRequest, RequestOptions.DEFAULT);
            System.out.println(bulkResponse);
        }


        result = "Records created " + numRecords;

        return result;
    }
}
