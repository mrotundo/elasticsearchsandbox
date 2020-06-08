package io.rotundo.elastictest.controller;
import com.github.javafaker.Faker;
import io.rotundo.elastictest.service.ElasticsearchService;
import io.rotundo.elastictest.util.Util;
import org.apache.http.HttpHost;
import org.elasticsearch.action.bulk.BulkRequest;
import org.elasticsearch.action.bulk.BulkResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestClient;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.json.simple.parser.JSONParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
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

    @GetMapping(value={"/query/{indexName}/{queryName}","/query/{indexName}", "/query"},produces={"application/json"})
    public JSONObject query(@RequestParam("query") Optional<String> query,@PathVariable("queryName") Optional<String> queryName,@PathVariable("indexName") Optional<String> indexName) throws Exception{

        JSONObject queryObject = new JSONObject();

        if(queryName.isPresent()){
            queryObject = Util.loadResourceQuery(queryName.get());

        }
        else{
            try{
                JSONParser parser = new JSONParser();
                queryObject = (JSONObject) parser.parse(query.orElse("{}"));
            }
            catch(Exception e){
                System.out.println("Invalid query "+ query);
            }
        }

        System.out.println(queryObject);

        return elasticsearchService.runQuery(indexName.orElse("*"),queryObject);
    }

    @GetMapping(value={"/scrolltest"},produces={"application/json"})
    public JSONObject scrolltest() throws Exception{

        JSONObject queryObject = Util.loadResourceQuery("basic-term");

        System.out.println(queryObject);
        JSONObject response =  elasticsearchService.runQueryWithScroll("indexone*",queryObject);
        return response;
    }

    @GetMapping(value={"/querytest"},produces={"application/json"})
    public JSONObject querytest() throws Exception{

        JSONObject queryObject = Util.loadResourceQuery("aggregation-bi-temporal");

        System.out.println(queryObject);
        long count = elasticsearchService.runAggregationQueryContinuous("indexone*",queryObject);
        JSONObject response =  new JSONObject();
        response.put("count",count);
        return response;
    }

}
