package cn.shu.weichat.utils.tools;

import lombok.extern.log4j.Log4j2;
import cn.shu.weichat.beans.BaseMsg;
import cn.shu.weichat.core.Core;
import cn.shu.weichat.utils.MyHttpClient;
import cn.shu.weichat.utils.enums.MsgTypeEnum;
import cn.shu.weichat.utils.enums.URLEnum;
import org.apache.http.HttpEntity;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.FileOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 下载工具类
 * 
 * @author SXS
 * @date 创建时间：2017年4月21日 下午11:18:46
 * @version 1.1
 *
 */
@Log4j2
public class DownloadTools {
	private static Core core = Core.getInstance();
	private static MyHttpClient myHttpClient = core.getMyHttpClient();

	/**
	 * 处理下载任务
	 * 
	 * @author SXS
	 * @date 2017年4月21日 下午11:00:25
	 * @param msg
	 * @param msgTypeEnum
	 * @param path
	 * @return
	 */
	public static Object getDownloadFn(BaseMsg msg, MsgTypeEnum msgTypeEnum, String path) {
		Map<String, String> headerMap = new HashMap<String, String>();
		List<BasicNameValuePair> params = new ArrayList<BasicNameValuePair>();
		String url = "";
		switch (msgTypeEnum) {
			case PIC:
			case EMOTION:
				url = String.format(URLEnum.WEB_WX_GET_MSG_IMG.getUrl(), (String) core.getLoginInfoMap().get("url"));
				break;
			case VOICE:
				url = String.format(URLEnum.WEB_WX_GET_VOICE.getUrl(), (String) core.getLoginInfoMap().get("url"));
				break;
			case VIDEO:
				headerMap.put("Range", "bytes=0-");
				url = String.format(URLEnum.WEB_WX_GET_VIEDO.getUrl(), (String) core.getLoginInfoMap().get("url"));
				break;
			case APP:
			case MEDIA:
				headerMap.put("Range", "bytes=0-");
				url = String.format(URLEnum.WEB_WX_GET_MEDIA.getUrl(), (String) core.getLoginInfoMap().get("fileUrl"));
				params.add(new BasicNameValuePair("sender", msg.getFromUserName()));
				params.add(new BasicNameValuePair("mediaid", msg.getMediaId()));
				params.add(new BasicNameValuePair("filename", msg.getFileName()));
				break;
		}
		params.add(new BasicNameValuePair("msgid", msg.getNewMsgId()));
		params.add(new BasicNameValuePair("skey", (String) core.getLoginInfoMap().get("skey")));
		HttpEntity entity = myHttpClient.doGet(url, params, true, headerMap);
		try {
			OutputStream out = new FileOutputStream(path);
			byte[] bytes = EntityUtils.toByteArray(entity);
			out.write(bytes);
			out.flush();
			out.close();
		} catch (Exception e) {
			log.info(e.getMessage());
			return false;
		}
		return null;
	};

}