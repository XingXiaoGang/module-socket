package com.gang.soket;

import android.util.Log;

import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketConnector;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

/**
 * Created by xingxiaogang on 2016/5/27.
 */
public class MinaClient {

    ClientThread thread;

    public MinaClient() {

    }

    public void start(String ip, int port) {
        thread = new ClientThread(ip, port);
        thread.start();
    }

    public void sendMessage(String msg) {
        thread.sendMessage(msg);
    }

    class ClientThread extends Thread {

        private String ipAdress;
        private int port;
        private IoSession session;

        public ClientThread(String ip, int port) {
            this.ipAdress = ip;
            this.port = port;
        }

        public void sendMessage(String msg) {
            session.write(msg);
        }

        @Override
        public void run() {
            super.run();
            IoConnector connector = new NioSocketConnector();
            //设置链接超时时间
            connector.setConnectTimeoutMillis(30000);
            //添加过滤器
            //connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new CharsetCodecFactory()));
            connector.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"), LineDelimiter.WINDOWS.getValue(), LineDelimiter.WINDOWS.getValue())));
            connector.setHandler(new MinaClientHandler());
            try {
                ConnectFuture future = connector.connect(new InetSocketAddress(ipAdress, port));//创建链接
                future.awaitUninterruptibly();// 等待连接创建完成
                session = future.getSession();//获得session
                session.write("start");
            } catch (Exception e) {
                Log.d("TEST", "客户端链接异常...");
            }
            session.getCloseFuture().awaitUninterruptibly();//等待连接断开
            Log.d("TEST", "客户端断开...");
            connector.dispose();
        }
    }

    public class MinaClientHandler extends IoHandlerAdapter {

        @Override
        public void exceptionCaught(IoSession session, Throwable cause)
                throws Exception {
            Log.d("TEST", "客户端发生异常");
            super.exceptionCaught(session, cause);
        }

        @Override
        public void messageReceived(IoSession session, Object message)
                throws Exception {
            String msg = message.toString();
            Log.d("TEST", "客户端接收到的信息为:" + msg);
            super.messageReceived(session, message);
        }

        @Override
        public void messageSent(IoSession session, Object message) throws Exception {
            Log.d("TEST", "messageSent:");
            super.messageSent(session, message);
        }
    }
}
