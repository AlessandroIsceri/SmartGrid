package com.ii.smartgrid.smartgrid.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ii.smartgrid.smartgrid.model.Cable;
import java.io.File;

public class CableUtil {
    private static Map<String, Cable> cableTypes;
    private static List<CableEdge> links;
    
    static {
        ObjectMapper objectMapper = new ObjectMapper();
        TypeReference<HashMap<String, Object>> typeRef = new TypeReference<HashMap<String, Object>>() {};
        Map<String, Object> fileContent;
        try {
            fileContent = objectMapper.readValue(new File(JsonUtil.CABLES_PATH), typeRef);
            
            TypeReference<HashMap<String, Cable>> typeRefCables = new TypeReference<HashMap<String, Cable>>() {};
            cableTypes = objectMapper.convertValue(fileContent.get("types"), typeRefCables);

            TypeReference<ArrayList<CableEdge>> typeRefArrayList = new TypeReference<ArrayList<CableEdge>>() {};
            links = objectMapper.convertValue(fileContent.get("links"), typeRefArrayList);

            } catch (StreamReadException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (DatabindException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
    }

    public static Cable getCableTypeInfo(String from, String to){
        for(CableEdge cable : links){
            boolean eq1 = cable.getFrom().equals(from) && cable.getTo().equals(to);
            boolean eq2 = cable.getTo().equals(from) && cable.getFrom().equals(to);
            if(eq1 || eq2){
                return cableTypes.get(cable.getCableType());
            }
        }
        return null;
    }


    public static class CableEdge{
        private String from;
        private String to;
        private String cableType;

        public CableEdge(){

        }

        public String getFrom(){
            return from;
        }

        public void setFrom(String from) {
            this.from = from;
        }

        public String getTo() {
            return to;
        }

        public void setTo(String to) {
            this.to = to;
        }

        public String getCableType() {
            return cableType;
        }

        public void setCableType(String cableType) {
            this.cableType = cableType;
        }

    }

}
