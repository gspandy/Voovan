package org.voovan.http.server;

import org.voovan.http.message.Request;
import org.voovan.http.message.packet.Cookie;
import org.voovan.tools.TObject;
import org.voovan.tools.log.Logger;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * HTTPServer 请求对象
 * @author helyho
 *
 * Voovan Framework.
 * WebSite: https://github.com/helyho/Voovan
 * Licence: Apache v2 License
 */
public class HttpRequest extends Request {

	private HttpSession session;
	private String remoteAddres;
	private int remotePort;
	private String characterSet;
	private Map<String, String> parameters;

	private Map<String, Object> attributes;
	
	protected HttpRequest(Request request,String characterSet){
		super(request);
		this.characterSet=characterSet;
		parameters = new HashMap<String, String>();
		attributes = new HashMap<String, Object>();
		parseQueryString();
	}

	/**
	 * 根据 Cookie 名称取 Cookie
	 *
	 * @param name  Cookie 名称
	 * @return Cookie
	 */
	public Cookie getCookie(String name){
		for(Cookie cookie : this.cookies()){
			if(cookie.getName().equals(name)){
				return cookie;
			}
		}
		return null;
	}

	/**
	 * 获取 Session
	 *
	 * @return HTTP-Session 对象
	 */
	public HttpSession getSession() {
		return session;
	}

	/**
	 * 设置一个 Session
	 *
	 * @param session  HTTP-Session 对象
	 */
	protected void setSession(HttpSession session) {
		this.session = session;
	}



	/**
	 * 获取对端连接的 IP
	 *
	 * @return 对端连接的 IP
	 */
	public String getRemoteAddres() {
		String xForwardedFor = header().get("X-Forwarded-For");
		String xRealIP = header().get("X-Real-IP");
		if (xRealIP != null) {
			return xRealIP;
		} else if (xForwardedFor != null) {
			return xForwardedFor.split(",")[0].trim();
		}else{
			return remoteAddres;
		}
	}

	/**
	 * 设置对端连接的 IP
	 *
	 * @param remoteAddres 对端连接的 IP
	 */
	protected void setRemoteAddres(String remoteAddres) {
		this.remoteAddres = remoteAddres;
	}

	/**
	 * 获取对端连接的端口
	 *
	 * @return 对端连接的端口
	 */
	public int getRemotePort() {
		return remotePort;
	}

	/**
	 * 设置对端连接的端口
	 *
	 * @param port 对端连接的端口
	 */
	protected void setRemotePort(int port) {
		this.remotePort = port;
	}

	/**
	 * 获取当前默认字符集
	 *
	 * @return 字符集
	 */
	public String getCharacterSet() {
		return characterSet;
	}

	/**
	 * 设置当前默认字符集
	 *
	 * @param charset 字符集
	 */
	public void setCharacterSet(String charset) {
		this.characterSet = charset;
	}
	
	/**
	 * 获取请求字符串
	 *
	 * @return 请求字符串
	 */
	public String getQueryString(){
		return getQueryString(characterSet);
	}
	
	/**
	 * 获取请求变量集合
	 *
	 * @return 请求变量集合
	 */
	public Map<String, String> getParameters() {
		return parameters;
	}
	
	/**
	 * 获取请求变量
	 *
	 * @param paramName 请求变量名称
	 * @return 请求变量值
	 */
	public String getParameter(String paramName){
		return parameters.get(paramName);
	}
	
	/**
	 * 获取请求变量
	 *
	 * @return 请求变量集合
	 */
	public List<String> getParameterNames(){
		return Arrays.asList(parameters.keySet().toArray(new String[]{}));
	}

	/**
	 * 获取请求属性.此属性是会话级的
	 * @return 返回请求属性
     */
	public Map<String, Object> getAttributes() {
		return attributes;
	}

	/**
	 * 获取请求属性值
	 * @param attrName 请求属性名称
	 * @return 请求属性值
     */
	public Object getAttributes(String attrName){
		return attributes.get(attrName);
	}

	/**
	 * 设置请求属性
	 * @param attrName 请求属性名称
	 * @param attrValue 请求属性值
     */
	public void setAttributes(String attrName,Object attrValue){
		attributes.put(attrName,attrValue);
	}


	/**
	 * 解析请求参数
	 */
	private void  parseQueryString() {
		if(getQueryString()!=null){
			String[] parameterEquals = getQueryString().split("&");
			for(String parameterEqual :parameterEquals){
				int equalFlagPos = parameterEqual.indexOf("=");
				if(equalFlagPos>0){
					String name = parameterEqual.substring(0, equalFlagPos);
					String value = parameterEqual.substring(equalFlagPos+1, parameterEqual.length());
					try {
						parameters.put(name, URLDecoder.decode(value,characterSet));
					} catch (UnsupportedEncodingException e) {
						Logger.error("QueryString URLDecoder.decode failed by charset:"+characterSet,e);
					}
				}else{
					parameters.put(parameterEqual, null);
				}
			}
		}
	}



	/**
	 * 重置请求
	 * 		用于在 Filter 中重新定向,其他地方无用
	 * @param url 请求地址,"/"起始,可以包含"?"参数引导及参数.
	 */
	public void redirect(String url){
		String[] parsedURL = url.split("\\?");

		this.protocol().clear();
		this.body().clear();
		this.parts().clear();

		if(parsedURL.length>0) {
			this.protocol().setPath(parsedURL[0]);
		}

		if(parsedURL.length > 1) {
			this.protocol().setQueryString(parsedURL[1]);
		}
	}
}
