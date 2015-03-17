package utils;

import java.io.*;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;

import javax.security.auth.callback.CallbackHandler;

import net.spy.memcached.AddrUtil;
import net.spy.memcached.ConnectionFactory;
import net.spy.memcached.ConnectionFactoryBuilder;
import net.spy.memcached.MemcachedClient;
import net.spy.memcached.auth.AuthDescriptor;
import net.spy.memcached.auth.PlainCallbackHandler;
import net.spy.memcached.transcoders.SerializingTranscoder;
import play.Play;

public class MemCachierClient extends MemcachedClient {

    private static volatile MemCachierClient instance = null;

    public static MemCachierClient getInstance() {
        synchronized (MemCachierClient.class) {
            if (instance == null) {
                String username = Play.application().configuration().getString("memcached.username");
                String password = Play.application().configuration().getString("memcached.password");
                String servers = Play.application().configuration().getString("memcached.servers");

                try {
                    if ((username != null) && (password != null)) {
                        instance = new MemCachierClient(username, password, servers);
                    }
                    else {
                        ConnectionFactory config = new ConnectionFactoryBuilder()
                                .setTranscoder(new CustomSerializingTranscoder())
                                .setProtocol(ConnectionFactoryBuilder.Protocol.BINARY)
                                .setDaemon(true)
                                .build();
                        instance = new MemCachierClient(config, AddrUtil.getAddresses(servers));
                    }
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }

        return instance;
    }

    private MemCachierClient(String username, String password, String servers) throws IOException {
        this(new SASLConnectionFactoryBuilder().build(username, password), getAddresses(servers));
    }

    private MemCachierClient(ConnectionFactory cf, List<InetSocketAddress> addrs) throws IOException {
        super(cf, addrs);
    }

    private static List<InetSocketAddress> getAddresses(String servers) {
        List<InetSocketAddress> addrList = new ArrayList<InetSocketAddress>();
        for (String server : servers.split(",")) {
            String addr = server.split(":")[0];
            int port = Integer.parseInt(server.split(":")[1]);
            addrList.add(new InetSocketAddress(addr, port));
        }
        return addrList;
    }
}

class SASLConnectionFactoryBuilder extends ConnectionFactoryBuilder {
    public ConnectionFactory build(String username, String password) {
        CallbackHandler ch = new PlainCallbackHandler(username, password);
        AuthDescriptor ad = new AuthDescriptor(new String[]{"PLAIN"}, ch);
        this.setTranscoder(new CustomSerializingTranscoder());
        this.setProtocol(Protocol.BINARY);
        this.setDaemon(true);
        this.setAuthDescriptor(ad);
        return this.build();
    }
}

class CustomSerializingTranscoder extends SerializingTranscoder {

    @Override
    protected Object deserialize(byte[] bytes) {
        final ClassLoader currentClassLoader = Thread.currentThread().getContextClassLoader();
        ObjectInputStream in = null;
        try {
            ByteArrayInputStream bs = new ByteArrayInputStream(bytes);
            in = new ObjectInputStream(bs) {
                @Override
                protected Class<?> resolveClass(ObjectStreamClass objectStreamClass) throws IOException, ClassNotFoundException {
                    try {
                        return currentClassLoader.loadClass(objectStreamClass.getName());
                    } catch (Exception e) {
                        return super.resolveClass(objectStreamClass);
                    }
                }
            };
            return in.readObject();
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } finally {
            closeStream(in);
        }
    }

    private static void closeStream(Closeable c) {
        if (c != null) {
            try {
                c.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}