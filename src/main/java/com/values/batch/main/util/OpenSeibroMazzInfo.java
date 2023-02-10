package com.values.batch.main.util;

import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.springframework.beans.factory.annotation.Value;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

/*
 * 2023-02-08
 * https://seibro.or.kr/OpenPlatform/callOpenAPI.jsp?key=9614eeb7673b721fea4508494ef64499e5597915ed1864e0d0ba33e35c564190&apiId=getBondStatInfo&params=ISIN:KR6053951A68
 * OPEN SEIBRO api 를 통한 메짜닌 추가정보 데이터를 조회 
 * '공공데이터포털 발행채권정보를 통하여 세이브로 사이트에서 제공하는 메짜닌 정보 (추가 상세정보)를 가져오기 위함' 
 * 신청일    2023년 
 * URL - https://seibro.or.kr/OpenPlatform/callOpenAPI.jsp
 * 인증키 - 9614eeb7673b721fea4508494ef64499e5597915ed1864e0d0ba33e35c564190
 */
public class OpenSeibroMazzInfo {
	private String basicUrl = "https://seibro.or.kr/OpenPlatform/callOpenAPI.jsp?apiId=getBondStatInfo"; // 기본
	private String optionUrl = "https://seibro.or.kr/OpenPlatform/callOpenAPI.jsp?apiId=getBondOptionXrcInfo"; // 옵션이 있는 경우
	private String dateUrl = "https://seibro.or.kr/OpenPlatform/callOpenAPI.jsp?apiId=getBondIssuInfo"; // 특정 날짜로 조회하는 경우 params=ISSU_DT:{isudt}
	private String mainUrl = "";
	private String apiKey = "9614eeb7673b721fea4508494ef64499e5597915ed1864e0d0ba33e35c564190";
	private String parameter = "";
	
	public List<Map<String, Object>> getOpenSeibroMazzInfo(String isinCode, String strDate, boolean optionYn) {
		List<Map<String, Object>> resultList = new ArrayList<Map<String, Object>>();
		HttpURLConnection con = null;
        String s = null; // 에러 메시지
        Map<String, Object> map = null;
        
        System.out.println("== getOpenSeibroMazzInfo ======================================================");
        System.out.println("isinCode : " + isinCode);
        System.out.println("strDate : " + strDate);
        System.out.println("optionYn : " + optionYn);
        
        /*
        if(!"".equals(strDate) && strDate != null) {
        	mainUrl = dateUrl;
        	parameter = "ISSU_DT:" + strDate;
        } else {
        	if(optionYn) {
        		mainUrl = optionUrl;
            	parameter = "ISIN:" + isinCode;
        	} else {
        		mainUrl = basicUrl;
            	parameter = "ISIN:" + isinCode;
        	}
        }
        */
        
        // 기본 data 먼저 가져오기
        mainUrl = basicUrl;
    	parameter = "ISIN:" + isinCode;
    	
		String fullUrl = mainUrl + "&key=" + apiKey + "&params=" + parameter;
        try{
            URL url = new URL(fullUrl);
            con = (HttpURLConnection)url.openConnection();
            con.setRequestProperty("Accept-language", "ko");
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(con.getInputStream());
            
            boolean ok = false; // <status>000</status> 획득 여부
            
            Element e;
            NodeList ns = doc.getElementsByTagName("vector");
             
            if (ns.getLength() > 0)
            {
                e = (Element)ns.item(0);
                if (e.getAttribute("result").toString().equals("0")) {
                	s = "0건";
                	System.out.println("err : " + s);
                }
                else { 
                	ok = true; // 성공 여부
                	System.out.println("ok : " + ok);
            	}
            }
            
            if (ok)
            {
                ns = doc.getElementsByTagName("result");
                for (int i = 0; i < ns.getLength(); i++)
                {
                	map = new HashMap<String, Object>();
                    e = (Element)ns.item(i);
                    
                    // 기본 데이터
                    String korSecnNm = getEleAttrValue(e, "KOR_SECN_NM"); // 종목명 
                    String issuDt = getEleAttrValue(e, "ISSU_DT"); // 발행일 
                    String xpirDt = getEleAttrValue(e, "XPIR_DT"); // 만기일 
                    String firstIssuAmt = getEleAttrValue(e, "FIRST_ISSU_AMT"); // 발행액 
                    String issuRema = getEleAttrValue(e, "ISSU_REMA"); // 발행잔액 
                    String couponRate = getEleAttrValue(e, "COUPON_RATE"); // 쿠폰 - 표면이자율 
                    String xpiredRate = getEleAttrValue(e, "XPIRED_RATE"); // 만기상환율 
                    String recuWhcd = getEleAttrValue(e, "RECU_WHCD"); // 모집방법 ) 11:공모, 12:사모, 13:일반(국채,지방채,특수채), 14:CBO기초사모, 21:매출
                    String particulBondKindTpcd = getEleAttrValue(e, "PARTICUL_BOND_KIND_TPCD"); // 사채종류 ) 1:전환, 2:교환, 3:신주인수권, 4:분리형신주인수권, 6:이익참가, 9:해당없음
                    String optionTpcd = getEleAttrValue(e, "OPTION_TPCD"); // 옵션구분 ) 9401:call, 9402:put, 9403:call+put, 9404:note, 0:옵션해당없음 
                    String forcErlyRedYn = getEleAttrValue(e, "FORC_ERLY_RED_YN"); // 강제조기상환여부 
                    String mrChgTpcd = getEleAttrValue(e, "MR_CHG_TPCD"); // 금리변동구분 ) / 1:고정, 2:변동, 3:고정+변동 
                    String grtyTpcd = getEleAttrValue(e, "GRTY_TPCD"); // 보증구분 ) 1:보증, 2:무보증, 3:담보부, 4:일반 
                    String rankTpcd = getEleAttrValue(e, "RANK_TPCD"); // 순위구분 ) 1:선순위, 2:후순위, 3:중순위, 9:해당없음 
                    String intPayWayTpcd = getEleAttrValue(e, "INT_PAY_WAY_TPCD"); // 이자지급방법 / 1:이표, 2:할인, 3:복리, 4:단리 
                    String sintCintTpcd = getEleAttrValue(e, "SINT_CINT_TPCD"); // 단리복리구분 / 1:단리, 2:복리, 3:단리+복리 
                    String irateChgTpcd = getEleAttrValue(e, "IRATE_CHG_TPCD"); // 이자율변동구분 / 1:이율동일, 2:이율상이, 3:비정형 
                    String xpirGuarPrate = getEleAttrValue(e, "XPIR_GUAR_PRATE"); // 만기보장수익율 
                    String xpirGuarPrateTpcd = getEleAttrValue(e, "XPIR_GUAR_PRATE_TPCD"); // 이자가산주기 ) 01:연복리, 02:3개월복리, 03:6개월복리, 04:연단리, 05:1개월복리, 99:해당없음 
                    String prcpRedWhcd = getEleAttrValue(e, "PRCP_RED_WHCD"); // 원금상환구분 ) 11:만기상환, 21:중도상환, 31:조기상환, 41:이익분배, 51:자동상환, 14:수시상환, 12:균등분할상환, 13:불균등분할상환 
                    String kisValatGrdCd = getEleAttrValue(e, "KIS_VALAT_GRD_CD"); // 110:AAA, 111:AAA+, 112:AAA0(A1), 113:AAA- 
                    String niceValatGrdCd = getEleAttrValue(e, "NICE_VALAT_GRD_CD"); // 상동 
                    String sciValatGrdCd = getEleAttrValue(e, "SCI_VALAT_GRD_CD"); // 상동 
                    String krValatGrdCd = getEleAttrValue(e, "KR_VALAT_GRD_CD"); // 상동 
                    
                    map.put("ISIN", isinCode + "");
                    map.put("KOR_SECN_NM", korSecnNm);
                    map.put("ISSU_DT", issuDt);
                    map.put("XPIR_DT", xpirDt);
                    map.put("FIRST_ISSU_AMT", firstIssuAmt);
                    map.put("ISSU_REMA", issuRema);
                    map.put("COUPON_RATE", couponRate);
                    map.put("XPIRED_RATE", xpiredRate);
                    map.put("RECU_WHCD", recuWhcd);
                    map.put("PARTICUL_BOND_KIND_TPCD", particulBondKindTpcd);
                    map.put("OPTION_TPCD", optionTpcd);
                    map.put("FORC_ERLY_RED_YN", forcErlyRedYn);
                    map.put("MR_CHG_TPCD", mrChgTpcd);
                    map.put("GRTY_TPCD", grtyTpcd);
                    map.put("RANK_TPCD", rankTpcd);
                    map.put("INT_PAY_WAY_TPCD", intPayWayTpcd);
                    map.put("SINT_CINT_TPCD", sintCintTpcd);
                    map.put("IRATE_CHG_TPCD", irateChgTpcd);
                    map.put("XPIR_GUAR_PRATE", xpirGuarPrate);
                    map.put("XPIR_GUAR_PRATE_TPCD", xpirGuarPrateTpcd);
                    map.put("PRCP_RED_WHCD", prcpRedWhcd);
                    map.put("KIS_VALAT_GRD_CD", kisValatGrdCd);
                    map.put("NICE_VALAT_GRD_CD", niceValatGrdCd);
                    map.put("SCI_VALAT_GRD_CD", sciValatGrdCd);
                    map.put("KR_VALAT_GRD_CD", krValatGrdCd);
                    map.put("result", true);
                    
                    Map<String, Object> totalMap = getOpenSeibroOptionData(isinCode, map);
                    
                    System.out.println(totalMap);
                    resultList.add(totalMap);
                    
                }
            } else {
            	map = new HashMap<String, Object>();
            	map.put("result", false);
                resultList.add(map);
            }
            
            System.out.println("resultList size : " + resultList.size());
            
            return resultList;
        }
        catch (Exception e)
        {
            s = e.getMessage();
            System.out.println("err OpenSeibroMazzInfo Class : " + e.getStackTrace().toString());
            map.put("result", false);
            resultList.add(map);
        }
       
        if (con != null)
            con.disconnect();
        
		return resultList;
	}
	
	public Map<String, Object> getOpenSeibroOptionData(String isinCode, Map<String, Object> map) {
		HttpURLConnection con = null;
        String s = null; // 에러 메시지
        
        System.out.println("== getOpenSeibroOptionData ======================================================");
        System.out.println("isinCode : " + isinCode);
        
        // option data 추가로 가져오기
		String fullUrl = optionUrl + "&key=" + apiKey + "&params=" + "ISIN:" + isinCode;
        try{
            URL url = new URL(fullUrl);
            con = (HttpURLConnection)url.openConnection();
            con.setRequestProperty("Accept-language", "ko");
            Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(con.getInputStream());
            
            boolean ok = false; // <status>000</status> 획득 여부
            
            Element e;
            NodeList ns = doc.getElementsByTagName("vector");
             
            if (ns.getLength() > 0)
            {
                e = (Element)ns.item(0);
                if (e.getAttribute("result").toString().equals("0")) {
                	s = "0건";
                	System.out.println("err : " + s);
                }
                else { 
                	ok = true; // 성공 여부
                	System.out.println("ok : " + ok);
            	}
            }
            
            if (ok)
            {
                ns = doc.getElementsByTagName("result");
                for (int i = 0; i < ns.getLength(); i++)
                {
                    e = (Element)ns.item(i);
                    
                    // 옵션 데이터
                    String stdate = getEleAttrValue(e, "XRC_BEGIN_DT"); // 행사시작일 
                    String enddate = getEleAttrValue(e, "XRC_EXPRY_DT"); // 행사종료일 
                    String reddate = getEleAttrValue(e, "ERLY_RED_DT"); // 조기상환일 
                    String ytpc = getEleAttrValue(e, "APLI_IRATE"); // 적용이자율 
                    String redamt = getEleAttrValue(e, "ERLY_REDAMT_VAL"); // 조기상환액 
                    String rateamt = getEleAttrValue(e, "INT_PAY_AMT"); // 이자지급액 
                    String remain = getEleAttrValue(e, "ISSU_REMA"); // 최근발행잔액 
                    String exratio = getEleAttrValue(e, "XRC_RATIO"); // 행사비율
                    
                    map.put("ISIN", isinCode + "");
                    map.put("XRC_BEGIN_DT", stdate);
                    map.put("XRC_EXPRY_DT", enddate);
                    map.put("ERLY_RED_DT", reddate);
                    map.put("APLI_IRATE", ytpc);
                    map.put("ERLY_REDAMT_VAL", redamt);
                    map.put("INT_PAY_AMT", rateamt);
                    map.put("CURR_ISSU_REMA", remain);
                    map.put("XRC_RATIO", exratio);
                    
                    map.put("optionResult", true);
                    
                    System.out.println(map);
                    
                }
            } else {
            	map.put("optionResult", false);
            }
            
            return map;
        }
        catch (Exception e)
        {
            s = e.getMessage();
            System.out.println("err getOpenSeibroOptionData Class : " + e.getStackTrace().toString());
            map.put("optionResult", false);
        }
       
        if (con != null)
            con.disconnect();
        
		return map;
	}
	
	private String getEleAttrValue(Element e, String tagName) {
		Element e2 = (Element) e.getElementsByTagName(tagName).item(0);
        String result = e2.getAttribute("value");
		return result;
	}
	
}
