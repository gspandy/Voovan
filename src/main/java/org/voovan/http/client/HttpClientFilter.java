package org.voovan.http.client;

import org.voovan.http.message.HttpParser;
import org.voovan.http.message.Request;
import org.voovan.http.message.Response;
import org.voovan.http.websocket.WebSocketFrame;
import org.voovan.network.IoFilter;
import org.voovan.network.IoSession;
import org.voovan.network.exception.IoFilterException;
import org.voovan.tools.ByteBufferChannel;
import org.voovan.tools.TByteBuffer;
import org.voovan.tools.TObject;
import org.voovan.tools.log.Logger;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

/**
 * HTTP 请求过滤器
 * @author helyho
 * 
 * Voovan Framework.
 * WebSite: https://github.com/helyho/Voovan
 * Licence: Apache v2 License
 */
public class HttpClientFilter implements IoFilter {

	@Override
	public Object encode(IoSession session,Object object) {
		if(object instanceof WebSocketFrame){
			return ((WebSocketFrame)object).toByteBuffer();
		}
		if(object instanceof Request){
			Request request = TObject.cast(object);
			try {
				request.send(session);
			} catch (IOException e) {
				Logger.error(e);
			}
			return ByteBuffer.allocateDirect(0);
		}
		return null;
	}

	@Override
	public Object decode(IoSession session,Object object) throws IoFilterException{
		try{
			ByteBufferChannel byteBufferChannel = session.getByteBufferChannel();
			if("WebSocket".equals(session.getAttribute("Type"))){
				return WebSocketFrame.parse((ByteBuffer)object);
			}

			if(object instanceof ByteBuffer){
				session.enabledMessageSpliter(false);
				ByteBuffer byteBuffer = TObject.cast(object);
				Response response = HttpParser.parseResponse(byteBufferChannel, session.socketContext().getReadTimeout());
				session.enabledMessageSpliter(true);
				return response;
			}
		}catch(IOException e){
			throw new IoFilterException("HttpClientFilter decode Error. "+e.getMessage(),e);
		}
		return null;
	}
}
