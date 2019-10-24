package pencil.oauth2autoconfig;


import org.springframework.context.annotation.Bean;
import org.springframework.security.oauth2.common.exceptions.InvalidGrantException;
import org.springframework.security.oauth2.common.util.RandomValueStringGenerator;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.AuthorizationCodeServices;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * 主要是用来创建code与保存
 */
public class OssAusthorizationCodeServices implements AuthorizationCodeServices {
    private final ConcurrentMap<String, OAuth2Authentication> codes = new ConcurrentHashMap<>();

    @Bean
    public RandomValueStringGenerator randomValueStringGenerator(){
        return new RandomValueStringGenerator();
    }

    /**
     * 获取code 与 销毁 等工作
     * @param code
     * @return
     * @throws InvalidGrantException
     */
    @Override
    public OAuth2Authentication consumeAuthorizationCode(String code) throws InvalidGrantException {
        System.out.println("consume authorization code");
        return codes.remove(code);
    }

    /**
     * 创建code 每个一个字母加一个accountId的数字
     * @param oAuth2Authentication
     * @return
     */
    @Override
    public String createAuthorizationCode(OAuth2Authentication oAuth2Authentication) {
        String code, client, random;

        do {
            random = randomValueStringGenerator().generate().replaceAll("[0-9]", "");
        }
        while (random.length() < 4);

        random = random.substring(0, 4);

        client = oAuth2Authentication.getOAuth2Request().getClientId();

        client = "1038";

        StringBuffer buffer = new StringBuffer();

        for (int i = 0; i < 4; i++) {
            buffer.append(random, i, i+1);
            buffer.append(client, i, i+1);
        }

        buffer.append(client.substring(4));

        code = buffer.toString();

        codes.put(code, oAuth2Authentication);

        System.out.println(String.format("create authorization code %s, filter is %s", code, code.replaceAll("[a-zA-Z]", "")));

        return code;
    }

}