package com.gang.soket;

import android.util.Log;

import org.apache.mina.core.service.IoAcceptor;
import org.apache.mina.core.service.IoHandlerAdapter;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.filter.codec.ProtocolCodecFilter;
import org.apache.mina.filter.codec.textline.LineDelimiter;
import org.apache.mina.filter.codec.textline.TextLineCodecFactory;
import org.apache.mina.transport.socket.nio.NioSocketAcceptor;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.util.Date;

/**
 * Created by xingxiaogang on 2016/5/27.
 */
public class MinaServer {

    private static MinaServer mInstance;
    IoAcceptor acceptor = null;
    private ServerThread mServer;

    public static MinaServer getInstance() {
        if (mInstance == null) {
            mInstance = new MinaServer();
        }
        return mInstance;
    }

    private MinaServer() {
        //创建一个非阻塞的server端的Socket
        acceptor = new NioSocketAcceptor();
    }

    public void start(int port) {
        if (mServer == null || !mServer.isAlive()) {
            mServer = new ServerThread(acceptor, port);
        }
        mServer.start();
    }

    public void stop() {
        mServer.stopServer();
    }

    private class ServerThread extends Thread {

        private IoAcceptor mAcceptor;
        private int mPort;

        ServerThread(IoAcceptor acceptor, int port) {
            this.mAcceptor = acceptor;
            this.mPort = port;
        }

        @Override
        public void run() {
            super.run();
            try {
                //设置过滤器（使用mina提供的文本换行符编解码器）
                acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new TextLineCodecFactory(Charset.forName("UTF-8"), LineDelimiter.WINDOWS.getValue(), LineDelimiter.WINDOWS.getValue())));
                //自定义的编解码器
                //acceptor.getFilterChain().addLast("codec", new ProtocolCodecFilter(new CharsetCodecFactory()));
                //设置读取数据的换从区大小
                acceptor.getSessionConfig().setReadBufferSize(2048);
                //读写通道10秒内无操作进入空闲状态
                acceptor.getSessionConfig().setIdleTime(IdleStatus.BOTH_IDLE, 10);
                //为接收器设置管理服务
                acceptor.setHandler(new TextServerHandler());
                //绑定端口
                acceptor.bind(new InetSocketAddress(mPort));

                Log.e("test_socket", "服务器启动成功...    端口号未：" + mPort);

            } catch (Exception e) {
                Log.e("test_socket", "服务器启动异常..." + e.getMessage());
                e.printStackTrace();
            }
        }

        public void stopServer() {
            mAcceptor.unbind();
        }
    }

    /**
     * 消息处理
     **/
    private class TextServerHandler extends IoHandlerAdapter {

        //从端口接受消息，会响应此方法来对消息进行处理
        @Override
        public void messageReceived(IoSession session, Object message)
                throws Exception {
            String msg = message.toString();
            if ("exit".equals(msg)) {
                //如果客户端发来exit，则关闭该连接
                session.close(true);
            }
            //向客户端发送消息
            Date date = new Date();
            session.write(date);
            Log.e("test_socket", "服务器接受消息成功...msg:" + msg);
            super.messageReceived(session, message);
        }

        //向客服端发送消息后会调用此方法
        @Override
        public void messageSent(IoSession session, Object message) throws Exception {
            super.messageSent(session, message);
            Log.e("test_socket", "服务器发送消息成功...");
        }

        //关闭与客户端的连接时会调用此方法
        @Override
        public void sessionClosed(IoSession session) throws Exception {
            super.sessionClosed(session);
            Log.e("test_socket", "服务器与客户端断开连接...");
        }

        //服务器与客户端创建连接
        @Override
        public void sessionCreated(IoSession session) throws Exception {
            super.sessionCreated(session);
            Log.e("test_socket", "服务器与客户端创建连接...");
        }

        //服务器与客户端连接打开
        @Override
        public void sessionOpened(IoSession session) throws Exception {
            super.sessionOpened(session);
            Log.e("test_socket", "服务器与客户端连接打开...");
        }

        @Override
        public void sessionIdle(IoSession session, IdleStatus status)
                throws Exception {
            super.sessionIdle(session, status);
            Log.e("test_socket", "服务器进入空闲状态...");
        }

        @Override
        public void exceptionCaught(IoSession session, Throwable cause)
                throws Exception {
            super.exceptionCaught(session, cause);
            Log.e("test_socket", "服务器发送异常...");
        }
    }

}
