package com.example.code.controller;

import cn.hutool.crypto.SecureUtil;
import cn.hutool.http.HttpUtil;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.example.code.model.InMessage;
import com.example.code.model.OutMessage;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Arrays;

@RestController
@RequestMapping("wechat/publicAccount")
public class WechatController {

  private static final String TOKEN = "abcd";

  @GetMapping("validate")
  public String validate(HttpServletRequest req,HttpServletResponse res) throws IOException {
    String signature = req.getParameter("signature");
    String timestamp = req.getParameter("timestamp");
    String nonce = req.getParameter("nonce");
    String echostr = req.getParameter("echostr");
    System.out.println(signature);
    //1.将token, timestamp,nonce三个参数字典排序
    String[] userInfoArray = {timestamp, nonce, TOKEN};
    Arrays.sort(userInfoArray);
    //2.将三个参数字符串拼接成一个字符串进行sha1加密
    StringBuilder userInfoString = new StringBuilder();
    for (String userInfo : userInfoArray) {
      userInfoString.append(userInfo);
    }

    String sha1 = SecureUtil.sha1(userInfoString.toString());
    if (sha1.equals(signature)){
      return echostr;
    }
    return null;
  }

  @PostMapping(value="validate", produces="application/xml;charset=UTF-8")
  public Object handleMessage(@RequestBody InMessage inMessage){
    OutMessage outMessage = new OutMessage();
    outMessage.setFromUserName(inMessage.getToUserName());
    outMessage.setToUserName(inMessage.getFromUserName());
    outMessage.setMsgType(inMessage.getMsgType());
    outMessage.setCreateTime(System.currentTimeMillis());
    String msgType = inMessage.getMsgType();
    if("text".equals(msgType)){
      String inContent = inMessage.getContent();
      if (inContent.contains("游戏")){
        outMessage.setContent("不要再玩游戏了");
      }else if (inContent.contains("动漫")){
        outMessage.setContent("进击的巨人");
      }else{
        outMessage.setContent(inMessage.getContent());
      }
    }else if("image".equals(msgType)){
      outMessage.setMediaId(new String[]{inMessage.getMediaId()});
    }else if ("event".equals(msgType)){
      String event = inMessage.getEvent();
      if("subscribe".equals(event)){
        outMessage.setMsgType("text");
        outMessage.setContent("这都被你找到了，可真是独具慧眼吖");
      }
    }
    return outMessage;
  }

  @GetMapping("getAccessToken")
  public String getAccessToken(){
    String APPID = "wxc73c65ec3f7ec144";
    String APPSECRET = "73e793dee95f7c64524edab2b16935c4";
    String url = "https://api.weixin.qq.com/cgi-bin/token?grant_type=client_credential&appid=" + APPID +
            "&secret=" + APPSECRET;
    // 利用hutool的http工具类请求获取access_token
    String result = HttpUtil.get(url);
    // 将结果解析为json
    JSONObject jsonObject = JSONUtil.parseObj(result);
    // 获取access_token
    String accessToken = jsonObject.getStr("access_token");
    if (!StringUtils.isEmpty(accessToken)){
      // 将access_token存入redis
//      stringRedisTemplate.opsForValue().set("access_token", accessToken);
    }
    return accessToken;
  }

//  @GetMapping("createMenu")
//  public String createMenu(){
//    // 从redis中取出access_token
//    String accessToken = stringRedisTemplate.opsForValue().get("access_token");
//    String url = "https://api.weixin.qq.com/cgi-bin/menu/create?access_token=" + accessToken;
//    // 创建菜单的请求体
//    String body = "{\n" +
//            "     \"button\":[\n" +
//            "     {\t\n" +
//            "          \"type\":\"click\",\n" +
//            "          \"name\":\"位置\",\n" +
//            "          \"key\":\"button_location\"\n" +
//            "      }]}";
//    return HttpUtil.post(url, body);
//  }


}
