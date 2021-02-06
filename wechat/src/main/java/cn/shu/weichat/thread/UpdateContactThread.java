package cn.shu.weichat.thread;

import cn.shu.weichat.api.WechatTools;
import cn.shu.weichat.core.Core;
import cn.shu.weichat.service.ILoginService;
import cn.shu.weichat.utils.SleepUtils;
import lombok.extern.log4j.Log4j2;
import cn.shu.weichat.service.impl.LoginServiceImpl;

/**
 * 检查微信在线状态
 * <p>
 * 如何来感知微信状态？
 * 微信会有心跳包，LoginServiceImpl.syncCheck()正常在线情况下返回的消息中retcode报文应该为"0"，心跳间隔一般在25秒，
 * 那么可以通过最后收到正常报文的时间来作为判断是否在线的依据。若报文间隔大于60秒，则认为已掉线。
 * </p>
 * 
 * @author SXS
 * @date 创建时间：2017年5月17日 下午10:53:15
 * @version 1.1
 *
 */
@Log4j2
public class UpdateContactThread implements Runnable {
	private Core core = Core.getInstance();
	private final ILoginService loginService = LoginServiceImpl.loginService;
	@Override
	public void run() {
		while (core.isAlive()) {
			//log.info("1. 更新联系人信息");
			loginService.webWxGetContact();

			//log.info("2. 更新群好友及群好友列表");
			loginService.WebWxBatchGetContact();

			//log.info("3. 更新本次登陆好友相关消息");
			WechatTools.setUserInfo(); // 登陆成功后缓存本次登陆好友相关消息（NickName, UserName）

			SleepUtils.sleep(10 * 1000); // 休眠10秒
		}
	}

}