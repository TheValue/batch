package com.values.batch.main.util;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import java.io.ByteArrayOutputStream;
import java.io.File;
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
import java.io.StringReader;

/*
 * 2023-02-03
 * https://opendart.fss.or.kr/guide/detail.do?apiGrpCd=DS001&apiId=2019018
 * DART open api 를 통한 공시정보 기업의 고유번호 리스트 데이터를 조회 
 * 'DART_고유번호 공시정보' 
 * 신청일    2023년 
 * URL - https://opendart.fss.or.kr/api/corpCode.xml
 * 인증키 - 5077aca71b48161728f18cb794caad946134abbc
 * GET 방식으로 호출 시 응답을 zip파일 형태로 제공 받음
 * 해당 파일을 string으로 파싱하여 처리
 */
public class DartDataCorpCode {
	private String mainUrl = "https://opendart.fss.or.kr/api/corpCode.xml";
	private String apiKey = "5077aca71b48161728f18cb794caad946134abbc";
	
	public Map<String, Object> getDartDataCorpCode(String uploadPath) {
		Map<String, Object> resultMap = new HashMap<String, Object>();
		InputStream is = null;
		FileOutputStream os = null;

        try{
            URL url = new URL(mainUrl + "?crtfc_key=" + apiKey);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            int responseCode = conn.getResponseCode();

            // Status 가 200 일 때
            if (responseCode == HttpURLConnection.HTTP_OK) {
                String fileName = "";
                String disposition = conn.getHeaderField("Content-Disposition");
                
                // 일반적으로 Content-Disposition 헤더에 있지만 
                if (disposition != null) {
                    String target = "filename=";
                    int index = disposition.indexOf(target);
                    if (index != -1) {
                        fileName = disposition.substring(index + target.length() + 1);
                    }
                } else {
                	fileName = "corpCode.xml";
                }

                is = conn.getInputStream();
                os = new FileOutputStream(new File(uploadPath, fileName));
                
                // 전달받은 inputStream을 읽어서 데이터를 추출
                ZipInputStream zis = new ZipInputStream(is);
                ZipEntry ze = null;

        		byte[] buf = new byte[4096];
        		
        		try {
        			while( (ze = zis.getNextEntry()) != null ) {
        				ByteArrayOutputStream baos = new ByteArrayOutputStream();
        				
        				int len = 0;
        				while( (len = zis.read(buf, 0, 4096) ) != -1 ){
        					baos.write(buf, 0, len);
        				}
        				
        				Document doc = convertStringToXml(baos.toString());
        				// 파싱한 XML 내용을 한줄씩 읽어서 테이블에 저장
        				resultMap = getDartCorpCodeInsertData(doc);
        				
        				baos.flush();
        				baos.close();
        				zis.closeEntry();
        			}
        			
        			zis.close();

        		} catch (IOException e) {
        			e.printStackTrace();
        		}
                os.close();
                is.close();
                System.out.println("File downloaded");
                
            } else {
                System.out.println("No file to download. Server replied HTTP code: " + responseCode);
            }
            conn.disconnect();
        } catch (Exception e){
            System.out.println("An error occurred while trying to download a file.");
            e.printStackTrace();
            try {
                if (is != null){
                    is.close();
                }
                if (os != null){
                    os.close();
                }
            } catch (IOException e1){
                e1.printStackTrace();
            }
        }
        
        return resultMap;
	}
	
	// XML 파싱하여 저장할 데이터를 가공
	private Map<String, Object> getDartCorpCodeInsertData(Document doc) {
		Map<String, Object> paramMap = new HashMap<String, Object>();
		
		List<Map<String, Object>> list = new ArrayList<Map<String, Object>>();
		Element e;
		NodeList ns = doc.getElementsByTagName("list");
        System.out.println("ns.getLength() : " + ns.getLength());
        for (int i = 0; i < ns.getLength(); i++)
        {
            e = (Element)ns.item(i);
            String corpCode = e.getElementsByTagName("corp_code").item(0).getTextContent();
            String corpName = e.getElementsByTagName("corp_name").item(0).getTextContent();
            String stockCode = e.getElementsByTagName("stock_code").item(0).getTextContent();
            String modifyDate = e.getElementsByTagName("modify_date").item(0).getTextContent();
            
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("CORP_CODE", corpCode + "");
            map.put("CORP_NAME", corpName + "");
            map.put("STOCK_CODE", stockCode + "");
            map.put("MODIFY_DATE", modifyDate + "");
            
            list.add(map);
        }
        
        paramMap.put("list", list);
        paramMap.put("TOTAL_CNT", list.size());
        
        System.out.println("getDartCorpCodeInsertData list : " + list.size());
        
        if(list.size() == 0) {
        	paramMap.put("result", false);
        } else {
        	paramMap.put("result", true);
        }
        
        return paramMap;
	}
	
	// XML 형태의 스트링 문자열을 XML로 변환
	private static Document convertStringToXml(String xmlString) {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        try {
            // optional, but recommended
            // process XML securely, avoid attacks like XML External Entities (XXE)
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            DocumentBuilder builder = dbf.newDocumentBuilder();
            Document doc = builder.parse(new InputSource(new StringReader(xmlString)));
            return doc;

        } catch (ParserConfigurationException | IOException | SAXException e) {
            throw new RuntimeException(e);
        }

    }
	
}
