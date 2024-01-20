package org.example;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.handler.codec.MessageToMessageEncoder;
import io.netty.util.CharsetUtil;

import java.util.List;
import java.util.UUID;

public class Info {

    public static void main(String[] args) throws InterruptedException {
        NioEventLoopGroup bossGroup = new NioEventLoopGroup();
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap()
                    .group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new RequestDecoder());
//                            pipeline.addLast(new ResponseEncoder());
                            pipeline.addLast(new RequestHandler());
                        }
                    });

            ChannelFuture channelFuture = serverBootstrap.bind(8080).sync();
            channelFuture.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

// Custom request data class
class RequestData {
    private byte[] bytes;

    public RequestData() {
    }

    public byte[] getBytes() {
        return bytes;
    }

    public void setBytes(byte[] bytes) {
        this.bytes = bytes;
    }
}

// Custom response data class
class ResponseData {
    private String content;

    public ResponseData(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    // Constructor, getters, and setters
}

// Decoder to convert incoming ByteBuf to RequestData
class RequestDecoder extends MessageToMessageDecoder<ByteBuf> {
    private String id;

    public RequestDecoder() {
        id = UUID.randomUUID().toString();
        System.out.println("created decoder... " + id);
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf msg, List<Object> out) {
        // you have to drain the whole input buffer no matter what; there is one decoder per connection
        // so that means you can organize state without issue of concurrency
        out.add(Unpooled.copiedBuffer(msg));
    }
}

// Encoder to convert ResponseData to outgoing ByteBuf
class ResponseEncoder extends MessageToMessageEncoder<ResponseData> {
    @Override
    protected void encode(ChannelHandlerContext ctx, ResponseData msg, List<Object> out) {
        ByteBuf buffer = Unpooled.copiedBuffer(msg.getContent(), CharsetUtil.UTF_8);
        out.add(buffer);
    }
}

// Custom handler to process incoming requests and send responses
class RequestHandler extends SimpleChannelInboundHandler<ByteBuf> {
    private String id;


    public RequestHandler() {
        id = UUID.randomUUID().toString();
        System.out.println("created... " + id);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, ByteBuf requestData) {
        byte[] info = new byte[1024];
        int size = 0;
        while (requestData.isReadable()) {
            byte bt = requestData.readByte();

            info[size] = bt;
            size ++;
        }

        String val = new String(info, 0, size);
        System.out.println(val);

//        // Process the request and generate a response
//        String requestContent = requestData.getContent();
//        String responseContent = "Processed: " + requestContent;
//        ResponseData responseData = new ResponseData(responseContent);
//
//        // Send the response back to the client
//        ctx.writeAndFlush(responseData);
        ByteBuf buffer = Unpooled.copiedBuffer("infonec", CharsetUtil.UTF_8);
        ctx.writeAndFlush(buffer);
    }
}
