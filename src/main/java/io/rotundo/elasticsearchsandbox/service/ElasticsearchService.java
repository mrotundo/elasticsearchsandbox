package io.rotundo.elasticsearchsandbox.service;

import com.github.javafaker.Faker;
import io.rotundo.elasticsearchsandbox.util.Util;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.*;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.springframework.stereotype.Service;

import javax.annotation.PreDestroy;
import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Service
public class ElasticsearchService {

    protected RestHighLevelClient client;

    //@Value("${elasticsearch.host}")
    protected String elasticsearchHost;


    public ElasticsearchService(){
        //@TODO: move host info to config
        elasticsearchHost = "192.168.1.175";

        client = new RestHighLevelClient(
                RestClient.builder(
                        new HttpHost(elasticsearchHost, 9200, "http")));
    }

    public long runAggregationQueryContinuous(String indexName, JSONObject query) throws Exception{
        JSONObject response = runQuery(indexName,query);

        long count = getAggregationResultCount(response);
        if(count==0){
            System.out.println("done");
            return 0;
        }

        JSONObject after = getAggregationAfter(response);
        query = setAggregationAfter(query,after);

        return count + runAggregationQueryContinuous(indexName,query);
    }

    public JSONObject runQueryWithScroll(String indexName, JSONObject query) throws Exception{
        RestClient restClient = client.getLowLevelClient();
        Request request = new Request( "POST", "/"+indexName+"/_search?scroll=1m");
        request.setJsonEntity(query.toString());
        Response response = restClient.performRequest(request);

        OutputStream os = new ByteArrayOutputStream();
        response.getEntity().writeTo(os);

        String output = os.toString();
        JSONParser parser = new JSONParser();
        JSONObject responseObject = (JSONObject) parser.parse(output);

        long count = getResultCount(responseObject);
        String scrollId = getScrollId(responseObject);

        System.out.println("num records: " + count);
        System.out.println("scroll: "+ scrollId);

        long curCount = -1;
        while(curCount!=0&&scrollId!=null){
            JSONObject scrollResponse = scroll(scrollId);
            curCount = getResultCount(scrollResponse);
            scrollId = getScrollId(scrollResponse);
            count += curCount;
        }

        JSONObject compiledResponse = new JSONObject();
        compiledResponse.put("count",count);

        return compiledResponse;
    }


    public JSONObject runQuery(String indexName, JSONObject query) throws Exception{

        RestClient restClient = client.getLowLevelClient();
        Request request = new Request( "POST", "/"+indexName+"/_search");
        request.setJsonEntity(query.toString());
        Response response = restClient.performRequest(request);

        OutputStream os = new ByteArrayOutputStream();
        response.getEntity().writeTo(os);

        String output = os.toString();
        JSONParser parser = new JSONParser();
        JSONObject out = (JSONObject) parser.parse(output);

        return out;
    }

    public JSONObject scroll(String scrollId) throws Exception{
        RestClient restClient = client.getLowLevelClient();
        Request request = new Request( "POST", "/_search/scroll");

        JSONObject requestObject = new JSONObject();
        requestObject.put("scroll","1m");
        requestObject.put("scroll_id",scrollId);

        request.setJsonEntity(requestObject.toString());
        Response response = restClient.performRequest(request);

        OutputStream os = new ByteArrayOutputStream();
        response.getEntity().writeTo(os);

        String output = os.toString();
        JSONParser parser = new JSONParser();
        JSONObject out = (JSONObject) parser.parse(output);

        return out;
    }



    public boolean createIndexes() throws Exception{
        //@TODO: implement
        /*
            Index List
                - indextwo
                - indexone-YYYY.MM (for all of 2019)
         */
        return false;
    }

    public boolean createBulkRecords() throws Exception{

        Faker faker = new Faker();

            BulkRequest bulkRequest = new BulkRequest();

            for(int i=0;i<10000;i++){

                Integer year = 2019;
                Integer month = faker.number().numberBetween(1,12);
                String monthString = month>9?month.toString():"0"+month;
                String indexName = "indexone-"+year+"."+monthString;
                //String indexName = "indextwo";

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

    protected long getAggregationResultCount(JSONObject response) throws Exception{
        return ((JSONArray)Util.getNestedJsonObject(response,"aggregations","my_buckets","buckets")).size();
    }

    protected long getResultCount(JSONObject response) throws Exception{
        return ((JSONArray)Util.getNestedJsonObject(response,"hits","hits")).size();
    }

    protected String getScrollId(JSONObject response) throws Exception{
        return (String)Util.getNestedJsonObject(response,"_scroll_id");
    }

    protected JSONObject getAggregationAfter(JSONObject response) throws Exception{
        return (JSONObject)Util.getNestedJsonObject(response,"aggregations","my_buckets","after_key");
    }

    protected JSONObject setAggregationAfter(JSONObject query, JSONObject after) throws Exception{
        return Util.setNestedJsonObject(query,after,"aggs","my_buckets","composite","after");
    }


}
