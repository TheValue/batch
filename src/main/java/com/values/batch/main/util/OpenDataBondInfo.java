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
 * 2023-01-31
 * data.go.kr
 * open api 를 통한 채권기본정보 데이터를 조회 
 * '금융위원회_채권기본정보' 
 * 신청일    2023년 1월 31일
 * 만료예정일 2025년 1월 31일
 */
public class OpenDataBondInfo {
	
	private String mainUrl = "http://apis.data.go.kr/1160100/service/GetBondIssuInfoService/getBondBasiInfo";
	private String apiKey = "UL8tAUjj%2F2iI%2FVkUGz4cgYs0ZiVU%2BtolZ2143oRlVSnYcBaoNPYaonDxNkuBebo2d5VTPdjNCrR%2B9BCSqze2SA%3D%3D";
	
	public int getOpenDataBondInfoCnt(String stdDate) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException  {
		HttpURLConnection con = null;
        String s = null; // 에러 메시지
        stdDate = stdDate.replaceAll("-", "");
        
        System.out.println("== getOpenDataBondInfoCnt ======================================================");
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
	
	public Map<String, Object> getOpenDataBondInfo(String stdDate, String pageNo) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException  {
		HttpURLConnection con = null;
        String s = null; // 에러 메시지
        Map<String, Object> paramMap = new HashMap<String, Object>();
       
        stdDate = stdDate.replaceAll("-", "");
        
        System.out.println("== getOpenDataBondInfo ======================================================");
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
                    String isinCd = e.getElementsByTagName("isinCd").item(0).getTextContent();
                    String isinCdNm = e.getElementsByTagName("isinCdNm").item(0).getTextContent();
                    String bondIsurNm = e.getElementsByTagName("bondIsurNm").item(0).getTextContent();
                    String crno = e.getElementsByTagName("crno").item(0).getTextContent();
                    String bondRnknDcdNm = e.getElementsByTagName("bondRnknDcdNm").item(0).getTextContent();
                    String bondIssuDt = e.getElementsByTagName("bondIssuDt").item(0).getTextContent();
                    String bondExprDt = e.getElementsByTagName("bondExprDt").item(0).getTextContent();
                    String scrsItmsKcdNm = e.getElementsByTagName("scrsItmsKcdNm").item(0).getTextContent();
                    String bondIssuAmt = e.getElementsByTagName("bondIssuAmt").item(0).getTextContent();
                    String bondBal = e.getElementsByTagName("bondBal").item(0).getTextContent();
                    String bondSrfcInrt = e.getElementsByTagName("bondSrfcInrt").item(0).getTextContent();
                    String bondIntTcdNm = e.getElementsByTagName("bondIntTcdNm").item(0).getTextContent();
                    String bondIntTcd = e.getElementsByTagName("bondIntTcd").item(0).getTextContent();
                    String intPayCyclCtt = e.getElementsByTagName("intPayCyclCtt").item(0).getTextContent();
                    String nxtmCopnDt = e.getElementsByTagName("nxtmCopnDt").item(0).getTextContent();
                    String irtChngDcdNm = e.getElementsByTagName("irtChngDcdNm").item(0).getTextContent();
                    String intCmpuMcdNm = e.getElementsByTagName("intCmpuMcdNm").item(0).getTextContent();
                    String pclrBondKcdNm = e.getElementsByTagName("pclrBondKcdNm").item(0).getTextContent();
                    String optnTcdNm = e.getElementsByTagName("optnTcdNm").item(0).getTextContent();
                    String bondIssuCurCdNm = e.getElementsByTagName("bondIssuCurCdNm").item(0).getTextContent();
                    
                    Map<String, Object> map = new HashMap<String, Object>();
                    map.put("STD_DATE", stdDate + "");
                    map.put("ISIN", isinCd + "");
                    map.put("ITEM_NAME", isinCdNm + "");
                    map.put("ISSUER", bondIsurNm + "");
                    map.put("CRNO", crno + "");
                    map.put("RANK", bondRnknDcdNm + "");
                    map.put("ISSUE_DATE", bondIssuDt + "");
                    map.put("EXP_DATE", bondExprDt + "");
                    map.put("BOND_TYPE", scrsItmsKcdNm + "");
                    map.put("AMT", bondIssuAmt + "");
                    map.put("REMAIN_AMT", bondBal + "");
                    map.put("COUPON", bondSrfcInrt + "");
                    map.put("COUPON_TYPE", bondIntTcdNm + "");
                    map.put("COUPON_TYPE_CODE", bondIntTcd + "");
                    map.put("COUPON_CYCLE", intPayCyclCtt + "");
                    map.put("COUPON_DT", nxtmCopnDt + "");
                    map.put("PAY_TYPE", irtChngDcdNm + "");
                    map.put("COMPOUND", intCmpuMcdNm + "");
                    map.put("MAZZ_YN", pclrBondKcdNm + "");
                    map.put("OPTION_DESC", optnTcdNm + "");
                    map.put("CURRENCY", bondIssuCurCdNm + "");
                    
                    list.add(map);
                }
                
                paramMap.put("list", list);
                
                System.out.println("OpenDataBondInfo list : " + list.size());
                
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
	
	public Map<String, Object> getOpenDataBondInfoJson(String stdDate, String pageNo) throws IOException, SAXException, ParserConfigurationException, XPathExpressionException  {
        String result = ""; // 결과
        
        Map<String, Object> paramMap = new HashMap<String, Object>();
        stdDate = stdDate.replaceAll("-", "");
        
        System.out.println("== getOpenDataBondInfo (JSON) ======================================================");
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
        		
        		String isinCd = (String) it.get("isinCd");
                String isinCdNm = (String) it.get("isinCdNm");
                String bondIsurNm = (String) it.get("bondIsurNm");
                String crno = (String) it.get("crno");
                String bondRnknDcdNm = (String) it.get("bondRnknDcdNm");
                String bondIssuDt = (String) it.get("bondIssuDt");
                String bondExprDt = (String) it.get("bondExprDt");
                String scrsItmsKcdNm = (String) it.get("scrsItmsKcdNm");
                String bondIssuAmt = (String) it.get("bondIssuAmt");
                String bondBal = (String) it.get("bondBal");
                String bondSrfcInrt = (String) it.get("bondSrfcInrt");
                String bondIntTcdNm = (String) it.get("bondIntTcdNm");
                String bondIntTcd = (String) it.get("bondIntTcd");
                String intPayCyclCtt = (String) it.get("intPayCyclCtt");
                String nxtmCopnDt = (String) it.get("nxtmCopnDt");
                String irtChngDcdNm = (String) it.get("irtChngDcdNm");
                String intCmpuMcdNm = (String) it.get("intCmpuMcdNm");
                String pclrBondKcdNm = (String) it.get("pclrBondKcdNm");
                String optnTcdNm = (String) it.get("optnTcdNm");
                String bondIssuCurCdNm = (String) it.get("bondIssuCurCdNm");
                
                Map<String, Object> map = new HashMap<String, Object>();
                map.put("STD_DATE", stdDate + "");
                map.put("ISIN", isinCd + "");
                map.put("ITEM_NAME", isinCdNm + "");
                map.put("ISSUER", bondIsurNm + "");
                map.put("CRNO", crno + "");
                map.put("RANK", bondRnknDcdNm + "");
                map.put("ISSUE_DATE", bondIssuDt + "");
                map.put("EXP_DATE", bondExprDt + "");
                map.put("BOND_TYPE", scrsItmsKcdNm + "");
                map.put("AMT", bondIssuAmt + "");
                map.put("REMAIN_AMT", bondBal + "");
                map.put("COUPON", bondSrfcInrt + "");
                map.put("COUPON_TYPE", bondIntTcdNm + "");
                map.put("COUPON_TYPE_CODE", bondIntTcd + "");
                map.put("COUPON_CYCLE", intPayCyclCtt + "");
                map.put("COUPON_DT", nxtmCopnDt + "");
                map.put("PAY_TYPE", irtChngDcdNm + "");
                map.put("COMPOUND", intCmpuMcdNm + "");
                map.put("MAZZ_YN", pclrBondKcdNm + "");
                map.put("OPTION_DESC", optnTcdNm + "");
                map.put("CURRENCY", bondIssuCurCdNm + "");
                
                list.add(map);
        	}
        	
        	paramMap.put("list", list);
            
            System.out.println("OpenDataBondInfo list : " + list.size());
            
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
