package io.rotundo.elastictest.util;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.File;
import java.nio.file.Files;

public class Util {
    public static Object getNestedJsonObject(JSONObject object, String... keys){

        int depth = keys.length;
        int i=1;
        for(String key:keys){
            if(depth==i){
                return object.get(key);
            }
            object = (JSONObject)object.get(key);
            i++;
        }

        return null;
    }

    public static JSONObject setNestedJsonObject(JSONObject object, JSONObject inner, String... keys){

        if(keys.length==0){
            return object;
        }

        if(keys.length==1){
            object.put(keys[0],inner);
        }

        String[] newKeys = new String[keys.length-1];

        for(int i=1;i<keys.length;i++){
            newKeys[i-1] = keys[i];
        }

        object.put(keys[0],setNestedJsonObject((JSONObject)object.get(keys[0]),inner,newKeys));

        return object;
    }

    public static JSONObject loadResourceQuery(String queryName) throws Exception{
        File file = new File(
                Util.class.getClassLoader().getResource("queries/"+queryName+".json").getFile()
        );

        String content = new String ( Files.readAllBytes(file.toPath()));

        return (JSONObject) (new JSONParser()).parse(content);
    }
}
