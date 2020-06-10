package io.rotundo.elasticsearchsandbox.controller;


import io.rotundo.elasticsearchsandbox.service.ElasticsearchService;
import io.rotundo.elasticsearchsandbox.util.Util;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/endpoint")
public class EndpointController {

    @Autowired
    public ElasticsearchService elasticsearchService;

    @GetMapping(value={"/bitemporal"},produces={"application/json"})
    public JSONObject bitemporal() throws Exception{

        JSONObject queryObject = Util.loadResourceQuery("aggregation-bi-temporal");

        System.out.println(queryObject);
        long count = elasticsearchService.runAggregationQueryContinuous("indexone*",queryObject);
        JSONObject response =  new JSONObject();
        response.put("count",count);
        return response;
    }


}
