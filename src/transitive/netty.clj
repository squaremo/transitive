(ns transitive.netty
  (import [io.netty.buffer ByteBuf]
          [io.netty.bootstrap ServerBootstrap]
          [io.netty.channel
           Channel
           ChannelHandlerContext
           ChannelInboundHandlerAdapter
           ChannelFuture
           ChannelInitializer
           ChannelOption
           EventLoopGroup]
          [io.netty.channel.nio NioEventLoopGroup]
          [io.netty.channel.socket SocketChannel]
          [io.netty.channel.socket.nio NioServerSocketChannel]))

(defn make-channel-adapter []
  (proxy [ChannelInboundHandlerAdapter] []
    (channelRead
      [^ChannelHandlerContext ctx buf]
      (.write ctx buf))
    (channelReadComplete [ctx]
      (.flush ctx))
    (exceptionCaught
      [^ChannelHandlerContext ctx ^Throwable cause]
      (.printStackTrace cause)
      (.close ctx))))

(defn make-channel-initializer []
  (proxy [ChannelInitializer] []
    (initChannel [^SocketChannel channel]
      (doto (.pipeline channel)
        (.addLast "echo" (make-channel-adapter))))))

(defn make-listener []
  (let [bosses  (NioEventLoopGroup.)
        workers (NioEventLoopGroup.)
        shutdown #(do (.shutdownGracefully bosses)
                      (.shutdownGracefully workers))]
    (let [boot (-> (ServerBootstrap.)
                   (.group bosses workers)
                   (.channel NioServerSocketChannel)
                   (.childHandler (make-channel-adapter))
                   (.option ChannelOption/SO_BACKLOG (int 128))
                   (.childOption ChannelOption/SO_KEEPALIVE true))]
      [boot, shutdown])))
