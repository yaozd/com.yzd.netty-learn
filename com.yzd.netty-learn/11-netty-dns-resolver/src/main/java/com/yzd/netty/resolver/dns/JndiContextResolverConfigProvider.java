package com.yzd.netty.resolver.dns;

import lombok.extern.slf4j.Slf4j;

import javax.naming.Context;
import javax.naming.NamingException;
import javax.naming.directory.DirContext;
import javax.naming.directory.InitialDirContext;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.*;

/**
 * @Author: yaozh
 * @Description:
 */
@Slf4j
public class JndiContextResolverConfigProvider {

    private static final JndiContextResolverConfigProvider mInstance=new JndiContextResolverConfigProvider();
    private List<InetSocketAddress> nameServers = new ArrayList<>(3);

    private JndiContextResolverConfigProvider() {
        initialize();
    }

    public static JndiContextResolverConfigProvider getInstance() {
        return mInstance;
    }

    public void initialize() {
        Hashtable<String, String> env = new Hashtable<>();
        env.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.dns.DnsContextFactory");
        // http://mail.openjdk.java.net/pipermail/net-dev/2017-March/010695.html
        env.put("java.naming.provider.url", "dns://");

        String servers = null;
        try {
            DirContext ctx = new InitialDirContext(env);
            servers = (String) ctx.getEnvironment().get("java.naming.provider.url");
            ctx.close();
        } catch (NamingException e) {
            // ignore
        }
        if (servers != null) {
            StringTokenizer st = new StringTokenizer(servers, " ");
            while (st.hasMoreTokens()) {
                String server = st.nextToken();
                try {
                    URI serverUri = new URI(server);
                    String host = serverUri.getHost();
                    if (host == null || host.isEmpty()) {
                        // skip the fallback server to localhost
                        continue;
                    }
                    int port = serverUri.getPort();
                    if (port == -1) {
                        port = DnsConstants.DEFAULT_PORT;
                    }
                    addNameServer(new InetSocketAddress(host, port));
                } catch (URISyntaxException e) {
                    log.debug("Could not parse {} as a dns server, ignoring", server, e);
                }
            }
        }
    }

    private void addNameServer(InetSocketAddress server) {
        if (!nameServers.contains(server)) {
            nameServers.add(server);
            log.debug("Added {} to nameservers", server);
        }
    }

    public final List<InetSocketAddress> servers() {
        return Collections.unmodifiableList(nameServers);
    }

    public InetSocketAddress getFirstNameServer() {
        List<InetSocketAddress> servers = servers();
        if (servers == null || servers.isEmpty()) {
            throw new NullPointerException("not found name server");
        }
        return servers.get(0);
    }
}
