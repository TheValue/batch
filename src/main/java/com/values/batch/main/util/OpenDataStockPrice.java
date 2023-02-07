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

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import java.io.IOException;

/*
 * 2023-01-20
 * data.go.kr
 * open api 를 통한 주식 주가 (종가) 정보 리스트 조회
 * '금융위원회_주식시세정보' 
 * 신청일    2023년 1월 20일
 * 만료예정일 2025년 1월 20일
 */
public class OpenDataStockPrice {
	
	private String mainUrl = "http://apis.data.go.kr/1160100/service/GetStockSecuritiesInfoService/getStockPriceInfo";
	private String apiKey = "UL8tAUjj%2F2iI%2FVkUGz4cgYs0ZiVU%2BtolZ2143oRlVSnYcBaoNPYaonDxNkuBebo2d5VTPdjNCrR%2B9BCSqze2SA%3D%3D";
	
	public int getOpenDataStockPriceCnt(String fromDate, String toDate, String orgDate) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException  {
		HttpURLConnection con = null;
        String s = null; // 에러 메시지
       
        fromDate = fromDate.replaceAll("-", "");
        toDate = toDate.replaceAll("-", "");
        
        System.out.println("== getOpenDataStockPriceCnt ======================================================");
        System.out.println("fromDate : " + fromDate);
        System.out.println("toDate : " + toDate);
        System.out.println("orgDate : " + orgDate);
        
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
                    + "&beginBasDt=" + fromDate
                    + "&endBasDt=" + toDate
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
	
	public Map<String, Object> getOpenDataStockPrice(String fromDate, String toDate, String orgDate, String pageNo) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException  {
		HttpURLConnection con = null;
        String s = null; // 에러 메시지
        Map<String, Object> paramMap = new HashMap<String, Object>();
       
        fromDate = fromDate.replaceAll("-", "");
        toDate = toDate.replaceAll("-", "");
        
        System.out.println("== getOpenDataStockPrice ======================================================");
        System.out.println("fromDate : " + fromDate);
        System.out.println("toDate : " + toDate);
        System.out.println("orgDate : " + orgDate);
        System.out.println("pageNo : " + pageNo);
        
        try
        {
        	URL url = new URL(
        			mainUrl
                    + "?serviceKey=" + apiKey // 서비스키
                    + "&pageNo=" + pageNo 
                    + "&numOfRows=1000" 
                    + "&resultType=xml" 
                    + "&beginBasDt=" + fromDate
                    + "&endBasDt=" + toDate
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
                    String srtnCd = e.getElementsByTagName("srtnCd").item(0).getTextContent();
                    String itmsNm = e.getElementsByTagName("itmsNm").item(0).getTextContent();
                    String basDt = e.getElementsByTagName("basDt").item(0).getTextContent();
                    String tickerType = "STOCK";
                    String clpr = e.getElementsByTagName("clpr").item(0).getTextContent();
                    
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("TICKER", srtnCd + "");
                    map.put("CODE_NAME", itmsNm + "");
                    
                    if(orgDate != null && !orgDate.equals("")) {
                    	map.put("STD_DATE", orgDate + "");
                    } else {
                    	map.put("STD_DATE", basDt + "");
                    }
                    map.put("TICKER_TYPE", tickerType + "");
                    map.put("INDEX_INFO", clpr + "");
                    
                    list.add(map);
                }
                
                paramMap.put("list", list);
                
                System.out.println("OpenDataStock list : " + list.size());
                
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
        }
       
        if (con != null)
            con.disconnect();
        
		return paramMap;
	}
    
	public static boolean isNumber(String strValue) {
		return strValue != null && strValue.matches("[-+]?\\d*\\.?\\d+");
    }
    
}
