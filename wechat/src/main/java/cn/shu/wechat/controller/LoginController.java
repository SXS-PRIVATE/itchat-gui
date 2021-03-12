package cn.shu.wechat.controller;

import cn.shu.wechat.api.WeChatTool;
import cn.shu.wechat.utils.*;
import cn.shu.wechat.api.ContactsTools;
import cn.shu.wechat.core.Core;
import cn.shu.wechat.face.IMsgHandlerFace;
import cn.shu.wechat.service.ILoginService;
import cn.shu.wechat.runnable.CheckLoginStatusRunnable;
import cn.shu.wechat.runnable.UpdateContactRunnable;
import cn.shu.wechat.api.DownloadTools;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * 登陆控制器
 *
 * @author SXS
 * @date 创建时间：2017年5月13日 下午12:56:07
 * @version 1.1
 *
 */
@Log4j2
@Component
public class LoginController {
	/**
	 * 登陆服务实现类
	 */
	@Resource
	private ILoginService loginService;

	/**
	 * 更新联系人的线程处理器
	 */
	@Resource
	private CheckLoginStatusRunnable checkLoginStatusRunnable;

	/**
	 * 检测登录状态的线程
	 */
	@Resource
	private UpdateContactRunnable updateContactRunnable;

	/**
	 * 图表工具类
	 */
	@Resource
	private ChartUtil chart;

	public void login(String qrPath) {

		if (Core.isAlive()) {
			// 已登陆
			log.info("itchat4j已登陆");
			return;
		}
		while (true) {
			Process process = null;
			for (int count = 0; count < 10; count++) {
				log.info("获取UUID");
				while (loginService.getUuid() == null) {
					log.info("1. 获取微信UUID");
					while (loginService.getUuid() == null) {
						log.warn("1.1. 获取微信UUID失败，两秒后重新获取");
						SleepUtils.sleep(2000);
					}
				}
				log.info("2. 获取登陆二维码图片");
				qrPath = qrPath + File.separator + Config. DEFAULT_QR;
				if (loginService.getQR(qrPath)) {
					try {
						// 打开登陆二维码图片
						process = CommonTools.printQr(qrPath);
					} catch (Exception e) {
						log.info(e.getMessage());
					}
					break;
				} else if (count == 9) {
					log.error("2.2. 获取登陆二维码图片失败，系统退出");
					System.exit(0);
				}
			}
			log.info("3. 请扫描二维码图片，并在手机上确认");
			if (!Core.isAlive()) {
				loginService.login();
				//登录成功，关闭打开的二维码图片
				CommonTools.closeQr(process);
				Core.setAlive(true);
				log.info(("登陆成功"));
				break;
			}
			log.info("4. 登陆超时，请重新扫描二维码图片");
		}

		log.info("5. 登陆成功，微信初始化");
		if (!loginService.webWxInit()) {
			log.info("6. 微信初始化异常");
			System.exit(0);
		}

		log.info("6. 开启微信状态通知");
		loginService.wxStatusNotify();

		log.info("7. 清除。。。。");
		CommonTools.clearScreen();
		log.info(String.format("欢迎回来， %s", Core.getNickName()));

		log.info("8. 开始接收消息");
		loginService.startReceiving();

		log.info("9. 获取联系人信息");
		loginService.webWxGetContact();

		log.info("10. 获取群好友及群好友列表");
		loginService.WebWxBatchGetContact();

		Runnable chartRunnable = () -> {

				while (true){try{
					chart.create();
					//SleepUtils.sleep(1000 * 10 );
					SleepUtils.sleep(1000 * 60 * 60 * 8);
				}catch (Exception e){e.printStackTrace();}
				}

		};
		ExecutorsUtil.getGlobalExecutorService().submit(chartRunnable);

		log.info("11. 缓存本次登陆好友相关消息");
		// 登陆成功后缓存本次登陆好友相关消息（NickName, UserName）
		WeChatTool.setUserInfo();

		log.info("12.开启微信状态检测线程");
		ExecutorsUtil.getGlobalExecutorService().submit(checkLoginStatusRunnable);


		log.info("13. 下载联系人头像");

		for (Map.Entry<String, JSONObject> entry : Core.getGroupMap().entrySet()) {
			Runnable runnable = () -> {
				JSONObject value = entry.getValue();
				String headImgUrl = DownloadTools.downloadHeadImg(value.getString("HeadImgUrl"), value.getString("UserName"));
				Core.getContactHeadImgPath().put(value.getString("UserName"), headImgUrl);
				//System.out.println("头像已下载：" + headImgUrl);
			};
			ExecutorsUtil.getHeadImageDownloadExecutorService().execute(runnable);

		}
		for (Map.Entry<String, JSONObject> entry : Core.getContactMap().entrySet()) {
			Runnable runnable = () -> {
				JSONObject value = entry.getValue();
				String headImgUrl = DownloadTools.downloadHeadImg(value.getString("HeadImgUrl"), value.getString("UserName"));
				Core.getContactHeadImgPath().put(value.getString("UserName"), headImgUrl);
				//System.out.println("头像已下载：" + headImgUrl);
			};
			ExecutorsUtil.getHeadImageDownloadExecutorService().submit(runnable);

		}
		ExecutorsUtil.getHeadImageDownloadExecutorService().shutdown();
		ExecutorsUtil.getGlobalExecutorService().submit(() -> {
			try {
				//等待头像下载完成
				boolean b = ExecutorsUtil.getHeadImageDownloadExecutorService().awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			log.info("14.开启好友列表更新线程");
			new Thread(updateContactRunnable,"UpdateContactThread").start();
			log.info("头像下载完成");
		});


	}
}