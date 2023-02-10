package com.values.batch.main.controller;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import com.values.batch.main.persistence.MainMapper;
import com.values.batch.main.service.MainService;

@RestController 
public class MainController {
	
	@Autowired
	private MainService mainService;
	
	@Autowired
	private MainMapper mainMapper;
	
	@GetMapping("/")
	public String main() {
		return "index.html";
	}
	
	@GetMapping("/api/hello")
    public String hello() {
        return "야 이거 가져오냐 ? " + new Date() + "\n";
    }
	
	// 배치 리스트 조회 
	@ResponseBody
	@GetMapping("/api/findBatchList")
    public List<Map<String, Object>> findBatchList() {
		List<Map<String, Object>> list = mainMapper.findBatchList(new HashMap<String, Object>());
        return list;
    }
	
	/*  스케줄링 시간 참고
	 *  
	    *           *　　　　　　*　　　　　　*　　　　　　*　　　　　　*
		초(0-59)   분(0-59)　　시간(0-23)　　일(1-31)　　월(1-12)　　요일(0-7) 
		각 별 위치에 따라 주기를 다르게 설정 할 수 있다.
		순서대로 초-분-시간-일-월-요일 순이다. 그리고 괄호 안의 숫자 범위 내로 별 대신 입력 할 수도 있다.
		요일에서 0과 7은 일요일이며, 1부터 월요일이고 6이 토요일이다.
	 */
	
	// 스케줄링
	// 공공데이터포털 - 발행채권정보 
	// 데일리 배치 - 1일 1회 (오전 4시)
	// SPO_DATA_BOND_INFO
	@Scheduled(cron = "0 0 4 * * ?")
	public void scheduleOpenDataBondInfo() {
	    System.out.println("새벽 4시 배치 - scheduleOpenDataBondInfo 시작");
	    
	    // 발행채권정보 데이터 추출 및 저장
	    Map<String, Object> params = new HashMap<String, Object>();
	    Map<String, Object> result = mainService.saveOpenApiBondInfo(params);
	    
	    // 결과 로그 기록
	    result.put("LOG_TYPE", "공공 - 채권정보");
	    mainService.saveBatchLog(result);
	    System.out.println("새벽 1시 배치 - scheduleOpenDataBondInfo 종료");
	}
	
	// 스케줄링
	// 공공데이터포털 - 주식 정보 (주가 데이터)
	// 데일리 배치 - 1일 1회 (오전 5시)
	// SPO_DATA_STOCK_INFO
	@Scheduled(cron = "0 0 5 * * ?")
	public void scheduleOpenDataStockInfo() {
	    System.out.println("새벽 5시 배치 - scheduleOpenDataStockInfo 시작");
	    
	    // 주식정보 데이터 추출 및 저장
	    Map<String, Object> params = new HashMap<String, Object>();
	    Map<String, Object> result = mainService.saveOpenApiStockInfo(params);
	    
	    // 결과 로그 기록
	    result.put("LOG_TYPE", "공공 - 주식정보");
	    mainService.saveBatchLog(result);
	    System.out.println("새벽 5시 배치 - scheduleOpenDataStockInfo 종료");
	}
	
	// 스케줄링
	// 공공데이터포털 - 지수 시세 (KOSPI / KOSDAQ)
	// 데일리 배치 - 1일 1회 (오전 2시)
	// SPO_DATA_KOS_INFO
	@Scheduled(cron = "0 0 2 * * ?")
	public void scheduleOpenDataKosInfo() {
	    System.out.println("새벽 2시 배치 - scheduleOpenDataKosInfo 시작");
	    
	    // 지수 시세 데이터 추출 및 저장
	    Map<String, Object> params = new HashMap<String, Object>();
	    Map<String, Object> result = mainService.saveOpenApiKosInfo(params);
	    
	    // 결과 로그 기록
	    result.put("LOG_TYPE", "공공 - 지수시세");
	    mainService.saveBatchLog(result);
	    System.out.println("새벽 2시 배치 - scheduleOpenDataKosInfo 종료");
	}
	
	// 스케줄링
	// 크롤링 - 신용등급 커브 정보 데이터 크롤링
	// 데일리 배치 - 1일 1회 (오전 7시)
	// SPO_KOFIA_BOND
	// 신규로 크롤링하는 KIS-NET 데이터는 배치가 아닌 평가기준일 선택 시 날짜 체크하여 없으면 1회 크롤링하는 것으로 함 (중요!!)
	@Scheduled(cron = "0 0 7 * * ?")
	public void scheduleCrawlCurveInfo() {
	    System.out.println("오전 7시 배치 - scheduleCrawlCurveInfo 시작");
	    
	    // 신용등급 커브 정보 데이터 추출 및 저장
	    Map<String, Object> params = new HashMap<String, Object>();
	    Map<String, Object> result = mainService.saveCrawlCurveInfo(params);
	    
	    // 결과 로그 기록
	    result.put("LOG_TYPE", "크롤링 - 커브정보");
	    mainService.saveBatchLog(result);
	    System.out.println("오전 7시 배치 - scheduleCrawlCurveInfo 종료");
	}
	
	// 스케줄링
	// DART - DART 기업 공기정보 기업코드 정보 데이터 크롤링
	// 데일리 배치 - 1일 1회 (오전 6시)
	// SPO_DATA_MAZZ_CODE
	// findOpenDataBondInfo 메소드를 사용하므로 scheduleOpenDataBondInfo 메소드보다 뒷 시간으로 배치가 진행되어야 함
	@Scheduled(cron = "0 0 6 * * ?")
	public void scheduleOpenDartMazzCode() {
	    System.out.println("오전 6시 배치 - scheduleOpenDartMazzCode 시작");
	    
	    // DART 기업 공시코드 조회 
	    Map<String, Object> params = new HashMap<String, Object>();
	    Map<String, Object> result = mainService.saveOpenDartMazzCode(params);
	    
	    // 결과 로그 기록
	    result.put("LOG_TYPE", "DART - 기업공시코드");
	    mainService.saveBatchLog(result);
	    System.out.println("오전 6시 배치 - scheduleOpenDartMazzCode 종료");
	}
	
	// 스케줄링
	// DART - DART 메짜닌 발행정보 데이터 크롤링
	// 데일리 배치 - 1일 1회 (오전 6시 30분)
	// SPO_DATA_MAZZ_INFO
	// findOpenDataBondInfo 메소드를 사용하므로 scheduleOpenDataBondInfo 메소드보다 뒷 시간으로 배치가 진행되어야 함
	@Scheduled(cron = "0 30 6 * * ?")
	public void scheduleOpenDartMazzInfo() {
	    System.out.println("오전 6시 30분 배치 - scheduleOpenDartMazzInfo 시작");
	    
	    // DART 기업 메짜닌정보 조회
	    Map<String, Object> params = new HashMap<String, Object>();
	    Map<String, Object> result = mainService.saveOpenDartMazzInfo(params);
	    
	    // 결과 로그 기록
	    result.put("LOG_TYPE", "DART - 메짜닌정보");
	    mainService.saveBatchLog(result);
	    System.out.println("오전 6시 30분 배치 - scheduleOpenDartMazzInfo 종료");
	}
	
	// 스케줄링
	// SEIBRO - SEIBRO 메짜닌 발행정보 데이터 크롤링
	// 데일리 배치 - 1일 1회 (오전 6시 45분)
	// SPO_DATA_SEIBRO_MAZZ_INFO
	// findOpenDataBondInfo 메소드를 사용하므로 scheduleOpenDataBondInfo 메소드보다 뒷 시간으로 배치가 진행되어야 함
	@Scheduled(cron = "0 45 6 * * ?")
	public void scheduleOpenSeibroMazzInfo() {
	    System.out.println("오전 6시 45분 배치 - scheduleOpenSeibroMazzInfo 시작");
	    
	    // DART 기업 메짜닌정보 조회
	    Map<String, Object> params = new HashMap<String, Object>();
	    Map<String, Object> result = mainService.saveOpenSeibroMazzInfo(params);
	    
	    // 결과 로그 기록
	    result.put("LOG_TYPE", "SEIBRO - 메짜닌정보");
	    mainService.saveBatchLog(result);
	    System.out.println("오전 6시 45분 배치 - scheduleOpenSeibroMazzInfo 종료");
	}
}