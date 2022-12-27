package io.keychain.mitm;

import java.util.ArrayList;
import org.json.JSONObject;
import org.json.JSONArray;

public class JsonUtils {
  
    /**
     * {
     *   msg: {
     *     "meterId": <id>,
     *     "readings": [
     *       { "readingDateTime": <time>, "direction": <dir>, "accumulatedAmount": <amt> },
     *       ...
     *       ...
     *     ]
     *   }
     * }
     */
    public static String dataToString(String meterId, ArrayList<Reading> readings){
        JSONObject obj = new JSONObject();
        JSONArray ja = new JSONArray();

        for (int i = 0; i < readings.size(); i++){
            JSONObject robj = new JSONObject();
            robj.put("readingDateTime", readings.get(i).readingDateTime.toString());
            robj.put("direction", readings.get(i).direction ? "1" : "0");
            robj.put("accumulatedAmount", readings.get(i).accumulatedAmount);
            ja.put(robj);
        }

        obj.put("meterId", meterId);
        obj.put("readings", ja);
      
        JSONObject msg = new JSONObject();
        msg.put("msg", obj.toString());
        return msg.toString();
    }
}
