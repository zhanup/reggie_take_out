package com.itheima.reggie.utils;

import com.alibaba.fastjson2.JSONObject;
import com.aliyun.dysmsapi20170525.Client;
import com.aliyun.dysmsapi20170525.models.SendSmsRequest;
import com.aliyun.dysmsapi20170525.models.SendSmsResponse;
import com.aliyun.tea.TeaException;
import com.aliyun.teaopenapi.models.Config;
import com.aliyun.teautil.Common;
import com.aliyun.teautil.models.RuntimeOptions;
import lombok.extern.slf4j.Slf4j;

/**
 * 短信发送工具类
 */
@Slf4j
public class SMSUtils {

	public static Client createClient(String accessKeyId, String accessKeySecret) throws Exception {
		Config config = new Config();
		// 必填，您的 AccessKey ID
		config.setAccessKeyId(accessKeyId);
		// 必填，您的 AccessKey Secret
		config.setAccessKeySecret(accessKeySecret);
		// 访问的域名
		config.endpoint = "dysmsapi.aliyuncs.com";
		return new Client(config);
	}

	public static void sendMessage(String phoneNumbers, String param) throws Exception {
		Client client = SMSUtils.createClient("LTAI5tHbTyEfbbe38pXxQuNx", "m4AF8IRv0DjJpczdaACkpxDCq17iwe");
		SendSmsRequest sendSmsRequest = new SendSmsRequest();
		sendSmsRequest.setSignName("阿里云短信测试");
		sendSmsRequest.setTemplateCode("SMS_154950909");
		sendSmsRequest.setPhoneNumbers(phoneNumbers);
		sendSmsRequest.setTemplateParam("{\"code\":\""+param+"\"}");
		RuntimeOptions runtime = new RuntimeOptions();
		try {
			SendSmsResponse sendSmsResponse = client.sendSmsWithOptions(sendSmsRequest, runtime);
			log.info("sendSmsResponse=[{}]", JSONObject.toJSONString(sendSmsResponse));
		} catch (TeaException teaException) {
			Common.assertAsString(teaException.message);
		} catch (Exception exception) {
			TeaException error = new TeaException(exception.getMessage(), exception);
			Common.assertAsString(error.message);
		}
	}

}
