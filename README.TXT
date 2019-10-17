1.访问oauth2 服务(GET)
http://localhost:8080/oauth/authorize?response_type=code&client_id=client&state=1234

输入用户名: user 密码: 123456

2.点击“approve” 同意授权获取code返回：(GET)
http://localhost:8081/service/hello?code=9TvbJ9&state=1234

3.携带授权之后返回的code 获取token(POST)
http://localhost:8080/oauth/token?grant_type=authorization_code&code=HzQBNR&client_id=client&client_secret=123456

4、携带步骤3获取的到token，进行资源访问(GET)：
http://localhost:8081/api/welcome?access_token=669f16f0-0d5b-42b0-a64b-04259b949c2e
