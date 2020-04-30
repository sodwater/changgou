package weixin;

import com.github.wxpay.sdk.WXPayUtil;
import org.junit.Test;

public class WeixinUtilTest {

    @Test
    public void testDemo(){
        String s = WXPayUtil.generateNonceStr();
        System.out.println(s);
    }
}
