package com.values.batch.main.util;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPathExpressionException;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

/*
 * 2023-02-02
 * data.go.kr
 * open api 를 통한 지수시세 (코스피, 코스닥) 데이터를 조회 
 * '금융위원회_지수시세정보' 
 * 신청일    2023년 2월 2일
 * 만료예정일 2025년 2월 2일
 */
public class OpenDataKosInfo {
	
	private String mainUrl = "http://apis.data.go.kr/1160100/service/GetMarketIndexInfoService/getStockMarketIndex";
	private String apiKey = "UL8tAUjj%2F2iI%2FVkUGz4cgYs0ZiVU%2BtolZ2143oRlVSnYcBaoNPYaonDxNkuBebo2d5VTPdjNCrR%2B9BCSqze2SA%3D%3D";
	
	public int getOpenDataKosInfoCnt(String stdDate) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException  {
		HttpURLConnection con = null;
        String s = null; // 에러 메시지
        stdDate = stdDate.replaceAll("-", "");
        
        System.out.println("== getOpenDataKosInfoCnt ======================================================");
        System.out.println("stdDate : " + stdDate);
        
        int totalCount = 0;
        
        try
        {
        	// 파라미터에 해당하는 데이터 건수 가져오기 
        	URL url = new URL(
        			mainUrl
                    + "?serviceKey=" + apiKey // 서비스키
                    + "&pageNo=1" 
                    + "&numOfRows=1" 
                    + "&resultType=xml" 
                    + "&basDt=" + stdDate
                    );
        	
            con = (HttpURLConnection)url.openConnection();
            con.setRequestProperty("Accept-language", "ko");
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(con.getInputStream());

            boolean ok = false; // <resultCode>00</resultCode> 획득 여부
           
            Element e;
            NodeList ns = doc.getElementsByTagName("header");
            if (ns.getLength() > 0)
            {
                e = (Element)ns.item(0);
                if (e.getElementsByTagName("resultCode").item(0).getTextContent().equals("00")) {
                    ok = true; // 성공 여부
                	System.out.println("ok : " + ok);
                }
                else { // 에러 메시지 
                    s = e.getElementsByTagName("resultMsg").item(0).getTextContent();	
                	System.out.println("err : " + s);
            	}
            }
               
            if (ok)
            {
                ns = doc.getElementsByTagName("body");
                for (int i = 0; i < ns.getLength(); i++)
                {
                    e = (Element)ns.item(i);
                    String cnt = e.getElementsByTagName("totalCount").item(0).getTextContent();
                    
                    if(isNumber(cnt)) {
                    	totalCount = Integer.parseInt(cnt);
                    }
                }
            }
            
            return totalCount;
        }
        catch (Exception e)
        {
            s = e.getMessage();
            System.out.println("err2 : " + e.getStackTrace().toString());
            System.out.println(e.toString());
        }
       
        if (con != null)
            con.disconnect();
        
		return totalCount;
	}
	
	public Map<String, Object> getOpenDataKosInfo(String stdDate, String pageNo) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException  {
		HttpURLConnection con = null;
        String s = null; // 에러 메시지
        Map<String, Object> paramMap = new HashMap<String, Object>();
       
        stdDate = stdDate.replaceAll("-", "");
        
        System.out.println("== getOpenDataKosInfo ======================================================");
        System.out.println("stdDate : " + stdDate);
        System.out.println("pageNo : " + pageNo);
        try
        {
        	// 실 데이터 가져오기 
        	URL url = new URL(
        			mainUrl
                    + "?serviceKey=" + apiKey // 서비스키
                    + "&pageNo=" + pageNo 
                    + "&numOfRows=1000"
                    + "&resultType=xml" 
                    + "&basDt=" + stdDate
                    );
           
            con = (HttpURLConnection)url.openConnection();
            con.setRequestProperty("Accept-language", "ko");
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(con.getInputStream());

            boolean ok = false; // <resultCode>00</resultCode> 획득 여부
           
            Element e;
            NodeList ns = doc.getElementsByTagName("header");
            if (ns.getLength() > 0)
            {
                e = (Element)ns.item(0);
                if (e.getElementsByTagName("resultCode").item(0).getTextContent().equals("00")) {
                    ok = true; // 성공 여부
                	System.out.println("ok : " + ok);
                }
                else { // 에러 메시지 
                    s = e.getElementsByTagName("resultMsg").item(0).getTextContent();	
                	System.out.println("err : " + s);
            	}
            }
               
            if (ok)
            {
            	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
                ns = doc.getElementsByTagName("item");
                System.out.println("ns.getLength() : " + ns.getLength());
                for (int i = 0; i < ns.getLength(); i++)
                {
                    e = (Element)ns.item(i);
                    String idxNm = e.getElementsByTagName("idxNm").item(0).getTextContent();
                    String idxCsf = e.getElementsByTagName("idxCsf").item(0).getTextContent();
                    String tickerType = "";
                    if(idxCsf.equals("KOSDAQ시리즈")) {
                    	tickerType = "KOSDAQ";
                    } else if(idxCsf.equals("KOSPI시리즈")) {
                    	tickerType = "KOSPI";
                    } else {
                    	continue;
                    }
                    
                    String clpr = e.getElementsByTagName("clpr").item(0).getTextContent(); // INDEX_INFO
                    
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("STD_DATE", stdDate + "");
                    map.put("CODE_NAME", idxNm + "");
                    map.put("TICKER_TYPE", tickerType + "");
                    map.put("INDEX_INFO", clpr + "");
                    
                    list.add(map);
                }
                
                paramMap.put("list", list);
                
                System.out.println("OpenDataKosInfo list : " + list.size());
                
                if(list.size() == 0) {
                	paramMap.put("result", false);
                } else {
                	paramMap.put("result", true);
                }
            }
            
            return paramMap;
        }
        catch (Exception e)
        {
            s = e.getMessage();
            System.out.println("err2 : " + e.getStackTrace().toString());
            System.out.println(e.toString());
            paramMap.put("result", false);
        }
       
        if (con != null)
            con.disconnect();
        
		return paramMap;
	}
	
	public Map<String, Object> getOpenDataKosInfoJson(String stdDate, String pageNo) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException  {
        String result = ""; // 결과
        
        Map<String, Object> paramMap = new HashMap<String, Object>();
        stdDate = stdDate.replaceAll("-", "");
        
        System.out.println("== getOpenDataKosInfo (JSON) ======================================================");
        System.out.println("stdDate : " + stdDate);
        System.out.println("pageNo : " + pageNo);
        try
        {
        	// 실 데이터 가져오기 
        	URL url = new URL(
        			mainUrl
                    + "?serviceKey=" + apiKey // 서비스키
                    + "&pageNo=" + pageNo 
                    + "&numOfRows=1000"
                    + "&resultType=json" 
                    + "&basDt=" + stdDate
                    );
        	
        	BufferedReader bf;
        	bf = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));
        	result = bf.readLine();
        	
        	JSONParser jsonParser = new JSONParser();
        	JSONObject jsonObject = (JSONObject)jsonParser.parse(result);
        	JSONObject response = (JSONObject)jsonObject.get("response");
        	JSONObject body = (JSONObject)response.get("body");
        	JSONObject items = (JSONObject)body.get("items");
        	JSONArray item = (JSONArray)items.get("item");
        	
        	System.out.println("item : " + item.size());
        	
        	List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
        	
        	for(int i = 0; i < item.size(); i++) {
        		JSONObject it = (JSONObject) item.get(i);
        		
                String idxNm = (String) it.get("idxNm");
                String idxCsf = (String) it.get("idxCsf");
                String tickerType = "";
                if(idxCsf.equals("KOSDAQ시리즈")) {
                	tickerType = "KOSDAQ";
                } else if(idxCsf.equals("KOSPI시리즈")) {
                	tickerType = "KOSPI";
                } else {
                	continue;
                }
                
                String clpr = (String) it.get("clpr");
                
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("STD_DATE", stdDate + "");
                map.put("CODE_NAME", idxNm + "");
                map.put("TICKER_TYPE", tickerType + "");
                map.put("INDEX_INFO", clpr + "");
                
                list.add(map);
        	}
        	
        	paramMap.put("list", list);
            
            System.out.println("OpenDataKosInfo list : " + list.size());
            
            if(list.size() == 0) {
            	paramMap.put("result", false);
            } else {
            	paramMap.put("result", true);
            }
            
        } catch(Exception e) {
        	e.printStackTrace();
        	paramMap.put("result", false);
        }
        
		return paramMap;
	}
    
	public static boolean isNumber(String strValue) {
		return strValue != null && strValue.matches("[-+]?\\d*\\.?\\d+");
    }
	
}
