package com.mall.admin.user.controller;

import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.UUID;

import javax.annotation.Resource;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

import com.mall.admin.user.service.UserService;
import com.mall.common.spring.security.service.ShaEncoder;
import com.mall.common.util.crypto.Hash;
import com.mall.mail.service.MailService;
import com.sun.mail.util.BASE64DecoderStream;

import sun.misc.BASE64Encoder;


/**
 * @설명			: 관리자 회원가입  
 * @작성일		: 2017. 1. 10. 오전 10:27:55
 * @작성자		: Seo Myeongseok(sirosms@gmail.com)
 * @version 	: 12st.V1.0
 */

@Controller
public class UserController {
	
	private static final Logger logger = LoggerFactory.getLogger(UserController.class);

	@Resource
	private ShaEncoder encoder;
	
	@Resource
	private UserService userService
	;
	@Resource
	private MailService mailService;
	
	@RequestMapping("/admin/user/doJoin")
	public String doJoin() {
		return "/admin/user/join";
	}
	
	@RequestMapping("admin/user/findIdView")
	public String findIdView() {
		return "/admin/user/findId";
	}
	
	@RequestMapping("/admin/user/findPwdView")
	public String findPwdView() {
		return "/admin/user/findPwd";
	}
	
	@RequestMapping(value="admin/user/findId" , produces = {"application/json"})
	public Map<String, Object> findId(@RequestParam Map<String, String> paramMap) {
		
		List<Map<String, Object>> loginIdList = userService.selectLoginId(paramMap);
		
		if(loginIdList.size()>0){
			Map<String, Object> mailMap = new HashMap<String, Object>();
			String content = "당신의 아이디 입니다.\n\n";
			String title = "아이디 찾기 메일";
			String email = paramMap.get("email").toString();
			
			for(int i = 0 ; loginIdList.size()>i ; i++){
				content += loginIdList.get(i).get("login_id")+"\n";
			}
			
			mailMap.put("content", content);
			mailMap.put("title", title);
			mailMap.put("email", email);
			mailService.sendMail(mailMap);
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("result", loginIdList.size()>0?"ok":"fail");
		return map;
	}
	
	@RequestMapping(value="/admin/user/findPwd" , produces = {"application/json"})
	public Map<String, Object> findPwd(@RequestParam Map<String, String> paramMap) {
		
		Map<String, Object> mailMap = userService.selectPassWd(paramMap);
		
		if(mailMap !=null && !"".equals(mailMap.get("PASSWD"))){
			String content = "당신의 비밀번호는 입니다.\n\n";
			String title = "비밀번호 찾기 메일";
			String email = mailMap.get("email").toString();
			
			String password = randomValue("C",10);
			paramMap.put("passwd", password);
			content += encoder.encoding(password) ;
			userService.updatePassWd(paramMap);
			
			mailMap.put("content", content);
			mailMap.put("title", title);
			mailMap.put("email", email);
			mailService.sendMail(mailMap);
		}
		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("result", !"".equals(mailMap.get("PASSWD"))?"ok":"fail");
		return map;
	}
	
	
	@RequestMapping("/admin/user/insertUser")
	public String insertUser(@RequestParam Map<String, String> paramMap) {
		String password = encoder.encoding(paramMap.get("passwd")) ;
		paramMap.put("passwd", password);
		userService.insertUser(paramMap);
		
		return "/admin/user/login";
	}
	
	@RequestMapping(value="/user/ajaxDupIdChk", produces = {"application/json"})
	public Map<String, Object> ajaxDupIdChk(@RequestParam Map<String, String> paramMap) {
		int dupResult =  userService.checkLoginId(paramMap);
		//node는 서버에서받은 root의 id값이다. 
		Map<String, Object> map = new HashMap<String, Object>();
		
		map.put("result", dupResult);

		
		return map;
	}
	
	/**
	 * 접근권한 없을경우 페이지로 이동
	 * @param paramMap
	 * @return
	 */
	@RequestMapping("/common/accessDeniedPag")
	public String accessDenied(@RequestParam Map<String, String> paramMap) {
		
		return "/common/accessDeniedPage";
	}
	
	/**
	 * 비밀번호 생성
	 * @param type
	 * @param cnt
	 * @return
	 */
	public String randomValue(String type, int cnt) {
		
		StringBuffer strPwd = new StringBuffer();
		char str[] = new char[1];
		// 특수기호 포함
		if (type.equals("P")) {
			for (int i = 0; i < cnt; i++) {
				str[0] = (char) ((Math.random() * 94) + 33);
				strPwd.append(str);
			}
		// 대문자로만
		} else if (type.equals("A")) {
			for (int i = 0; i < cnt; i++) {
				str[0] = (char) ((Math.random() * 26) + 65);
				strPwd.append(str);
			}
		// 소문자로만
		} else if (type.equals("S")) {
			for (int i = 0; i < cnt; i++) {
				str[0] = (char) ((Math.random() * 26) + 97);
				strPwd.append(str);
			}
		// 숫자형으로
		} else if (type.equals("I")) {
			int strs[] = new int[1];
			for (int i = 0; i < cnt; i++) {
				strs[0] = (int) (Math.random() * 9);
				strPwd.append(strs[0]);
			}
		// 소문자, 숫자형
		} else if (type.equals("C")) {
			Random rnd = new Random();
			for (int i = 0; i < cnt; i++) {
				if (rnd.nextBoolean()) {
					strPwd.append((char) ((int) (rnd.nextInt(26)) + 97));
				} else {
					strPwd.append((rnd.nextInt(10)));
				}
			}
		}
		return strPwd.toString();
	}    


}
