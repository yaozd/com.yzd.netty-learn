## netty-dns
```
DNS服务默认使用的UDP的客户端
```

## DNS规范：
- [https://tools.ietf.org/html/rfc1035](https://tools.ietf.org/html/rfc1035)
- [DNS协议详解](http://blog.chinaunix.net/uid-12077794-id-91657.html)
- RCODE(响应码)
```
RCODE:占4位，1/2字节。由回复时指定的返回码：0:无差错；1:格式错；2:DNS出错；3:域名不存在；4:DNS不支持这类查询；5:DNS拒绝查询；6-15:保留字段。
RCODE           Response code - this 4 bit field is set as part of
                responses.  The values have the following
                interpretation:

                0               No error condition

                1               Format error - The name server was
                                unable to interpret the query.

                2               Server failure - The name server was
                                unable to process this query due to a
                                problem with the name server.

                3               Name Error - Meaningful only for
                                responses from an authoritative name
                                server, this code signifies that the
                                domain name referenced in the query does
                                not exist.

                4               Not Implemented - The name server does
                                not support the requested kind of query.

                5               Refused - The name server refuses to
                                perform the specified operation for
                                policy reasons.  For example, a name
                                server may not wish to provide the
                                information to the particular requester,
                                or a name server may not wish to perform
                                a particular operation (e.g., zone

```
- 查询的资源记录类型
```
enum QueryType //查询的资源记录类型。
{
A=0x01, //指定计算机 IP 地址。
NS=0x02, //指定用于命名区域的 DNS 名称服务器。
MD=0x03, //指定邮件接收站（此类型已经过时了，使用MX代替）
MF=0x04, //指定邮件中转站（此类型已经过时了，使用MX代替）
CNAME=0x05, //指定用于别名的规范名称。
SOA=0x06, //指定用于 DNS 区域的“起始授权机构”。
MB=0x07, //指定邮箱域名。
MG=0x08, //指定邮件组成员。
MR=0x09, //指定邮件重命名域名。
NULL=0x0A, //指定空的资源记录
WKS=0x0B, //描述已知服务。
PTR=0x0C, //如果查询是 IP 地址，则指定计算机名；否则指定指向其它信息的指针。
HINFO=0x0D, //指定计算机 CPU 以及操作系统类型。
MINFO=0x0E, //指定邮箱或邮件列表信息。
MX=0x0F, //指定邮件交换器。
TXT=0x10, //指定文本信息。 
AAAA=0x1c,//IPV6资源记录。
UINFO=0x64, //指定用户信息。
UID=0x65, //指定用户标识符。
GID=0x66, //指定组名的组标识符。
ANY=0xFF //指定所有数据类型。
};
```
- Opcode：
```
通常值为0（标准查询），其他值为1（反向查询）和2（服务器状态请求）。
```
- RD：表示期望递归


## 参考：
- DNSJava
    - [https://github.com/dnsjava/dnsjava](https://github.com/dnsjava/dnsjava)
- Netty DNS resolver library
    - [https://github.com/netty/netty/blob/4.1/example/src/main/java/io/netty/example/dns/udp/DnsClient.java](https://github.com/netty/netty/blob/4.1/example/src/main/java/io/netty/example/dns/udp/DnsClient.java)
- [https://github.com/nzhenry/dns-proxy](https://github.com/nzhenry/dns-proxy)

